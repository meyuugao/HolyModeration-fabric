package me.yuugao.holymoderation.client.util.command;

import me.yuugao.holymoderation.client.di.DIAccessor;
import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.service.LoggerService;
import me.yuugao.holymoderation.client.util.service.NotificationsService;
import me.yuugao.holymoderation.client.util.service.eventbus.Subscribe;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.chat.CommandSendEvent;
import me.yuugao.holymoderation.client.util.service.state.ModStateService;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton

public class CommandRegistry {
    private static final Set<String> UPDATE_REQUIRED_COMMANDS = Set.of("net", "sban", "frz", "unfrz");

    private final LoggerService loggerService;
    private final Map<String, CommandSpec> specs = new LinkedHashMap<>();

    public void register(CommandSpec spec) {
        specs.put(spec.name(), spec);
        loggerService.debug("Command registered: %s".formatted(spec.name()));
    }

    public Collection<CommandSpec> getSpecs() {
        return specs.values();
    }

    @Subscribe(priority = 100)
    public void onCommandSend(CommandSendEvent event) {
        String eventCommand = event.getCommand();
        if (!isHmCommand(eventCommand)) return;

        event.setCancelled(true);

        if (!passesModStateGate(eventCommand)) return;

        String[] headSplit = eventCommand.split(" ");
        if (headSplit.length < 2) return;

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

    private boolean passesModStateGate(String eventCommand) {

        ModStateService modStateService = DIAccessor.getDI().get(ModStateService.class);

        if (modStateService.isBlocked() || modStateService.isForceBlocked()) {
            return false;
        }
        String[] split = eventCommand.split(" ");
        String subCommand = split.length >= 2 ? split[1] : "";
        if (!modStateService.isEnabled()) {
            return subCommand.equals("enable");
        }
        if (modStateService.isUpdateRequired()) {
            return UPDATE_REQUIRED_COMMANDS.contains(subCommand);
        }
        return true;
    }

    private boolean isHmCommand(String eventCommand) {
        return eventCommand.equals("hm") || eventCommand.startsWith("hm ");
    }

    private CommandContext buildContext(String eventCommand, int argumentCount) {
        if (argumentCount == 0) {
            return new CommandContext(new String[0]);
        }
        String[] split = eventCommand.split(" ", argumentCount + 2);
        int provided = Math.max(0, split.length - 2);
        String[] args = new String[provided];
        System.arraycopy(split, 2, args, 0, provided);
        return new CommandContext(args);
    }

    public void contributeBrigadier(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        LiteralArgumentBuilder<FabricClientCommandSource> hm = ClientCommandManager.literal("hm");

        for (CommandSpec spec : specs.values()) {
            LiteralArgumentBuilder<FabricClientCommandSource> node = ClientCommandManager.literal(spec.name());
            buildArgumentChain(node, spec.arguments(), 0);
            hm.then(node);
        }

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
}
