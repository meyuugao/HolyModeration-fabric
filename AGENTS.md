# AGENTS.md — Universal Delegation Rules

Purpose: a strict multi-agent pipeline with maximum token economy. Stack-agnostic. Operates over the opencode config (roles: pm, build, plan, coder, explorer, reviewer) backed by DeepSeek V4 (Pro + Flash) via the Anthropic-compatible endpoint.

Language note: English is intentional. Russian tokenizes ~25-30% worse; an always-loaded file in English is the single highest-ROI economy decision.

DeepSeek economics: output costs 3x input (Pro $1.98/$0.66, Flash $0.66/$0.22 per 1M off-peak). Context caching is automatic (disk cache, ~97% discount on cache-hit: Flash $0.007/M, Pro $0.022/M). Peak hours 01:00-04:00 and 06:00-10:00 UTC — schedule batch work outside these for 50% off. Pro concurrency 500, Flash 2500.

Maintenance rule: humans edit this file. Agents MUST NOT write, regenerate, or auto-append to AGENTS.md. Every line must trace to a real incident. No speculative rules. Audit monthly; delete stale rules.

---

## 0. NEVER (critical — read first)

- NEVER let an agent (build/coder/explorer) edit this file. Lead approves every line. LLM-generated context files measurably reduce success rate.
- NEVER re-read a file already summarized in-session. Cite the summary.
- NEVER dump full codebase context into a subagent. Pass only the relevant slice.
- NEVER accept coder output without a reviewer pass.
- NEVER raise `subagent_depth` above 1 (opencode native key, default 1 already forbids nested subagents). Recursive fan-out is the #1 token blow-up.
- NEVER run more than 3-5 active subagents. Add one only if you can review its output.
- NEVER write "clean up this code" — it strips WHY comments. Use "refactor X, preserve comments that explain intent".
- NEVER ship conflicting rules. Resolve before commit.
- NEVER include philosophy, CONTRIBUTING.md restatement, install guides, or roadmap.

---

## 1. Topology

| Role    | Model | Mode     | Write | Shell | Purpose                              |
|---------|-------|----------|-------|-------|--------------------------------------|
| pm      | pro   | primary  | deny  | deny  | Intake, clarifying questions, spec   |
| build   | pro   | primary  | deny  | ask   | Orchestrator: plan, route, gate      |
| plan    | pro   | primary  | ask   | ask   | Analysis, proposals, no edits        |
| coder   | flash | subagent | allow | allow | Mechanical edits per discrete step   |
| explorer| flash | subagent | deny  | deny  | Read-only search, signatures, paths  |
| reviewer| pro   | subagent | deny  | allow | Verify before commit; PASS or FIX    |

Target: 80-90% of tokens on flash. Pro only for judgment, ambiguity, decomposition, review. Rule: if a junior could do it from clear instructions -> flash; if it needs architectural choice -> pro.

---

## 2. Delegation protocol

Flow: pm -> build -> explorer -> build (plan) -> coder -> reviewer -> build (integrate).

- build never reads source directly. Calls explorer.
- explorer returns dense summary: class names, method signatures, imports, paths. One real fragment beats three paragraphs. Never the whole file.
- build composes a step-by-step plan, delegates one step per coder call.
- coder writes heavy output (code, diffs, test logs) to a report file at `.agent/task-N.md`. Returns to build ONLY a pointer: `result in .agent/task-N.md` (~50 tokens). Never raw code in the message.
- reviewer reads the report file in its OWN isolated context. build passes the path, not the content.
- build sees only pointers and verdicts. Its context stays clean across the whole task.

Handoff files are append-only. Dependent agents read the report, not the work that produced it. One file, one owner at a time.

---

## 3. Role contracts

build (orchestrator):
- Accept spec -> call explorer -> plan -> delegate coder per step -> call reviewer -> integrate.
- Trust explorer output. Do not re-read to verify.
- /clear context between independent tasks (30-50% per-turn saving).
- Kill idle pro sessions.
- On bad subagent output: (1) retry with sharper instructions on same flash; (2) escalate that one task to pro; (3) inline fix if trivial.

explorer (read-only):
- Tools: read, glob, grep, read-only shell. No write, no mutation.
- If file already in session: answer "already in context above" + cite fragment. Do not re-read.
- Cold start for local tasks: skip AGENTS.md and git status load.
- Parallel-safe (no side effects).

coder (executor):
- Receives one concrete step: "In file X replace method Y with Z".
- If method code was passed in the task, do NOT re-read the file. Edit directly on the passed text.
- No global architecture thinking. Follow the step + stack rules.
- Write output to `.agent/task-N.md`. Return pointer only.
- After edit, run check (test/lint) if logical. Write result to same file.

reviewer (verifier):
- Reads report file in isolated context. build passes path only.
- Checks: bugs, security, conventions, regressions, side effects.
- Tools: read, shell (tests). No write.
- Verdict format: `PASS` or `FIX: [file:line] issue`. No prose, no code quotes.
- Mandatory before commit. Flash errs more; reviewer catches before cascade.

---

## 4. Output economy (output costs 3x input on DeepSeek V4)

- Subagents return ONLY final output. opencode isolates their context; parent sees just the final message, never intermediate tool calls.
- Mandate structured returns: `PASS|FIX`, severity tables, line refs. Never novels.
- No chain-of-thought in routine subagent work. CoT burns output tokens; reserve for genuinely hard pro calls.
- Diffs and line-number refs over full-file rewrites.
- Lean tool descriptions. Verbose definitions tax every call.
- `steps` cap per agent (opencode native key): coder 20, explorer 15, reviewer 25. On limit hit opencode removes tools and forces a summary — stops test spirals natively.
- Format instructions live in the system prompt, not the user message: "Respond with a single word", "Return only the JSON object".

---

## 5. Context and cache (DeepSeek automatic disk cache)

- Chain without re-reads: read once -> pass up -> build passes down -> coder edits.
- explorer summary is the canonical session memory. Later agents cite it, never re-open the file.
- Keep context append-only. Do not truncate or rewrite earlier tool outputs — that breaks the cache prefix match.
- DeepSeek caching is AUTOMATIC (disk cache, no `cache_control` needed — it is silently ignored on the Anthropic endpoint). Cache-hit costs ~1/30 of cache-miss (97% discount). Persistence happens at request boundaries and on common-prefix detection.
- Stable prefix maximizes hit rate: this file + system prompt + tool defs at TOP; per-task specifics at BOTTOM near the user turn.
- Never inject dynamic data (timestamps, user names, session IDs) into the system prompt or this file — one char breaks the prefix match.
- /clear between independent tasks. Stale history resends every turn.

---

## 6. Escalation and kill criteria

Escalate one call flash -> pro when:
- architectural judgment or ambiguity is required;
- coder output failed review;
- two flash attempts failed.
  Do not upgrade the whole agent. Escalate the single task.

Kill / reassign when:
- subagent stuck 3+ iterations on same error;
- subagent hits its `steps` cap;
- review queue exceeds your review bandwidth (unreviewed work piling up is slower than fewer agents).

Reviewer ratio: 1 pro reviewer per 3-4 flash builders.

---

## 7. Anti-patterns (forbidden)

- build writes code itself when a subagent is available.
- Full codebase dump into any flash subagent.
- Accepting coder output without reviewer.
- Re-reading a file explorer already summarized.
- /init-generated context committed without human audit.
- Duplicating stack rules across CLAUDE.md / .cursorrules / copilot-instructions.md. Symlink (CLAUDE.md -> AGENTS.md) to prevent drift.
- Stale structural references after refactors.
- Emojis, decorative formatting, prose philosophy.
- Speculative "just in case" rules.
- Recursive subagent spawning (`subagent_depth` > 1).
- Vague rules ("write clean code") — ignored regardless of length; use specific contrarian rules ("use early returns, not nested if").
- Conflicting rules — cause silent stalls.

---

## 8. Checklist before adding a rule

1. Traces to a real incident? No -> do not add.
2. Agent can infer it from code/docs? Yes -> do not add.
3. Universal (needed every conversation)? No -> move to skill or references/.
4. Conflicts with an existing rule? Yes -> merge, do not patch.
5. File still under ~100 lines? No -> split into nested AGENTS.md per package.
6. Critical rule placed in section 0 (NEVER)? No -> move up.
7. Written by a human, not an agent? No -> reject.

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
