# ТЗ: портирование HolyModeration с 1.20.1 на 1.21.11

## Цель
Заставить код (сейчас идентичный ветке `1.20.1`) компилироваться и работать на Minecraft 1.21.11, сохранив всю архитектуру и логику. Менять только то, что требуется для совместимости с новым API.

## Контекст
- Ветка `1.21.11` уже содержит build-конфиг под 1.21.11 (`gradle.properties`, `build.gradle`, `gradle-wrapper.properties`, `holymoderation.mixins.json` → `JAVA_21`). **Java-код не менялся и написан под 1.20.1.**
- `git diff 1.20.1 1.21.11 --stat` показывает различия только в 4 конфиг-файлах — исходники идентичны.
- Нужно пройтись по `src/main/` и поправить всё, что не компилируется/не работает под 1.21.11.

## Порядок работы
1. `gradlew.bat genSources` — получить декомпилированные исходники MC 1.21.11.
2. `gradlew.bat compileJava` — увидеть список ошибок компиляции.
3. Для каждого изменённого API сверяться с декомпилированным исходником (`%USERPROFILE%\.gradle\caches\fabric-loom\1.21.11\...\*-sources.jar`) или доками Yarn (`https://maven.fabricmc.net/docs/yarn-1.21.11+build.6/`).
4. Править минимально, не меняя логику.
5. Финал: `gradlew.bat build` проходит без ошибок.

## Что обязательно перепроверить (1.20.1 → 1.21.11)

### 1. Миксины (`client/mixin/`)
Сверить каждый `@Inject`/`@Shadow` с декомпилированным классом:
- `MinecraftClientMixin`: `disconnect(Screen)`, `onInitFinished` (мог быть переименован), `getResourceManager()`.
- `GameRendererMixin`: `render(...)` — в 1.21.2+ сигнатура изменилась (tickDelta → `RenderTickCounter`, параметр `tick` удалён). Таргеты `Profiler.push` (ordinal 1) и `Screen.renderWithTooltip`. Поле `client.world`. `@Local DrawContext`.
- `ScreenMixin`: `Screen.render(...)` — сигнатура могла измениться (возможно `RenderTickCounter`). `@Shadow protected MinecraftClient client`.
- `MessageHandlerMixin`: `onGameMessage(...)` — в 1.21.2+ добавлен параметр `MessageType`, сигнатура изменилась.
- `ClientPlayNetworkHandlerMixin`: `onGameJoin`, `sendChatMessage(String)`, `sendChatCommand(String)`, `getServerInfo()` — проверить существование/сигнатуры.
- `MouseMixin`: `onMouseButton(long,int,int,int)`, `onMouseScroll(long,double,double)`, `getX()/getY()`.
- `KeyBoardMixin`: `onKey(long,int,int,int,int)`.
- `ScreenEventsMixin`: таргет — класс Fabric API `ScreenEvents` (не MC). Проверить, что `beforeRender(Screen)`/`afterRender(Screen)` и `ScreenEvents.BeforeRender`/`AfterRender` в fabric-api 0.141.6 имеют сигнатуру `(Screen, DrawContext, int, int, float)`. Если нет — переписать.

### 2. Рендер (`util/service/Render2DService.java`) — самый большой блок
1.21.2+ радикально переработал рендер:
- `new ShaderProgram(ResourceManager, String, VertexFormat)` — старый конструктор удалён; шейдеры грузятся через `ShaderProgramKeys`/`ShaderLoader`. Разобраться с новым способом загрузки кастомных шейдеров.
- `RenderSystem.setShader(() -> shader)` → новый API (`RenderSystem.setShader(RegistryKey<ShaderProgram>)` и т.п.).
- `RenderSystem.setShader(GameRenderer::getPositionTexColorProgram)` → заменить на ключ программы.
- `Tessellator.getInstance().getBuffer()` / `BufferBuilder` + `b.begin(VertexFormat.DrawMode, VertexFormats...)`, `b.vertex(...).color(...).texture(...).next()`, `t.draw()` — API изменился в 1.21.2+.
- `VertexFormats.POSITION_COLOR`, `POSITION_COLOR_TEXTURE`.
- `MatrixStack` / `matrices.peek().getPositionMatrix()`.
- `TextRenderer.draw(String/OrderedText, x, y, color, shadow, matrix, vertexConsumers, TextLayerType, backgroundColor, light)` — сигнатура в 1.21.x изменилась.
- `TextRenderer.TextLayerType.NORMAL`.
- `getClient().getBufferBuilders().getEntityVertexConsumers()`.
- `RenderSystem.enableBlend()/defaultBlendFunc()/enableDepthTest()/depthFunc(GL11.GL_LEQUAL)/setShaderColor/disableBlend/disableDepthTest` — часть удалена/переехала в 1.21.2+.
- `GlStateManager._activeTexture(33984)` / `GlStateManager._bindTexture(0)`.
- `ctx.drawTexture(...)` overload — сигнатура изменилась.
- Шейдеры-ресурсы `assets/minecraft/shaders/core/*.json`: формат JSON в 1.21.2+ изменён (поле `program`, `vertex`/`fragment` — ссылки на ключи). Переписать 6 json (и, при необходимости, .vsh/.fsh — uniform'ы `ModelViewMat`/`ProjMat` могли переименоваться). Свериться с ванильными шейдерами в декомпилированных ресурсах 1.21.11.

### 3. Текст/чат (`util/service/ChatService.java`, `MessageModule`, `MessageHandlerMixin`)
- `new HoverEvent(HoverEvent.Action.SHOW_TEXT, text)` — в 1.21.x удалён → `new HoverEvent.ShowText(text)`.
- `ClickEvent.Action.*` — проверить (SUGGEST_COMMAND/COPY_TO_CLIPBOARD/OPEN_URL).
- `player.sendMessage(Text, boolean)` — перегрузка с boolean удалена → `sendMessage(Text)`.
- `inGameHud.getChatHud().addMessage(Text)` — в 1.21.x требует `MessageType`/другую сигнатуру.
- `networkHandler.sendChatMessage(String)` / `sendCommand(String)` — проверить (sendChatMessage мог быть удалён).
- `Text.of(...)` — при необходимости заменить на `Text.literal(...)`.

### 4. GUI / DrawContext
- `DrawContext.getScaledWindowWidth()/getScaledWindowHeight()`.
- `ctx.enableScissor(int,int,int,int)` / `disableScissor()`.
- Override `Screen.render(DrawContext,int,int,float)` в `GuiScreen`/`MainGuiScreen`/`AnimatedGuiScreen` — обновить под новую сигнатуру.
- `ScreenEvents.remove(this)`, `super.close()`, `removed()` в `AnimatedGuiScreen`.
- `mc.execute(() -> mc.setScreen(...))`.
- `mc.getWindow().getScaledWidth()/getScaledHeight()`.

### 5. Прочее
- `ScreenHandlerService`: `ClickSlotC2SPacket` (конструктор мог измениться), `ScreenHandler.onSlotClick(...)`, `SlotActionType`, `handler.getRevision()`, `handler.syncId`.
- `StateModule`: `net.minecraft.world.GameMode` (мог переехать), `ClientPlayerInteractionManager.getCurrentGameMode()`, `clientWorld.getRegistryKey().getValue()`.
- `ImageButtonDrawableElement`: `NativeImage`/`NativeImage.read()` (deprecated/удалён в 1.21.x), `getResourceManager().getResource(Identifier)`.
- `ReportsParserDrawableElement`: `Identifier.of(...)`, `player.currentScreenHandler`, `slot.getStack()`, `stack.getName()`.
- `Argument.player`: `client.getNetworkHandler().getPlayerList()`.
- `AnimationService`: `mc.getLastFrameDuration()`.
- `MinecraftService`: `MinecraftClient.getInstance().player/.world` — без изменений.

## Чего НЕ делать
- Не менять архитектуру (DI / EventBus / модули / конфиги / структуру HUD).
- Не менять бизнес-логику и русские строки.
- Не трогать `archive/`, `backend/`, `lib/` — они вне сборки.
- Не добавлять комментарии в код.

## Definition of done
`gradlew.bat build` проходит без ошибок; `remapJar` собирается. Мод запускается (`gradlew.bat runClient`), HUD/уведомления/команды работают.
