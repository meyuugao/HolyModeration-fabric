# AGENTS.md — Universal Delegation Rules

Stack-agnostic. Operates over the opencode config (roles: build, plan, reviewer) backed by DeepSeek V4 Pro via the native DeepSeek API endpoint.

Language: English. Russian tokenizes ~25-30% worse.

DeepSeek cache: automatic disk cache on the messages[] prefix. Cache-hit billed ~1/30 of cache-miss. A full byte-for-byte prefix match from index 0 is required. Any change to system prompt, tool list, provider, or a mid-sequence insertion breaks the entire prefix cache.

Maintenance: humans edit this file. Agents MUST NOT write, regenerate, or auto-append to AGENTS.md.

---

## 0. NEVER

- NEVER edit this file. Lead approves every line.
- NEVER re-read a file already in session context. Cite it from memory.
- NEVER switch to a different agent mid-task. Each agent has its own system prompt -> cache miss on the whole prefix.
- NEVER call a subagent for work you can do yourself. Every subagent invocation is a separate cache.
- NEVER inject dynamic data (timestamps, paths, session IDs) into prompts or rules files.
- NEVER write "clean up this code" — it strips WHY comments. Use "refactor X, preserve comments that explain intent".
- NEVER dump full codebase context into a subagent. Pass only the relevant slice.
- NEVER accept build output as final when a commit is intended — reviewer must pass first.
- NEVER raise `subagent_depth` above 1.
- NEVER retry the same approach expecting different results.
- NEVER retry a shell command that failed with a file-lock error (AccessDeniedException, LockedException, "being used by another process", EBUSY, EPERM on write). The lock is held by another process — retrying the same command will hang or fail identically.
- NEVER pipe long-running build output through a buffer-stage command (`Select-Object`, `head`, `tail`, `more`) when wrapping a shell call. These buffer the entire stream in memory and cause pipe deadlocks when the underlying process holds a handle after exit.

---

## 1. Topology

| Role    | Mode     | Write | Shell | Subagent | Purpose                                          |
|---------|----------|-------|-------|----------|--------------------------------------------------|
| build   | primary  | allow | ask   | allow    | Orchestrator + executor: plan, read, edit, verify |
| plan    | primary  | deny  | deny  | deny     | Read-only analysis, architecture, tradeoffs       |
| reviewer| subagent | deny  | allow | deny     | Pre-commit verification; PASS or FIX              |

All three run on DeepSeek V4 Pro. build does the work itself. plan and reviewer are narrow specialists invoked only when their isolation is genuinely useful — every agent switch costs a full cache miss.

---

## 2. Delegation protocol

Default flow: build does everything — reads, plans, edits, runs checks. One agent, one session, one stable cache prefix.

- build reads files directly. Do not delegate reading.
- build edits files directly. Do not delegate edits.
- build calls reviewer ONLY before a commit, passing the path `.agent/task-N.md` — never the content.
- For a multi-step refactor: write a plan to `.agent/plan-N.md` first, then execute step by step. This externalizes reasoning so reviewer has a reference.
- For a single-file fix: edit directly. No plan file, no reviewer.
- Do NOT switch to plan mid-task. If architectural judgment is needed, finish the current step first or start a fresh build session.
- reviewer runs in its own isolated context. It sees only the file you point it to.

---

## 3. Role contracts

build (orchestrator + executor):
- Accept task -> plan -> read -> edit -> verify -> optionally call reviewer.
- Do not re-read files already in context.
- Preserve comments that explain intent. Delete only what the task requires.
- Prefer diffs and line refs over full-file rewrites in any output you return.
- When calling reviewer: pass ONLY the path, never the content.

plan (read-only investigator):
- Use only when the user explicitly asks for analysis or design without edits.
- Cannot edit, cannot run shell, cannot delegate.
- Return: proposal with tradeoffs, file refs, no code changes.

reviewer (verifier):
- Read `.agent/task-N.md` in your isolated context. build passes the path.
- Check: bugs, security, conventions, regressions, side effects.
- Tools: read, shell (tests). No write.
- Verdict: `PASS` or `FIX: [file:line] issue`. No prose, no code quotes.

---

## 4. Output economy

Output costs 2-3x input on DeepSeek V4. Minimize output.

- Return only the final result. No chain-of-thought in routine work.
- Structured returns: `PASS|FIX`, severity tables, line refs. Never novels.
- Diffs and line-number refs over full-file rewrites.
- Format instructions belong in the user message at the END, not the system prompt: "Respond with PASS or FIX only", "Return only the JSON object".

---

## 5. Cache preservation

DeepSeek caches the messages[] prefix on disk. Cache-hit costs ~1/30 of cache-miss. The prefix must match byte-for-byte from index 0.

What breaks the cache — avoid:
- Changing system prompt or rules files mid-task.
- Switching agents mid-task (different system prompt).
- Switching providers mid-session.
- Inserting messages in the middle of the sequence.
- Editing AGENTS.md or rules files during a task.

What does NOT break the cache:
- Adding new messages at the END.
- Time passing (cache lives hours-days while in use).

Workflow:
- Within a task: do NOT clear context. The accumulated prefix IS the cache.
- Between tasks: clear context to drop stale history.
- Stay on one agent per task.
- Do not call reviewer unless a commit is intended.

---

## 6. Shell and build commands

Long-running build tools (gradle, maven, npm, cargo, dotnet, go build, decompilers, codegen) produce large output and hold file handles. Shell wrappers can deadlock on this. Follow these rules.

### Running build commands

- Run build commands directly through the native shell executable, not through an intermediate pipe that buffers.
    - Windows: prefer `cmd /c <command>` over `powershell -Command "<command> | Select-Object ..."`. PowerShell's `Select-Object -Last N` buffers the entire stream in memory before emitting, which deadlocks when the underlying process holds a stdout handle after exit.
    - Unix: avoid `cmd | tail -n 30` for processes that may leave a child holding the pipe. Stream directly and let opencode truncate.
- If the wrapper must truncate output, prefer a wrapper that streams and truncates on the fly (e.g. `cmd /c <command> 2>&1`) over one that buffers and slices.
- Set a per-command timeout expectation mentally. If a build step takes longer than reasonable for its type (gradle genSources > 60s with no output, npm install > 120s with no output), treat it as hung — see "On a hung command" below.

### Recognizing file-lock failures

The following patterns in command output mean a file is locked by another process. They are NOT transient. Retrying the same command will fail identically or hang.

- `AccessDeniedException` (Java/gradle)
- `LockedException`, `OverlappingFileLockException`
- `being used by another process` (Windows)
- `Resource temporarily unavailable`, `EBUSY`, `EACCES` on write (Unix)
- `Failed to query processes holding a lock`
- `RmRegisterResources failed` (Windows resource manager, usually on `.gradle` caches)
- `Cannot delete ... because it is being used`

When you see any of these: STOP immediately. Do not retry. Report to the user with the locked path and the likely holder.

### On a hung command

A command is hung when: no new output for >30s on a build step, or the shell call has not returned after the expected duration.

- Do NOT wait silently. Report to the user: "Command `<cmd>` appears hung. Last output: `<tail>`. Suspected cause: file lock / pipe buffer / child process holding a handle."
- Do NOT auto-retry the same command. The hang is almost always caused by a lock or a pipe deadlock that retry does not fix — it only appears to "fix" because the lock releases during the wait.
- If a previous run was killed (ChildProcess.kill in the log), assume the killed process may have left a lock or zombie. Diagnose before re-running.

### Recovery sequence for a lock failure

When a build command failed with a file-lock error on a cache/build-output directory, run this sequence before retrying the original command — and only if the user has agreed:

1. Identify the locked path from the error message.
2. Identify likely holders: IDE processes (IntelliJ, VSCode), gradle daemons, java child processes, antivirus scanners, file indexers.
3. Kill the holders targeting the lock. On Windows: `taskkill /F /IM java.exe`, `taskkill /F /IM gradle.exe`. On Unix: `pkill -f gradle`, `pkill -f <build-tool>`. Be specific — do not kill unrelated processes.
4. Remove the locked cache directory only (not the whole `.gradle` or build cache — just the subtree named in the error). Example: `rmdir /S /Q .gradle\loom-cache\minecraftMaven\<path-from-error>` on Windows, `rm -rf .gradle/loom-cache/minecraftMaven/<path-from-error>` on Unix.
5. Retry the original command ONCE. If it fails again with the same lock error, stop and tell the user the environment is unstable (likely IDE or antivirus holds the cache).

### General shell discipline

- Do not run more than one build command in parallel against the same project. Gradle/maven/cargo daemons lock shared state.
- Do not run a build command while the IDE is running the same build.
- After a build command fails, capture the full tail of the output (last 30-50 lines) into your reasoning, then report a one-line diagnosis plus the tail. Do not paste 500 lines of build log.
- If a build command succeeds but the shell call reports a kill or timeout, do not assume failure — re-read the captured output for a BUILD SUCCESSFUL / build finished / exit 0 marker before deciding.

---

## 7. Escalation

Escalate to plan only when build genuinely cannot decide an architectural question. Finish the current step first; do not switch mid-task.

Stop and report to the user when:
- Stuck 3+ attempts on the same error.
- Cannot reproduce a bug after 3 attempts.
- A task requires edits beyond build's permissions.
- A shell command fails with a file-lock error (do not retry — see section 6).
- A shell command hangs with no output for >30s on a build step (do not retry silently — see section 6).

Do NOT loop on the same error. If an approach fails twice, change the approach or stop.

---

## 8. Anti-patterns (forbidden)

- Delegating to a subagent for trivial edits.
- Full codebase dump into a subagent.
- Accepting build output for commit without reviewer.
- Re-reading a file already in session.
- Switching agents mid-task.
- Switching providers mid-session.
- Editing AGENTS.md or rules files mid-task.
- Dynamic data in system prompt (timestamps, paths, session IDs).
- Recursive subagent spawning (`subagent_depth` > 1).
- /init-generated context committed without audit.
- Duplicating stack rules across CLAUDE.md / .cursorrules / copilot-instructions.md. Symlink to prevent drift.
- Stale structural references after refactors.
- Emojis, decorative formatting, prose philosophy.
- Speculative "just in case" rules.
- Vague rules ("write clean code") — use specific contrarian rules ("use early returns, not nested if").
- Conflicting rules — cause silent stalls.
- Retrying a shell command that failed with a file-lock error.
- Piping long-running build output through `Select-Object`, `head`, `tail`, `more`, or any buffer-stage command when wrapping a shell call.
- Running a build command in parallel with the IDE running the same build against the same project.
- Waiting silently on a hung shell command (>30s, no output) without reporting to the user.
- Assuming a killed shell call failed — re-read the captured output for a success marker first.

# HolyModeration (Fabric)

Client-only Fabric mod for moderation on the HolyWorld server. Environment — client only (`"environment": "client"`).

## Stack

- Minecraft **1.21.11**, Yarn mappings `1.21.11+build.6` (v2)
- Fabric Loader `0.19.3`, Fabric API `0.141.6+1.21.11`
- Fabric Loom `1.17-SNAPSHOT`, Gradle wrapper `9.6.0`
- Java **21** (toolchain 21)
- Lombok `1.18.42` (compileOnly + annotationProcessor)
- oshi-core `6.4.0` (HWID)
- MixinExtras (bundled with Fabric Loader) — `@Local` is used in mixins
- The rest — MC bundled libraries: guava, gson, log4j2, commons-lang3, fastutil

## Build and verification (Windows)

- `gradlew.bat build` — full build (compileJava + remapJar)
- `gradlew.bat compileJava` — quick compilation check
- `gradlew.bat genSources` — decompile MC and generate sources with Yarn names (for API reading)
- `gradlew.bat runClient` — launch in dev environment

Decompiled sources are located in `%USERPROFILE%\.gradle\caches\fabric-loom\<mc-version>\...` (jars with `-sources.jar` suffix). Look up the API of a specific class there, or in IntelliJ after `genSources`. Online Yarn docs: `https://maven.fabricmc.net/docs/yarn-1.21.11+build.6/`.

## Structure

```
src/main/java/me/yuugao/holymoderation/
  HolyModeration.java           — main entrypoint (ModInitializer, empty)
  client/
    HolyModerationClient.java   — client entrypoint: DI assembly, EventBus, modules, commands
    di/                         — custom DI container (@Inject/@Singleton/@PostConstruct; DIContainer/DIRegistry; modules in di/module/impl)
    mixin/                      — 8 mixins (see holymoderation.mixins.json)
    modules/                    — DrawableModule + impl/*
    util/
      command/                  — Brigadier wrapper (Fabric client command v2)
      service/                  — services (DI @Singleton)
      service/eventbus/         — custom EventBus (@Subscribe + priorities)
      service/config/           — JSON configs (Config, ConfigManagerService, impl/*)
      service/state/            — states (UserState/PlayerState/ModState)
      handler/                  — GlobalExceptionHandler
      factory/                  — DrawableElementFactory
    gui/
      screen/                   — GuiScreen / AnimatedGuiScreen / MainGuiScreen
      tabs/                     — GUI tabs
      drawable/                 — HUD: Drawable / DrawableElement / StatefulDrawableElement, buttons, singleton-elements, RenderState + provider
src/main/resources/
  fabric.mod.json
  holymoderation.mixins.json
  assets/minecraft/shaders/core/*.json + *.vsh + *.fsh  — custom shaders (rect, rounded_rect, soft_rounded_rect, *_outline, rgb_palette)
  assets/minecraft/textures/gui/clear.png
  assets/minecraft/icon.png
```

## Architecture

1. **DI**: `HolyModerationClient.onInitializeClient()` builds the `DIContainer` via `DIRegistry` from modules `ContainerModule`, `ServicesModule`, `ModulesModule`, `DrawableElementsModule`, `FactoriesModule`, `HandlerModule`. Services are annotated with `@Singleton`; injection is done via `@Inject` on the constructor (`@RequiredArgsConstructor(onConstructor_ = @Inject)`) or on a field; `@PostConstruct` is called after creation.
2. **EventBus**: custom (`util/service/eventbus/EventBus`). Handlers use `@Subscribe(priority=N)`; the higher the priority, the earlier it is invoked. Events are subclasses of `Event` (with a `cancelled` flag). Access is via `EventBusService`.
3. **Modules**: `ModuleManagerService.registerAll()` registers modules in the EventBus; modules implement `CommandProvider` (commands) and/or extend `DrawableModule` (HUD).
4. **Commands**: `CommandRegistry` wraps Brigadier (Fabric client command v2). The root command `hm` is registered in `HolyModerationClient.commandsInitialize()`. Specs — `CommandSpec`, arguments — `Argument`.
5. **Configs**: `ConfigManagerService` stores JSON in `~/HolyModeration/Config/*.json` (Gson). Config classes extend `Config`, fields — `@Expose`.
6. **Rendering**: `Render2DService` — custom shaders + `DrawContext`/`MatrixStack`/`TextRenderer`. HUD elements — `StatefulDrawableElement` with `RenderState`/`RenderStateProvider` (modes `LIVE`/`CONFIG`).

## Code rules

- **Do not add comments to code** (unless explicitly requested).
- Preserve the existing architecture and logic — do not refactor during porting.
- User-visible strings — in Russian, via `.formatted(...)` / `String.format`.
- Logging via `LoggerService` (`info`/`debug`/`exception`), prefixes `[HM INFO]` / `[HM DEBUG]` / `[HM EXCEPTION]`.
- New services/modules must be registered in the corresponding `DIModule` / `ModuleManagerService`.
- Lombok — as in the existing code (`@Getter`, `@Setter`, `@RequiredArgsConstructor(onConstructor_ = @Inject)`).
- Do not change `gradle.properties`, `build.gradle`, `fabric.mod.json`, `holymoderation.mixins.json` unless necessary.