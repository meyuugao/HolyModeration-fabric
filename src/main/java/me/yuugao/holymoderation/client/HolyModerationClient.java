package me.yuugao.holymoderation.client;

import me.yuugao.holymoderation.client.di.DIAccessor;
import me.yuugao.holymoderation.client.di.DIContainer;
import me.yuugao.holymoderation.client.di.DIRegistry;
import me.yuugao.holymoderation.client.di.module.impl.*;
import me.yuugao.holymoderation.client.util.command.CommandRegistry;
import me.yuugao.holymoderation.client.util.handler.GlobalExceptionHandler;
import me.yuugao.holymoderation.client.util.service.HwidService;
import me.yuugao.holymoderation.client.util.service.ModuleManagerService;
import me.yuugao.holymoderation.client.util.service.UserValidationService;
import me.yuugao.holymoderation.client.util.service.eventbus.EventBusService;
import me.yuugao.obfuscator.DontObf;
import me.yuugao.obfuscator.ObfRule;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

import lombok.Getter;

public class HolyModerationClient implements ClientModInitializer {
    @Getter
    private static DIContainer di;

    @Override
    @DontObf(ObfRule.MAP_METHOD)
    public void onInitializeClient() {
        di = new DIRegistry()
                .addModule(new ContainerModule())
                .addModule(new ServicesModule())
                .addModule(new ModulesModule())
                .addModule(new DrawableElementsModule())
                .addModule(new FactoriesModule())
                .addModule(new HandlerModule())
                .build();

        di.get(GlobalExceptionHandler.class).register();

        DIAccessor.initialize(di);

        EventBusService eventBusService = di.get(EventBusService.class);
        eventBusService.initializeEventBus();

        di.get(ModuleManagerService.class).registerAll();

        // CommandRegistry must subscribe to CommandSendEvent so registered commands actually execute.
        // (Services are not auto-registered like modules.)
        CommandRegistry commandRegistry = di.get(CommandRegistry.class);
        eventBusService.getEventBus().register(commandRegistry);
        // NOTE: /hm help is intentionally disabled in-product (moved to the docs);
        // the CommandRegistry.registerHelpCommand() entry point is kept available.

        commandsInitialize();

        di.get(HwidService.class).calculateHwid();
        di.get(UserValidationService.class).onMinecraftStart();
    }

    private void commandsInitialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            // Register the "hm" literal so it exists as a root child, then let CommandRegistry
            // attach all declared commands (with their typed arguments and suggestions) to it.
            dispatcher.register(ClientCommandManager.literal("hm"));
            di.get(CommandRegistry.class).contributeBrigadier(dispatcher);
        });
    }
}