package me.yuugao.holymoderation.client.util.command;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.Colors;
import me.yuugao.holymoderation.client.util.service.LoggerService;
import me.yuugao.holymoderation.client.util.service.NotificationType;
import me.yuugao.holymoderation.client.util.service.NotificationsService;
import me.yuugao.holymoderation.client.util.service.eventbus.Subscribe;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.chat.CommandSendEvent;
import me.yuugao.obfuscator.DontObf;
import me.yuugao.obfuscator.ObfRule;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton

public class CommandRegistry {
    private final LoggerService loggerService;
    private final NotificationsService notificationsService;
    private final Map<String, CommandSpec> specs = new LinkedHashMap<>();

    public void register(CommandSpec spec) {
        specs.put(spec.name(), spec);
        loggerService.debug("Command registered: %s".formatted(spec.name()));
    }

    /** All registered specs (insertion-ordered). Used by /hm help. */
    public Collection<CommandSpec> getSpecs() {
        return specs.values();
    }

    @Subscribe(priority = 100)
    public void onCommandSend(CommandSendEvent event) {
        String eventCommand = event.getCommand();
        String[] headSplit = eventCommand.split(" ");
        if (!eventCommand.startsWith("hm") || headSplit.length < 2) return;

        CommandSpec spec = specs.get(headSplit[1]);
        if (spec == null) return;

        CommandContext ctx = buildContext(eventCommand, spec.argumentCount());
        loggerService.debug("Dispatching command '/hm %s' with %d arg(s).".formatted(spec.name(), ctx.length()));
        try {
            CommandHandler handler = spec.handler();
            if (handler != null) {
                handler.execute(ctx);
            }
        } catch (Exception e) {
            loggerService.exception("Исключение в CommandRegistry/onCommandSend (%s): %s".formatted(spec.name(), e));
        }
    }

//    @Subscribe(priority = -100)
//    public void onCommandSendSecond(CommandSendEvent event) {
//        if (event.getCommand().startsWith("hm")) event.setCancelled(true);
//    } //tip: фикс

    private CommandContext buildContext(String eventCommand, int argumentCount) {
        if (argumentCount == 0) {
            return new CommandContext(new String[0]);
        }
        // split exactly as the legacy handlers did: limit = argCount + 2 ("hm" + name + args)
        String[] split = eventCommand.split(" ", argumentCount + 2);
        int provided = Math.max(0, split.length - 2);
        // Only include args that were actually provided, so hasArg(i) reflects presence.
        String[] args = new String[provided];
        for (int i = 0; i < provided; i++) {
            args[i] = split[i + 2];
        }
        return new CommandContext(args);
    }

    public void contributeBrigadier(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralArgumentBuilder<FabricClientCommandSource> hm = ClientCommandManager.literal("hm");

        for (CommandSpec spec : specs.values()) {
            LiteralArgumentBuilder<FabricClientCommandSource> node = ClientCommandManager.literal(spec.name());
            buildArgumentChain(node, spec.arguments(), 0);
            hm.then(node);
        }

        // Brigadier merges nodes by name via addChild, so registering "hm" again
        // appends new children to the existing root node without conflict.
        dispatcher.register(hm);
    }

    private void buildArgumentChain(ArgumentBuilder<FabricClientCommandSource, ?> parent,
                                    java.util.List<Argument> arguments, int index) {
        if (index >= arguments.size()) return;
        Argument arg = arguments.get(index);
        RequiredArgumentBuilder<FabricClientCommandSource, ?> ab =
                ClientCommandManager.argument(arg.name(), arg.brigadierType());
        SuggestionProvider<FabricClientCommandSource> sugg = arg.suggestions();
        if (sugg != null) {
            ab.suggests(sugg);
        }
        buildArgumentChain(ab, arguments, index + 1);
        parent.then(ab);
    }

    /**
     * Build the help text grouped by spec.group(). Returns a colored, multi-line string.
     */
    public String buildHelpText() {
        // group -> list of specs, preserving group first-seen order and spec order within group
        java.util.Map<String, java.util.List<CommandSpec>> byGroup = new java.util.LinkedHashMap<>();
        for (CommandSpec s : specs.values()) {
            if (s.name().equals("help")) continue;
            String g = s.group() == null || s.group().isEmpty() ? "Прочее" : s.group();
            byGroup.computeIfAbsent(g, k -> new java.util.ArrayList<>()).add(s);
        }

        StringBuilder sb = new StringBuilder();
        boolean firstGroup = true;
        for (java.util.Map.Entry<String, java.util.List<CommandSpec>> e : byGroup.entrySet()) {
            if (!firstGroup) sb.append("\n");
            firstGroup = false;
            sb.append(Colors.AQUA).append(Colors.BOLD).append("▼ ").append(e.getKey()).append(Colors.WHITE).append("\n");
            for (CommandSpec s : e.getValue()) {
                sb.append(Colors.GOLD).append(s.syntax()).append(Colors.WHITE);
                if (s.description() != null && !s.description().isEmpty()) {
                    sb.append(" — ").append(Colors.GRAY).append(s.description()).append(Colors.WHITE);
                }
                sb.append("\n");
            }
        }
        sb.append(Colors.GRAY).append("Подсказка: аргументы в <угловых скобках> обязательны; TAB дополняет ввод.");
        return sb.toString();
    }

    /**
     * Register the built-in /hm help command. Must be called AFTER all modules have registered
     * their commands so help sees them at dispatch time (specs are read lazily in the handler).
     */
    public void registerHelpCommand() {
        if (specs.containsKey("help")) return;
        register(CommandSpec.of("help")
                .group("Система")
                .description("показать список всех команд")
                .handler(ctx -> notificationsService.addNotification(
                        NotificationType.SUCCESS,
                        Colors.GREEN + Colors.BOLD + "Справка по командам HolyModeration",
                        buildHelpText(),
                        30f)));
    }
}
