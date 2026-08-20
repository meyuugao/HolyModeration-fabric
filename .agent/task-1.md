# Task 1 — GUI fixes (branch 1.21.11)

Three fixes, verified with `gradlew compileJava --rerun-tasks` (BUILD SUCCESSFUL).

## 1. Scissor crop of tab content (right side cut off)

Root cause: `SettingsTab.onRender` pushed a scissor with raw local coordinates
`(0, contentStartY-8, pW, bottom)` while the matrix stack had the panel
translate+scale applied. `Render2DService.submitPass` interprets pushed
scissor as scaled-window coordinates, so the crop rectangle landed at the
window's top-left instead of the panel.

Fix: transform the scissor rect through `ctx.getMatrices()` before pushing,
matching the pattern already used by `TwinksTab.pushScissor`,
`DropdownDrawableElement.renderOptions`, `SearchDrawableElement.render`.

Files:
- `src/main/java/me/yuugao/holymoderation/client/gui/tabs/SettingsTab.java`
  - added `org.joml.Matrix3x2fStack` import
  - added `pushScissor(ctx, ...)` + `transformPoint(ms, ...)` helpers
  - `onRender` now calls `pushScissor(ctx, 0f, contentStartY() - 8f, pW, bottom)`

## 2. Mouse wheel scaled widget while also scrolling the tab

Root cause: `MouseMixin.onMouseScroll` fired the EventBus and then let the
vanilla `Mouse.onMouseScroll` continue, which called
`GuiScreen.mouseScrolled` -> `SettingsTab.onMouseScroll` (tab scroll). Both
paths ran independently.

Fix: widget scaling now consumes the event. `scaleHoveredElement` returns
whether a hovered (topmost-by-z) widget was found; when true, the event is
cancelled and the mixin cancels the vanilla scroll so the tab does not scroll.
Wheel over a widget = scale widget only; wheel elsewhere = scroll tab.

Files:
- `src/main/java/me/yuugao/holymoderation/client/util/service/InputService.java`
  - `updateScroll` returns `boolean` (event.isCancelled())
- `src/main/java/me/yuugao/holymoderation/client/mixin/MouseMixin.java`
  - `onMouseScroll` inject is now `cancellable = true`; cancels `ci` when consumed
- `src/main/java/me/yuugao/holymoderation/client/modules/impl/GuiManagerModule.java`
  - `onMouseScroll` sets `event.setCancelled(true)` when a widget is scaled
  - `scaleHoveredElement` now returns `boolean`

## 3. Scale badge "1.00x" did not resize with screen resolution

Root cause: badge width/height/text were fixed in scaled-window pixels.

Fix: badge geometry now multiplies by `resScale = min(sw/1280, sh/720)` and
renders inside a pushed translate+scale matrix, matching the rest of the GUI.

Files:
- `src/main/java/me/yuugao/holymoderation/client/modules/impl/GuiManagerModule.java`
  - `renderScaleBadge` computes `resScale`, scales `w/h/gap`, renders rect+text
    inside `ms.pushMatrix()/translate/scale/popMatrix()`
