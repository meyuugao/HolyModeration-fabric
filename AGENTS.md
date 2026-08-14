## 🛑 ЖЁСТКИЕ ПРАВИЛА ДЕЛЕГИРОВАНИЯ (КРИТИЧЕСКИ ВАЖНО)

В этом проекте работает конвейер. Если ты агент `build` (Архитектор), **ты не трогаешь код руками**.

### Роль агента `build` (Оркестратор)
1. **Тебе ЗАПРЕЩЕНО** использовать инструменты `read`, `glob`, `grep` для чтения исходного кода. 
2. **Тебе ЗАПРЕЩЕНО** использовать инструменты `edit` или `write` для изменения кода.
3. Если тебе нужно узнать, что находится в файле, как устроена архитектура или найти класс — **ВЫЗЫВАЙ `@explorer`**.
4. Если тебе нужно изменить код, создать файл или написать тест — **ВЫЗЫВАЙ `@coder`**.
5. Когда `@explorer` возвращает тебе ответ, **ДОВЕРЯЙ ЕМУ**. Не пытайся перечитать файл самостоятельно, чтобы "проверить". Это убивает контекст.
6. Твоя задача: принять ТЗ -> вызвать `@explorer` для анализа -> составить пошаговый план -> вызывать `@coder` для каждого шага -> вызвать `@reviewer` для проверки.

### Роль агента `@explorer` (Исследователь - Flash)
1. Ты работаешь только в режиме чтения (read-only).
2. Получив запрос от `build`, быстро найди нужные файлы, прочитай их и верни **краткую выжимку** (названия классов, сигнатуры методов, импорты).
3. Не возвращай весь текст файла, если просят только сигнатуру метода. Это экономит контекст.

### Роль агента `@coder` (Писатель - Flash)
1. Ты получаешь конкретную задачу: "В файле X замени метод Y на Z" или "напиши метод X", "дополни метод X".
2. Ты не обязан думать о глобальной архитектуре, просто сделай, что просят, используя правила стека.
3. После изменения кода, если это логично, запусти `gradlew.bat compileJava` для проверки.

---

## Правила кода (Для агента @coder)
- Не добавлять комментарии в код (если явно не попрошено).
- Соблюдай существующую архитектуру, стиль кода и стек проекта.

# HolyModeration (Fabric)

Client-only Fabric-мод для модерации на сервере HolyWorld. Среда — только клиент (`"environment": "client"`).

## Стек

- Minecraft **1.21.11**, Yarn mappings `1.21.11+build.6` (v2)
- Fabric Loader `0.19.3`, Fabric API `0.141.6+1.21.11`
- Fabric Loom `1.17-SNAPSHOT`, Gradle wrapper `9.6.0`
- Java **21** (toolchain 21)
- Lombok `1.18.42` (compileOnly + annotationProcessor)
- oshi-core `6.4.0` (HWID)
- MixinExtras (идёт в составе Fabric Loader) — используется `@Local` в миксинах
- Остальное — bundled-библиотеки MC: guava, gson, log4j2, commons-lang3, fastutil

## Сборка и проверка (Windows)

- `gradlew.bat build` — полная сборка (compileJava + remapJar)
- `gradlew.bat compileJava` — быстрая проверка компиляции
- `gradlew.bat genSources` — декомпиляция MC и генерация исходников с именами Yarn (для чтения API)
- `gradlew.bat runClient` — запуск в dev-окружении

Декомпилированные исходники лежат в `%USERPROFILE%\.gradle\caches\fabric-loom\<mc-version>\...` (jar'ы с суффиксом `-sources.jar`). API конкретного класса смотреть там либо в IntelliJ после `genSources`. Онлайн-доки Yarn: `https://maven.fabricmc.net/docs/yarn-1.21.11+build.6/`.

## Структура

```
src/main/java/me/yuugao/holymoderation/
  HolyModeration.java           — main entrypoint (ModInitializer, пустой)
  client/
    HolyModerationClient.java   — client entrypoint: сборка DI, EventBus, модули, команды
    di/                         — свой DI-контейнер (@Inject/@Singleton/@PostConstruct; DIContainer/DIRegistry; модули в di/module/impl)
    mixin/                      — 8 миксинов (см. holymoderation.mixins.json)
    modules/                    — DrawableModule + impl/*
    util/
      command/                  — обёртка над Brigadier (Fabric client command v2)
      service/                  — сервисы (DI @Singleton)
      service/eventbus/         — свой EventBus (@Subscribe + приоритеты)
      service/config/           — JSON-конфиги (Config, ConfigManagerService, impl/*)
      service/state/            — состояния (UserState/PlayerState/ModState)
      handler/                  — GlobalExceptionHandler
      factory/                  — DrawableElementFactory
    gui/
      screen/                   — GuiScreen / AnimatedGuiScreen / MainGuiScreen
      tabs/                     — вкладки GUI
      drawable/                 — HUD: Drawable / DrawableElement / StatefulDrawableElement, кнопки, singleton-элементы, RenderState + provider
src/main/resources/
  fabric.mod.json
  holymoderation.mixins.json
  assets/minecraft/shaders/core/*.json + *.vsh + *.fsh  — кастомные шейдеры (rect, rounded_rect, soft_rounded_rect, *_outline, rgb_palette)
  assets/minecraft/textures/gui/clear.png
  assets/minecraft/icon.png
```

## Архитектура

1. **DI**: `HolyModerationClient.onInitializeClient()` строит `DIContainer` через `DIRegistry` из модулей `ContainerModule`, `ServicesModule`, `ModulesModule`, `DrawableElementsModule`, `FactoriesModule`, `HandlerModule`. Сервисы помечаются `@Singleton`; инъекции — через `@Inject` на конструкторе (`@RequiredArgsConstructor(onConstructor_ = @Inject)`) или на поле; `@PostConstruct` вызывается после создания.
2. **EventBus**: свой (`util/service/eventbus/EventBus`). Обработчики — `@Subscribe(priority=N)`; чем больше priority, тем раньше вызывается. События — наследники `Event` (флаг `cancelled`). Доступ через `EventBusService`.
3. **Модули**: `ModuleManagerService.registerAll()` регистрирует модули в EventBus; модули реализуют `CommandProvider` (команды) и/или наследуют `DrawableModule` (HUD).
4. **Команды**: `CommandRegistry` оборачивает Brigadier (Fabric client command v2). Root-команда `hm` регистрируется в `HolyModerationClient.commandsInitialize()`. Спек — `CommandSpec`, аргументы — `Argument`.
5. **Конфиги**: `ConfigManagerService` хранит JSON в `~/HolyModeration/Config/*.json` (Gson). Классы конфигов наследуют `Config`, поля — `@Expose`.
6. **Рендер**: `Render2DService` — кастомные шейдеры + `DrawContext`/`MatrixStack`/`TextRenderer`. HUD-элементы — `StatefulDrawableElement` с `RenderState`/`RenderStateProvider` (режимы `LIVE`/`CONFIG`).

## Правила кода

- **Не добавлять комментарии в код** (если явно не попрошено).
- Сохранять существующую архитектуру и логику — не рефакторить при портировании.
- Строки, видимые пользователю, — на русском, через `.formatted(...)` / `String.format`.
- Логирование через `LoggerService` (`info`/`debug`/`exception`), префиксы `[HM INFO]` / `[HM DEBUG]` / `[HM EXCEPTION]`.
- Новые сервисы/модули обязаны быть зарегистрированы в соответствующем `DIModule` / `ModuleManagerService`.
- Lombok — как в существующем коде (`@Getter`, `@Setter`, `@RequiredArgsConstructor(onConstructor_ = @Inject)`).
- Не менять `gradle.properties`, `build.gradle`, `fabric.mod.json`, `holymoderation.mixins.json` без необходимости.
