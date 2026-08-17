# AGENTS.md — Universal Delegation Rules

Purpose: a strict multi-agent pipeline with maximum token economy. Stack-agnostic. Operates over the opencode config (roles: build, plan, reviewer) backed by DeepSeek V4 (Pro + Flash) via the native DeepSeek API endpoint.

Language note: English is intentional. Russian tokenizes ~25-30% worse; an always-loaded file in English is the single highest-ROI economy decision.

DeepSeek economics (native v1 endpoint, July 2026 V4-Flash-0731 release):
- V4 Pro: input cache miss $0.27/M, cache hit $0.014/M (19x discount), output $0.87/M.
- V4 Flash: input cache miss $0.14/M, cache hit $0.003/M (47x discount), output $0.28/M.
- Output is ~2-3x more expensive than input — minimize output tokens.
- Context caching is automatic (disk cache, no `cache_control` needed — it is silently ignored by DeepSeek). Cache-hit tokens are billed at the cache-hit rate, shown in `usage.prompt_cache_hit_tokens` of every API response.
- Persistence happens at request boundaries and on common-prefix detection. Cache lives hours-days while in use.
- Peak hours 01:00-04:00 and 06:00-10:00 UTC — schedule batch work outside these for 50% off. Pro concurrency 500, Flash 2500.

Cache hit rules (official DeepSeek docs — api-docs.deepseek.com/guides/kv_cache):
- Cache hit requires the FULL prefix to match byte-for-byte from index 0.
- Any insertion, deletion, or content change before the final position breaks the prefix hash → full cache miss.
- Tool result insertion between assistant and next user message breaks the prefix in naive agent loops. opencode with `setCacheKey: true` mitigates this by sending a stable cache key per session.
- Multiple agents with different system prompts = separate cache entries, no sharing. This is why the agent topology below uses 3 agents, not 6.

Maintenance rule: humans edit this file. Agents MUST NOT write, regenerate, or auto-append to AGENTS.md. Every line must trace to a real incident. No speculative rules. Audit monthly; delete stale rules.

---

## 0. NEVER (critical — read first)

- NEVER let an agent (build/reviewer) edit this file. Lead approves every line. LLM-generated context files measurably reduce success rate.
- NEVER re-read a file already summarized in-session. Cite the summary.
- NEVER dump full codebase context into a subagent. Pass only the relevant slice.
- NEVER accept build output without a reviewer pass (when reviewer was invoked).
- NEVER raise `subagent_depth` above 1 (opencode native key, default 1 already forbids nested subagents). Recursive fan-out is the #1 token blow-up.
- NEVER run more than 2 active subagents. The only subagent is `reviewer`.
- NEVER write "clean up this code" — it strips WHY comments. Use "refactor X, preserve comments that explain intent".
- NEVER ship conflicting rules. Resolve before commit.
- NEVER include philosophy, CONTRIBUTING.md restatement, install guides, or roadmap.
- NEVER inject dynamic data (timestamps, current date, session IDs, working directory paths) into the system prompt, this file, or rules files. One char difference breaks the entire prefix cache.
- NEVER switch providers between sessions. The cache is keyed per-provider; switching from deepseek to anthropic and back invalidates everything.

---

## 1. Topology (3 agents, cache-optimized)

| Role    | Model | Mode     | Write | Shell | Purpose                              |
|---------|-------|----------|-------|-------|--------------------------------------|
| build   | pro   | primary  | allow | ask   | Orchestrator + executor: plan, read, edit, verify, run tests |
| plan    | pro   | primary  | deny  | deny  | Read-only analysis, architecture, tradeoffs |
| reviewer| pro   | subagent | deny  | allow | Verify before commit; PASS or FIX |

Target: 1 single agent (build) per task. Subagent only for pre-commit review.

Why not the old 6-agent topology (pm, build, plan, coder, explorer, reviewer):
- Each agent has its own system prompt → separate DeepSeek cache entries → 0% cache sharing.
- pm + build + coder + explorer + reviewer = 5 separate requests per task = 5 cache misses on system+tools.
- With 1 build agent doing everything, system + tools stay stable across all steps → cache hit rate 80-95%.
- The old "build delegates to coder" pattern doubles tokens (build's system + coder's system) and breaks cache between them.

Rule: pm and plan are replaced by build + plain user prompts. coder is merged into build. explorer is merged into build. Only reviewer stays as a separate subagent — its isolated context is genuinely useful (it should not see build's reasoning, only its output file).

---

## 2. Delegation protocol

Flow: build (does everything itself, including reads and edits) → reviewer (only for pre-commit verification).

- build reads files directly (it has read+glob+grep permissions now).
- build edits files directly (it has edit permission now).
- build writes output to `.agent/task-N.md` ONLY when it needs reviewer to verify — then reviewer reads the file in its isolated context, build passes the path not the content.
- Between independent tasks: user runs `/clear` in opencode GUI, then starts the next task. This drops build's context so the next task starts fresh (no stale history resending every turn).
- Within a single task: do NOT clear context. build accumulates reads + edits in messages[], which is exactly what DeepSeek needs for cache hit (prefix grows at the end, start stays stable).

Handoff files are append-only. reviewer reads the file, not the work that produced it. One file, one owner at a time.

---

## 3. Role contracts

build (orchestrator + executor):
- Accept user task → plan (in head) → read files → edit → run tests → optionally call reviewer.
- Do NOT re-read files already in session context. The summary above is canonical.
- /clear context between independent tasks (drops everything, starts fresh — big saving).
- For a complex task (multi-file refactor, 3+ steps): write a plan to `.agent/plan-N.md` first, then execute step by step. This gives reviewer a reference.
- For a simple task (single file fix): edit directly, no plan file needed.
- On a bug you cannot reproduce in 3 attempts: stop, escalate to user with what you tried.
- When calling reviewer: pass ONLY the path `.agent/task-N.md`, not the content.

plan (read-only investigator):
- Use when user explicitly asks for analysis or design without edits.
- Cannot edit, cannot run shell, cannot delegate.
- Returns: proposal with tradeoffs, file refs, no code changes.
- After plan is approved, user switches to build to implement.

reviewer (verifier):
- Reads `.agent/task-N.md` in isolated context. build passes path only.
- Checks: bugs, security, conventions, regressions, side effects.
- Tools: read, shell (tests). No write.
- Verdict format: `PASS` or `FIX: [file:line] issue`. No prose, no code quotes.
- Mandatory before commit. Flash errs more; reviewer catches before cascade.

---

## 4. Output economy (output costs 2-3x input on DeepSeek V4)

- Subagent (reviewer) returns ONLY final verdict. opencode isolates its context; build sees just the final message.
- Mandate structured returns: `PASS|FIX`, severity tables, line refs. Never novels.
- No chain-of-thought in routine subagent work. CoT burns output tokens; reserve for genuinely hard pro calls.
- Diffs and line-number refs over full-file rewrites.
- Lean tool descriptions. Verbose definitions tax every call — review opencode's tool list in GUI Settings → Tools, disable what you don't use (MCP servers, custom tools).
- `steps` cap per agent: build 50, plan 25, reviewer 25. On limit hit opencode removes tools and forces a summary.
- Format instructions live in the user message at the END, not the system prompt: "Respond with PASS or FIX only", "Return only the JSON object".

---

## 5. Context and cache (THE most important section)

### How DeepSeek cache hit works (simple version)

DeepSeek remembers the BEGINNING of your messages[] array on disk. On the next request, if the new messages[] starts with the EXACT SAME beginning (byte-for-byte), DeepSeek reads that part from cache instead of recomputing it.

- Cache hit on 800K-token prefix: 800K × $0.003/M = $0.0024 (basically free).
- Cache miss on 800K-token prefix: 800K × $0.14/M = $0.112 (50x more expensive).

### What breaks the cache (any of these → full cache miss on entire prefix)

1. **System prompt change** — even one character. opencode injects timestamps, working directory, session IDs by default. Check Settings → Agent → System and remove dynamic fields.
2. **Tool list change** — opencode shuffles tool order or adds/removes one between steps. Disable unused MCP tools in Settings.
3. **Provider switch** — going from `deepseek` to `anthropic` and back creates a fresh cache. Stay on one provider per session.
4. **Message insertion in middle** — happens when opencode inserts a `tool_result` between `assistant` and `next user`. opencode's `setCacheKey: true` mitigates this by sending a stable cache key.
5. **AGENTS.md or rules file change** — these are loaded into system prompt. Edit them between tasks, not during.
6. **Switching agents** — `build` and `plan` have different system prompts. Switching between them in one session invalidates cache. Stay on one agent per task.

### What does NOT break the cache

1. Adding a new user message at the END of messages[].
2. Adding a new assistant response at the END.
3. Adding a new tool_result at the END (after the latest message).
4. Time passing (cache lives hours-days while in use).

### Cache-maximizing workflow

- Start task → build reads files → edits → tests → done. All in ONE build session.
- Do NOT `/clear` in the middle of a task — that destroys the accumulated prefix.
- Do `/clear` BETWEEN tasks — starts fresh, no stale history.
- Do NOT switch to plan or reviewer mid-task unless absolutely necessary. Each switch = cache miss.
- If reviewer is needed: build calls it as subagent. reviewer runs in its own context (separate cache). build's cache stays intact.

### Checking cache hit rate in opencode GUI

1. Open opencode GUI.
2. Run a task (any multi-step task).
3. After task completes, go to: top-right menu (≡) → Usage / Cost.
4. Look for these fields per turn:
    - `prompt_cache_hit_tokens` — should be HIGH (70-95% of input).
    - `prompt_cache_miss_tokens` — should be LOW.
5. If `prompt_cache_hit_tokens` is 0 or near-0 on turn 2+: cache is broken. See troubleshooting section below.

### Troubleshooting zero cache hit

1. **Verify provider config** — `setCacheKey: true` MUST be in `provider.deepseek.options`. Without it opencode does not send a cache key.
2. **Check baseURL** — must be `https://api.deepseek.com/v1` (not `/anthropic`). The Anthropic endpoint silently drops cache_control.
3. **Disable unused tools** — opencode Settings → Tools. Every enabled tool adds ~500-2000 tokens to EVERY request. If you don't use MCP servers, disable them.
4. **Check system prompt for dynamic content** — opencode Settings → Agent → System Prompt. Remove any `{{date}}`, `{{time}}`, `{{cwd}}`, `{{session_id}}` placeholders.
5. **Stay on one agent** — don't switch between build and plan mid-task.
6. **Don't edit AGENTS.md mid-session** — it's loaded into system. Edit between tasks.
7. **First 2-3 turns after `/clear`** will be cache misses (cache needs to be built). After that, hit rate should climb.

---

## 6. Escalation and kill criteria

Escalate (rare — only when build genuinely cannot decide):
- architectural judgment or ambiguity is required → switch to `plan` agent explicitly.
- Do NOT switch back to build mid-task after plan — start a fresh build session with the plan output.

Kill / reassign when:
- build stuck 3+ iterations on same error → `/clear`, restart with sharper user prompt.
- build hits its `steps` cap (50) → task too big, split it.
- review queue exceeds your review bandwidth.

Reviewer ratio: call reviewer only before commits, not every edit. Reviewing every edit wastes tokens.

---

## 7. Anti-patterns (forbidden)

- build delegates to a subagent for trivial edits. (Old coder pattern — kills cache.)
- Full codebase dump into any subagent.
- Accepting build output without reviewer (when commit is intended).
- Re-reading a file build already read in this session.
- /init-generated context committed without human audit.
- Duplicating stack rules across CLAUDE.md / .cursorrules / copilot-instructions.md. Symlink (CLAUDE.md -> AGENTS.md) to prevent drift.
- Stale structural references after refactors.
- Emojis, decorative formatting, prose philosophy.
- Speculative "just in case" rules.
- Recursive subagent spawning (`subagent_depth` > 1).
- Vague rules ("write clean code") — ignored regardless of length; use specific contrarian rules ("use early returns, not nested if").
- Conflicting rules — cause silent stalls.
- Dynamic data in system prompt (timestamps, paths, session IDs).
- Switching providers mid-session.
- Editing AGENTS.md or rules files mid-task.

---

## 8. Checklist before adding a rule

1. Traces to a real incident? No → do not add.
2. Agent can infer it from code/docs? Yes → do not add.
3. Universal (needed every conversation)? No → move to skill or references/.
4. Conflicts with an existing rule? Yes → merge, do not patch.
5. File still under ~100 lines? No → split into nested AGENTS.md per package.
6. Critical rule placed in section 0 (NEVER)? No → move up.
7. Written by a human, not an agent? No → reject.

---

## 9. Prompt templates for common tasks

### Simple bug fix (1 file, 1-5 line change)

```
In src/auth/LoginService.java the method validateToken() throws NPE when
token is null. Fix it to return false instead. Preserve the existing logging.
```

Why this works:
- Concrete file + method + line of failure.
- "Preserve logging" prevents the model from deleting comments.
- 1 step, no plan needed, no reviewer needed.

### Multi-file refactor

```
Refactor: extract the CSV parsing logic from GoogleSheetsService into a new
class CsvParser in the same package. Update all callers. Run tests after.

Plan first to .agent/plan-1.md (list files affected + step-by-step),
then execute step by step, then call reviewer on .agent/task-1.md.
```

Why this works:
- Forces a plan file first (build's reasoning is externalized, not lost).
- Each step is discrete.
- Reviewer reads only the final output file, isolated context.

### Investigation (no edits)

Switch to `plan` agent first, then:

```
Analyze why the DIContainer.resolve() method has 3 synchronized blocks.
Is this necessary for correctness? Are there deadlock risks? Return
a proposal with concrete alternatives, no edits.
```

Why this works:
- plan has read-only permissions, cannot accidentally edit.
- Output is a proposal, not code.
- After approval, switch to build to implement.

### Hard bug (cannot reproduce)

```
Bug: tests for LazyTest.lazyBreaksConstructorCycle pass locally but fail
on CI. Hypothesis: JDK version difference (local 21, CI 17).

Steps to try:
1. Check the test assertions for JDK-17-specific assumptions.
2. Look at Proxy.isProxyClass() behavior differences.
3. If still stuck after 3 attempts, stop and report what you tried.

Do NOT edit the test until you have a confirmed root cause.
```

Why this works:
- Explicit "stop after 3 attempts" prevents loops.
- "Do NOT edit until root cause" prevents flailing edits.
- Reviewer not needed (investigation, not commit).

### Pre-commit review

```
Review .agent/task-1.md before commit. Check:
- No new NPE risks.
- No public API breaking changes.
- Tests still pass (run `./gradlew test`).
Return PASS or FIX with file:line refs. No prose.
```

Why this works:
- Reviewer runs as subagent, isolated context.
- Strict output format (PASS/FIX).
- Concrete checks, not "is this good".

---

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