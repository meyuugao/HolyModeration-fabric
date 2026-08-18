# Rendering — Reference Notes (HolyModeration Fabric)

Scope: **rendering only**. Written from the 1.21.11 branch (main line of work), with a
comparison section for 1.20.1 built from already-collected data. Purpose: a single
reference for future render-related work on both branches. No build/config/mixin-meta
topics except where they directly touch rendering.

---

## 1. TL;DR — the two eras

| | 1.20.1 (legacy) | 1.21.11 (current) |
|---|---|---|
| Shader API | `ShaderProgram` + JSON (`assets/minecraft/shaders/core/*.json`) | `RenderPipeline` built in code from `core/*.vsh`/`*.fsh` + `#moj_import` |
| Immediate draw | `Tessellator` + `BufferBuilder` + `t.draw()` | `CommandEncoder` + `RenderPass` + `drawIndexed` |
| Matrix stack | `MatrixStack` (3D, `peek().getPositionMatrix()`) | `Matrix3x2fStack` (2D affine, `DrawContext.getMatrices()`) |
| GUI z-order | painter's algorithm (unified; was depth `GL_LEQUAL` + `translate(x,y,z)`) | painter's algorithm — submission order, fixed z `-11000` |
| Text | `TextRenderer.draw(...)` into `VertexConsumerProvider.Immediate` then `.draw()` | `TextRenderer.prepare(...)` → `GlyphDrawable.draw(GlyphDrawer)` into `BufferBuilder`, immediate pass with `RenderPipelines.GUI_TEXT` |
| Image | `ctx.drawTexture` inside `matrices.translate(0,0,z)` (immediate-ish) | deferred `ctx.drawTexture` OR immediate `RenderPipelines.GUI_TEXTURED` pass |
| Global state | `RenderSystem.enableBlend/depthFunc/setShaderColor` | pipeline objects carry blend/depth state; `RenderSystem` only holds shared UBOs |

The single most important mental model:

> **1.21.11 GUI has no depth buffer for UI. Layer order = draw order.**
> `getRenderPriority()` must map to *submission order*, never to a depth z.

---

## 2. Render flow in the mod (both branches share this architecture)

1. **Mixin entry point** posts a `RenderEvent` on the custom EventBus.
   - `GameRendererMixin` — injects into `GameRenderer.render` **after**
     `GuiRenderer.render(...)` (i.e. after the vanilla deferred GUI is flushed).
     Grabs the live `DrawContext` via MixinExtras `@Local` and posts
     `RenderEvent(drawContext, mouseX, mouseY, tickProgress)` with real mouse
     coords from `client.mouse.getScaledX/Y(client.getWindow())`. This is the
     **single** render entry point: one draw per frame, always on top of vanilla
     GUI. (The old `ScreenMixin` render post was removed — it drew too early,
     before the vanilla flush, putting vanilla UI on top of the widgets.)
2. `GuiManagerModule.onRender(RenderEvent)` iterates
   `GuiManagerService.getDrawableModules()` (already sorted) and calls
   `drawableModule.render(ctx, mode)`.
   - `mode = CONFIG` when `currentScreen instanceof MainGuiScreen`, else `LIVE`.
3. `DrawableModule<T extends StatefulDrawableElement<?>>.render(ctx, mode)` calls
   `drawableElement.updateRenderForScreen(ctx, getRenderPriority(), mode)`.
4. `DrawableElement.updateRender` wraps the actual draw in matrix push/pop:
   `push → translate(anchorX,anchorY) → scale(screenScale) → scale(scale) → translate(-basePivotX,-basePivotY) → render() → pop`.
5. Concrete elements (`WatermarkDrawableElement`, `SpyDrawableElement`, buttons,
   color picker, `NotificationsDrawableElement`, `ReportsParserDrawableElement`,
   `CheckoutsDrawableElement`) draw via `Render2DService`.

### Render priorities (1.21.11) — the layer order

`GuiManagerService.addDrawableModule` sorts ascending by `getRenderPriority()`;
**ascending = drawn first = bottom layer** in painter's algorithm.

| Module | priority |
|---|---|
| `WaterMarkModule` | 1000 |
| `SpyModule` | 1001 |
| `CheckoutsModule` | 1002 |
| `ReportsParserModule` | 1003 |
| `NotificationsModule` | 1004 |

> In 1.20.1 the same integer was a **depth z** (bigger = closer to camera = on top).
> In 1.21.11 the same integer must mean **later in submission order = on top**.
> The numbers already sort identically; only the *mechanism* changed.

---

## 3. `Render2DService` (1.21.11) — the core class

Path: `client/util/service/Render2DService.java`. `@Singleton`, injected via
`@RequiredArgsConstructor(onConstructor_ = @Inject)`.

### 3.1 Public API

- `initializeShaders(ResourceManager)` — builds all pipelines once.
- `renderRect / renderRoundedRect / renderSoftRoundedRect / renderRoundedRectOutline / renderSoftRoundedRectOutline / renderRGBPalette(ctx, x, y, w, h, z, ...)` — custom shader quads.
- `renderImage(ctx, Identifier, x, y, w, h, z)` — textured quad.
- `renderText(tr, String|OrderedText, x, y, z, color, shadow, ctx)` — text.
- `setupRender()` / `endRender()` — per-element state hooks (color modulator reset).

### 3.2 Constants

```java
GUI_NEAR = 1000.0f;   // matches vanilla gui projection
GUI_FAR  = 11000.0f;
GUI_Z    = -11000.0f; // matches vanilla gui DynamicTransforms translation z
UBO_SIZE = 16;        // one Std140 vec4
```

### 3.3 Pipeline construction (`buildPipeline`)

```java
RenderPipeline.builder()
    .withLocation("pipeline/hm_*")          // unique registry location
    .withVertexShader("core/rect")          // assets/minecraft/shaders/core/rect.vsh
    .withFragmentShader("core/rect")
    .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
    .withUniform("Projection", UniformType.UNIFORM_BUFFER)
    .withBlend(BlendFunction.TRANSLUCENT)
    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)  // MUST match vanilla GUI
    .withDepthWrite(false)                                    // MUST match vanilla GUI
    .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS)
    // + one .withUniform per custom float/vec2/vec4 ("Color", "Radius", "Size", ...)
```

**Critical defaults** in `RenderPipeline.Builder.build()` (when NOT set explicitly):

- `blendFunction = empty` → **no blending at all** (alpha ignored). Always set `.withBlend(...)` for translucent UI.
- `depthTestFunction = DepthTestFunction.LEQUAL_DEPTH_TEST` → **depth test ON by default**.
  For GUI you MUST override with `NO_DEPTH_TEST`, otherwise quads fail the depth
  test against the (possibly stale) main framebuffer depth and show rendering
  artifacts (darkening / missing pixels) — this was bug #1's root cause.
- `writeDepth = true`, `writeColor = true`, `writeAlpha = true`, `cull = true`.

### 3.4 Submitting a pass (`submitPass`)

The one path every draw goes through:

1. `GpuDevice device = RenderSystem.getDevice()`.
2. `CommandEncoder encoder = device.createCommandEncoder()`.
3. Optional uniform uploads **before** the pass: `encoder.writeToBuffer(uniformBuffer(name).slice(), byteBuffer)`.
4. Upload vertices: `device.createBuffer("hm_quad_vbo", USAGE_VERTEX | USAGE_COPY_DST, built.getBuffer())` — then `vbo.close()` in `finally`.
5. `guiProjection().set(scaledW, scaledH)` → `GpuBufferSlice projection` (ProjMat UBO).
6. `RenderSystem.getDynamicUniforms().write(modelView.setTranslation(0,0,GUI_Z), guiColorModulator, guiModelOffset, guiTextureMat)` → `GpuBufferSlice transforms`.
7. `encoder.createRenderPass(() -> "hm_render_pass", framebuffer.getColorAttachmentView(), OptionalInt.empty())`.
8. `pass.setPipeline(pipeline); pass.setUniform("Projection", projection); pass.setUniform("DynamicTransforms", transforms);` + extras.
9. Index buffer: `RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS)` →
   `shapeIndexBuffer.getIndexBuffer(indexCount)` / `getIndexType()`.
10. `pass.setVertexBuffer(0, vbo); pass.setIndexBuffer(...); pass.drawIndexed(0, 0, indexCount, 1);`

**Use `built.getDrawParameters().indexCount()`** — never a hardcoded `6` — because
text passes contain many quads. A hardcoded 6 only works for a single quad.

### 3.5 Custom shader quad geometry (pixel-space UV)

Quads are built in `VertexFormats.POSITION_TEXTURE_COLOR` with the position
transformed through the `DrawContext` 2D matrix manually:

```java
float[] p0 = transform(matrices, x,     y + h);
float[] p1 = transform(matrices, x + w, y + h);
float[] p2 = transform(matrices, x + w, y);
float[] p3 = transform(matrices, x,     y);
```

`transform()` reads `Matrix3x2fStack` fields directly:

```java
x' = m00*x + m10*y + m20
y' = m01*x + m11*y + m21
```

UV is **not normalized** — the fragment shaders use it as local pixel coords:

```java
vertex(p0).color(1,1,1,1).texture(0, h);
vertex(p1).color(1,1,1,1).texture(w, h);
vertex(p2).color(1,1,1,1).texture(w, 0);
vertex(p3).color(1,1,1,1).texture(0, 0);
```

The custom shaders (`rounded_rect.fsh`, `soft_rounded_rect.fsh`, ...) compute
`p = uv - size*0.5` against the `Size` uniform — so `Size` must equal `(w,h)`
and the vertex UV must span `0..w, 0..h`.

### 3.6 Uniform buffers

- One shared `GpuBuffer` per uniform *name* (`uniformBuffers` map), reused across
  frames; `UBO_SIZE = 16` bytes, direct `ByteBuffer`.
- `writeFloat` → `Std140Builder.putFloat` (4 B), `writeVec2` → 8 B,
  `writeVec4(Color)` → 16 B with `color.getAlpha()/255f`.
- Created as `GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST` so the encoder
  can `writeToBuffer` into them every frame.

### 3.7 Text — immediate path (`renderText`)

```java
tr.prepare(text, x, y, color, shadow, false, 0)   // OrderedText variant
tr.prepare(string, x, y, color, shadow, 0)        // String variant
```

`prepare(...)` returns `TextRenderer.GlyphDrawable`. Then:

```java
Matrix4f matrix = new Matrix4f().mul(ctx.getMatrices());
BufferBuilder bb = Tessellator.getInstance().begin(QUADS, VertexFormats.POSITION_COLOR_TEXTURE_LIGHT);
glyphDrawable.draw(new GlyphDrawer() { ... });
```

Inside the drawer, for each `TextDrawable`:

```java
drawable.render(matrix, bb, 15728880, /*noDepth=*/true);
```

`15728880` = `0xF000F0` = full lightmap. `noDepth=true` forces glyph z to `0`
(vanilla GUI behavior), otherwise glyphs emit z `0.001`/`0.03` shadow offsets.

Capture `drawable.getPipeline()` and `drawable.textureView()` from the first
glyph (all glyphs of one draw share the font atlas and pipeline):

- `getPipeline()` → `TextRenderLayerSet.guiPipeline()` → `RenderPipelines.GUI_TEXT`
  (or `GUI_TEXT_INTENSITY`), which is **NO_DEPTH_TEST** — correct for GUI.
- `textureView()` → the font atlas `GpuTextureView`.

Then submit one pass binding:

- `Sampler0` = font atlas view, sampler `RenderSystem.getSamplerCache().get(FilterMode.NEAREST)`.
- `Sampler2` = `gameRenderer.getLightmapTextureManager().getGlTextureView()`, sampler `FilterMode.LINEAR`.
- `Fog` = `RenderSystem.getShaderFog()` if non-null (pipeline declares a `Fog` UBO).

**Empty text**: `BufferBuilder.end()` throws `IllegalStateException("BufferBuilder was empty")`
when zero glyphs were written. Use `endNullable()` and bail out on `null`.

> Do **NOT** use `RenderLayers.text` / the `RENDERTYPE_TEXT` path for GUI text:
> it is `LEQUAL_DEPTH_TEST` + fog and is meant for in-world text; it fails against
> the main depth buffer and gets fog applied. The GUI-correct pipeline is
> `RenderPipelines.GUI_TEXT` (`GUI_TEXT_SNIPPET` = TEXT_SNIPPET + NO_DEPTH_TEST).

### 3.8 Image — immediate path (`renderImage`)

1. `AbstractTexture at = client.getTextureManager().getTexture(id);`
2. Build `POSITION_TEXTURE_COLOR` quad (uv `0..1`, color white).
3. Submit pass with `RenderPipelines.GUI_TEXTURED`, binding
   `Sampler0 = at.getGlTextureView()`, sampler `at.getSampler()`.

`RenderPipelines.GUI_TEXTURED` = `POSITION_TEX_COLOR_SNIPPET`
(NO_DEPTH_TEST + TRANSLUCENT blend) — exactly what `DrawContext.drawTexture` uses
when deferred, but here drawn immediately in the mod's own pass order.

### 3.9 setupRender / endRender

`setupRender()` resets the shared `guiColorModulator` to white (the custom shaders
multiply by it via the `DynamicTransforms` UBO; the mod reuses one field so it must
never be left tinted). `endRender()` is intentionally empty — blend/depth are owned
by pipelines, not global state.

---

## 4. Why text/images "rendered on one layer" (bug #2) — root cause

In the *original* 1.21.11 port:

- `renderQuad` (custom shaders) drew **immediately** (own `RenderPass`).
- `renderText`/`renderImage` called `ctx.drawText`/`ctx.drawTexture`, which are
  **deferred**: they append to `DrawContext.state` (`GuiRenderState`) and are only
  flushed by the vanilla `GuiRenderer.render()` call at the very end of
  `GameRenderer.render` (after `InGameHud.render`).

Result: every deferred element (all text, all images) was drawn **after** all
immediate quads — i.e. text and icons always sat on top of every widget's
background, regardless of `getRenderPriority()`.

**Fix**: make text and images immediate (sections 3.7/3.8) so every primitive is
drawn in the same submission stream, in priority order.

---

## 5. Z-layer unification decision (task #3)

- 1.20.1 used a real depth buffer (`GL_LEQUAL`) with `matrices.translate(x, y, z)`,
  `z = getRenderPriority()` → true z-layering.
- 1.21.11 vanilla GUI uses **painter's algorithm**: no depth, fixed
  `z = -11000`, `NO_DEPTH_TEST`, layer order = submission order.

**Chosen approach: painter's algorithm (1.21.11 style) on both.** It is the
native 1.21.11 concept, requires no depth attachment, and the mod already sorts
modules by `getRenderPriority()` ascending — which gives the exact same visual
layer order as the old depth-based system. Priority is kept as the single source
of truth for layer order.

**Applied to both branches.** 1.21.11 ships it natively. 1.20.1 was migrated:
depth test removed from `setupRender`/`endRender`, `renderImage` converted from
deferred `ctx.drawTexture` to an immediate `Tessellator` quad (so it draws after
the vanilla GUI flush instead of inside it), and the single `GameRendererMixin`
post now fires after `DrawContext.draw()`. Both branches now have exactly one
render entry point, after the vanilla GUI flush, in priority order, no depth.

---

## 6. Vanilla 1.21.11 reference classes (read these for future work)

Decompiled sources live in the loom sources jar:
`.gradle/loom-cache/minecraftMaven/net/minecraft/minecraft-merged-<hash>/<ver>/minecraft-merged-<hash>-<ver>-sources.jar`
(e.g. `...minecraft-merged-2ae02fda0f-1.21.11-net.fabricmc.yarn.1_21_11.1.21.11+build.6-v2-sources.jar`).

| Class | What to learn from it |
|---|---|
| `net.minecraft.client.gui.render.GuiRenderer` | The **reference GUI renderer**. Painter's algorithm: `prepare()` flushes immediate consumers, sorts simple elements (`SIMPLE_ELEMENT_COMPARATOR`: scissor → pipeline sortKey → textureSetup sortKey), splits BEFORE_BLUR/AFTER_BLUR. `renderPreparedDraws` sets projection `guiProjectionMatrix.set(fbW/scale, fbH/scale)`, DynamicTransforms `setTranslation(0,0,-11000)`, renders in order. |
| `net.minecraft.client.gui.DrawContext` | Deferred element collector. `fill/drawText/drawTexture/...` only append to `state` (`GuiRenderState`); `drawDeferredElements()`/`createNewRootLayer()` manage tooltips/blur layer. `getMatrices()` returns `Matrix3x2fStack`. |
| `net.minecraft.client.gui.render.state.GuiRenderState` | The bucket of deferred elements (simple/text/item/special) that `GuiRenderer` drains. |
| `net.minecraft.client.gui.render.state.SimpleGuiElementRenderState` / `ColoredQuadGuiElementRenderState` / `TexturedQuadGuiElementRenderState` | `setupVertices(VertexConsumer)` + `pipeline()` + `textureSetup()` — the pattern for building GUI quads. |
| `net.minecraft.client.gui.render.state.TextGuiElementRenderState` | Deferred text: `textRenderer.prepare(orderedText, x, y, color, shadow, trackEmpty, backgroundColor)` cached as `GlyphDrawable`. |
| `net.minecraft.client.gui.render.state.GlyphGuiElementRenderState` | **How vanilla draws GUI glyphs**: `renderable.render(new Matrix4f().mul(pose), vertices, 15728880, /*noDepth=*/true)` and `TextureSetup.withLightmap(renderable.textureView(), NEAREST)`. `pipeline()` returns `renderable.getPipeline()` (= GUI_TEXT). |
| `net.minecraft.client.font.TextRenderer` | `prepare(...)` → `GlyphDrawable`; `draw(String/Text/OrderedText, x, y, color, shadow, Matrix4f, VertexConsumerProvider, TextLayerType, backgroundColor, light)`; `GlyphDrawer.drawing(...)`. Constants `Z_INDEX=0.01F`, `FORWARD_SHIFT=0.03F`. |
| `net.minecraft.client.font.TextDrawable` | Interface: `render(Matrix4f, VertexConsumer, light, noDepth)`, `getRenderLayer(TextLayerType)`, `textureView()`, `getPipeline()`. |
| `net.minecraft.client.font.BakedGlyphImpl` | Glyph vertex emission: `POSITION_COLOR_TEXTURE_LIGHT`, `.vertex(matrix,x,y,z).color(c).texture(u,v).light(light)`; shadow/italic/bold offsets; `noDepth` forces `z=0`. |
| `net.minecraft.client.font.TextRenderLayerSet` | Maps a font atlas to `RenderLayers.text/seeThrough/polygonOffset` (in-world) + `guiPipeline` (`RenderPipelines.GUI_TEXT`/`GUI_TEXT_INTENSITY`). |
| `net.minecraft.client.font.FontStorage` | Glyph baking; produces `BakedGlyphImpl`s pointing at the font atlas texture. |
| `net.minecraft.client.render.RenderLayer` / `RenderLayers` | RenderLayer = pipeline + textures + lightmap + output target; `draw(BuiltBuffer)` shows the immediate in-world draw (uses LEQUAL depth for text). `RenderLayers.text(id)` → `RENDERTYPE_TEXT`. |
| `net.minecraft.client.render.RenderSetup` / `TextureSetup` | `RenderSetup` builds per-layer texture bindings; `TextureSetup.withLightmap(font, NEAREST)` → Sampler0+Sampler2; `TextureSetup.of(view, sampler)`. |
| `net.minecraft.client.gl.RenderPipelines` | All pipeline constants + snippets. Key: `GUI` (POSITION_COLOR, NO_DEPTH, TRANSLUCENT), `GUI_TEXTURED` (POSITION_TEX_COLOR, NO_DEPTH, TRANSLUCENT), `GUI_TEXT` / `GUI_TEXT_INTENSITY` (NO_DEPTH), `TEXT_SNIPPET` (LEQUAL default via TEXT snippets? — see below), `POSITION_TEX_COLOR_SNIPPET`. |
| `net.minecraft.client.render.ProjectionMatrix2` | ProjMat UBO: `setOrtho(0,w, invertY?h:0, invertY?0:h, near, far)`; vanilla gui `(1000, 11000, true)`. |
| `net.minecraft.client.gl.DynamicUniforms` | `write(modelView, colorModulator, modelOffset, textureMat)` → Std140 `mat4+vec4+vec3+mat4`. |
| `net.minecraft.client.texture.AbstractTexture` | `getGlTextureView()` / `getSampler()` for binding textures manually. |
| `net.minecraft.client.render.BuiltBuffer` | `getDrawParameters().indexCount()` / `.format()` / `.mode()`; `endNullable()` may return null. |
| `net.minecraft.client.render.BufferBuilder` | `end()` throws on empty; `endNullable()` safe; `begin(DrawMode, VertexFormat)` via `Tessellator.getInstance().begin(...)`. |
| `com.mojang.blaze3d.pipeline.RenderPipeline` (+ `Builder`) | `.withBlend/.withDepthTestFunction/.withDepthWrite/.withVertexFormat/.withUniform/.withSampler`; builder defaults (no blend, LEQUAL depth, writeDepth=true). |
| `com.mojang.blaze3d.pipeline.BlendFunction` | `TRANSLUCENT`, `TRANSLUCENT_PREMULTIPLIED_ALPHA`, `ADDITIVE`, `INVERT`, etc. |
| `com.mojang.blaze3d.platform.DepthTestFunction` | `NO_DEPTH_TEST`, `LEQUAL_DEPTH_TEST`. |
| `com.mojang.blaze3d.systems.RenderSystem` | `getDevice()`, `getDynamicUniforms()`, `getSequentialBuffer(DrawMode)` → `ShapeIndexBuffer` (`getIndexBuffer(n)` auto-grows, `getIndexType()`), `getShaderFog()`, `getSamplerCache().get(FilterMode)`, `bindDefaultUniforms(pass)` (binds Projection+Fog+Globals from current state). |
| `com.mojang.blaze3d.systems.CommandEncoder` / `RenderPass` | The pass API: `createRenderPass(label, colorView, OptionalInt.empty())` (+ optional depth view + clearDepth), `setPipeline`, `setUniform(name, GpuBuffer|GpuBufferSlice)`, `bindTexture(name, view, sampler)`, `setVertexBuffer`, `setIndexBuffer`, `drawIndexed(baseVertex, firstIndex, indexCount, instanceCount)`. |

---

## 7. Pipeline/shader facts (1.21.11)

### 7.1 Where shaders come from

New-style pipelines compile from `assets/minecraft/shaders/core/<name>.vsh/.fsh`.
Imports via `#moj_import <minecraft:dynamictransforms.glsl>` and
`<minecraft:projection.glsl>` (vanilla include files). The old per-shader `.json`
files (still present in `src/main/resources/assets/minecraft/shaders/core/*.json`)
are **leftovers of the 1.20.1 `ShaderProgram` format and are ignored** by the new
pipeline API.

### 7.2 Vanilla GUI shader snippets (from `RenderPipelines`)

- `GUI_SNIPPET`: `core/gui` vsh/fsh, blend TRANSLUCENT, `POSITION_COLOR`, NO_DEPTH.
- `POSITION_TEX_COLOR_SNIPPET`: `core/position_tex_color`, blend TRANSLUCENT,
  `POSITION_TEXTURE_COLOR`, NO_DEPTH, sampler Sampler0.
- `TEXT_SNIPPET`: blend TRANSLUCENT, `POSITION_COLOR_TEXTURE_LIGHT`.
- `GUI_TEXT_SNIPPET`: TEXT_SNIPPET + NO_DEPTH_TEST.

### 7.3 `gui.fsh` / `gui.vsh`

- `gui.vsh`: `gl_Position = ProjMat * ModelViewMat * vec4(Position,1)`, passes vertex color.
- `gui.fsh`: `fragColor = vertexColor * ColorModulator`, discards alpha==0.

`ColorModulator` is a `vec4` inside the `DynamicTransforms` UBO. Anything drawn
through these pipelines gets multiplied by it — so it must be white.

### 7.4 `rendertype_text` vs `gui_text`

- `rendertype_text.fsh`: `texture(Sampler0,uv) * vertexColor * ColorModulator`,
  alpha<0.1 discard, then `apply_fog(...)`.
- `rendertype_text.vsh`: also `texelFetch(Sampler2, UV2/16, 0)` — lightmap multiply.
- `RENDERTYPE_TEXT` pipeline: LEQUAL depth (in-world text).
- `GUI_TEXT` pipeline: same shaders, **NO_DEPTH_TEST** (screen text). ← use this.

### 7.5 Custom mod shaders

- `rect`: POSITION_TEXTURE_COLOR in code; fsh `vertexColor * color`.
- `rounded_rect`: SDF rounded rect (`sdRoundRect`), `discard` outside; `uv` is
  pixel-space, `Size` uniform = quad size.
- `rounded_rect_outline`: SDF + outline band via `smoothstep(-outlineWidth,0,d)`.
- `soft_rounded_rect` / `soft_rounded_rect_outline`: SDF + blur band; quad is
  expanded by `blurWidth` on all sides before submission.
- `rgb_palette`: HSV wheel from `atan`/`length`, circular outline.

---

## 8. `Drawable` / `DrawableElement` coordinate model (1.21.11)

Class: `client/gui/drawable/element/Drawable.java` (+ `DrawableElement`,
`StatefulDrawableElement`).

- `relX, relY` — anchor as fraction of parent (screen) size.
- `pivotMode` (`PivotMode`: LEFT_UP … CENTER, with `xFactor/yFactor`) — which point
  of the element sits on the anchor.
- `width, height` — base (unscaled) size, set per-frame by each element.
- `scale` — animated scale (`AnimationService.Value`, 0..1); `screenScale` — global
  GUI scale factor.
- `getScaledWidth() = width * scale * screenScale`.
- `getAnchorX(parentW) = parentW * relX`.
- `getPivotOffsetX() = pivotMode.getXFactor() * getScaledWidth()`.
- `getAbsoluteX(parentW) = getAnchorX(parentW) - getPivotOffsetX()` (top-left).
- `screenToLocal(...)` inverts anchor+scale for hit-testing.

`DrawableElement.updateRender` builds the matrix as described in §2 step 4, so the
element's `render(ctx, z)` works in its own local space (origin at top-left of the
unscaled element, pivot offset applied).

`StatefulDrawableElement.updateRenderForScreen` computes:

```java
screenScale = min(sw/1280, sh/720) * settings.hudScale * guiConfig.hudScale(id);
```

and passes the current `RenderState` (from `RenderStateProvider`, mode LIVE/CONFIG)
into `render(ctx, z, state)`; `positioned` flag defers `initPosition(ctx)` to first frame.

---

## 9. Rendering-related mixins (1.21.11)

| Mixin | Injection | Effect |
|---|---|---|
| `GameRendererMixin` | `GameRenderer.render` at `INVOKE GuiRenderer.render(...)` **AFTER** | posts `RenderEvent(drawContext, mouseX, mouseY, tickProgress)` — the only widget render entry point, on top of all vanilla GUI |
| `InGameHudMixin` | `InGameHud.renderCrosshair` HEAD (cancellable) | hides crosshair when `MainGuiScreen` is open |

---

## 10. `GuiManagerModule` — drag & the off-screen safeguard (task #4)

`client/modules/impl/GuiManagerModule.java` handles HUD dragging on the `RenderEvent`:

1. On LMB press, hit-test modules in **reverse** priority order (top-most first)
   via `isGlobalMouseOver`.
2. While dragging, sets `targetAnchor = mouse - dragOffset`, then **clamps**:

```java
float clampAnchor(float anchor, float screenSize, float scaledSize, float pivotFactor) {
    float pivotOffset = scaledSize * pivotFactor;
    float minAnchor = DRAG_MARGIN + pivotOffset;
    float maxAnchor = screenSize - DRAG_MARGIN - scaledSize + pivotOffset;
    if (maxAnchor < minAnchor) return screenSize / 2f;   // element bigger than screen
    return Math.max(minAnchor, Math.min(maxAnchor, anchor));
}
```

`DRAG_MARGIN = 4f`. The clamp uses `elem.getScaledWidth()/getScaledHeight()` and
`pivotMode` factors so at least the margin + pivot edge always stays reachable on
screen. Applied on both 1.21.11 and 1.20.1 (same logic; 1.20.1 `Drawable` exposes
the same getters).

Notifications are special-cased: instead of clamping they snap to the nearest
screen corner on mouse release (`snapNotificationToCorner`), and are excluded from
normal dragging.

---

## 11. 1.20.1 specifics (legacy, after unification)

1.20.1 was migrated to the same painter's-algorithm concept as 1.21.11. Current
state:

- **Single render entry point**: `GameRendererMixin.onRender` injects at
  `INVOKE DrawContext.draw()V` with `shift = AFTER` — i.e. after the vanilla GUI
  (hotbar/chat/screen) is flushed. No `isScreenRendering` flag, no `beforeScreen`,
  no `ScreenMixin` (removed). Posts `RenderEvent(drawContext, mouseX, mouseY,
  tickDelta)` with real mouse coords computed like vanilla:
  `(int)(mouse.getX() * scaledWidth / width)`.
- **No depth test**: `setupRender()`/`endRender()` no longer enable/disable
  depth; layering = submission order (ascending `getRenderPriority()`).
- **`renderImage` is immediate**: rebuilt as a `Tessellator` quad
  (`POSITION_TEXTURE_COLOR`, uv 0..1, `GameRenderer.getPositionTexColorProgram`).
  The old `ctx.drawTexture` was deferred — flushed inside `DrawContext.draw()`
  BEFORE the mod's post, so images would have been lost/under vanilla after the
  unification.
- Shaders: still `ShaderProgram` + JSON (`assets/minecraft/shaders/core/*.json`),
  immediate `Tessellator.draw()` for quads; text via
  `tr.draw(...) + textBuffers.draw()` (`getEntityVertexConsumers()`).

Frame order that matters (from 1.20.1 `GameRenderer.render` bytecode):
`InGameHud.render` (610) → `Screen.renderWithTooltip` (747) →
`Screen.updateNarrator` (851) → `Profiler.push("toasts")` (914) →
`ToastManager.draw` (928) → `DrawContext.draw()` (945, **vanilla GUI flush**) →
**mod post (after 945)** → `MatrixStack.pop()` (948).

---

## 12. Pitfalls checklist (1.21.11)

- [ ] Custom pipelines: always `.withBlend(TRANSLUCENT)` + `.withDepthTestFunction(NO_DEPTH_TEST)` + `.withDepthWrite(false)`.
- [ ] Never mix deferred (`ctx.drawText/drawTexture`) with immediate passes in one frame — deferred always wins (drawn last by vanilla `GuiRenderer`).
- [ ] Use `built.getDrawParameters().indexCount()` for `getIndexBuffer`/`drawIndexed`.
- [ ] Use `endNullable()` for possibly-empty buffers (text).
- [ ] Text matrix: `new Matrix4f().mul(ctx.getMatrices())`.
- [ ] Text pass: bind `Sampler0` (font, NEAREST) + `Sampler2` (lightmap, LINEAR) + `Fog` (if non-null).
- [ ] Keep `guiColorModulator` white; `setupRender()` resets it.
- [ ] GUI projection: near 1000 / far 11000 / invertY; DynamicTransforms z = -11000.
- [ ] `DrawContext.getMatrices()` is `Matrix3x2fStack` — transform positions manually (`m00*x+m10*y+m20`), don't pass it as a `Matrix4f`.
- [ ] Layer order = ascending `getRenderPriority()` = bottom→top. Don't re-introduce depth z.
- [ ] Off-screen drags: clamp anchors with pivot-aware `clampAnchor` (4px margin).
- [ ] The leftover `shaders/core/*.json` are dead files for 1.21.11 (1.20.1 format); new pipelines read only `.vsh/.fsh` + `#moj_import`.
