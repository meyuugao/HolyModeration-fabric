package me.yuugao.holymoderation.client;

import me.yuugao.holymoderation.client.di.DIAccessor;
import me.yuugao.holymoderation.client.di.DIContainer;
import me.yuugao.holymoderation.client.di.DIRegistry;
import me.yuugao.holymoderation.client.di.module.impl.*;
import me.yuugao.holymoderation.client.util.command.CommandRegistry;
import me.yuugao.holymoderation.client.util.handler.GlobalExceptionHandler;
import me.yuugao.holymoderation.client.util.service.HwidService;
import me.yuugao.holymoderation.client.util.service.ModuleManagerService;
import me.yuugao.holymoderation.client.util.service.eventbus.EventBusService;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;

import lombok.Getter;

public class HolyModerationClient implements ClientModInitializer {
    @Getter
    private static DIContainer di;

    @Override
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

        CommandRegistry commandRegistry = di.get(CommandRegistry.class);
        eventBusService.getEventBus().register(commandRegistry);

        commandsInitialize();

        di.get(HwidService.class).calculateHwid();
    }

    private void commandsInitialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(ClientCommandManager.literal("hm"));
            di.get(CommandRegistry.class).contributeBrigadier(dispatcher);
        });
    }
}