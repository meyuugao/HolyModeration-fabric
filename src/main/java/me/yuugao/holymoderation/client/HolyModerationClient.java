package me.yuugao.holymoderation.client;

import me.yuugao.holymoderation.client.config.ConfigManager;
import me.yuugao.holymoderation.client.eventbus.EventBus;
import me.yuugao.holymoderation.client.gui.main.MainGuiModule;
import me.yuugao.holymoderation.client.modules.*;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;
import me.yuugao.holymoderation.client.util.serviceLocator.service.*;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.EntityArgumentType;

import org.slf4j.LoggerFactory;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import obfuscator.DontObf;
import obfuscator.ObfRule;

public class HolyModerationClient implements ClientModInitializer {
    @Override
    @DontObf(ObfRule.MAP_METHOD)
    public void onInitializeClient() {
        ServiceLocator.initialize(new ConfigManager(), new EventBus(), new ChatService(), new CheckoutsService(), new KeyBindingService(), new MinecraftService(), new NetService(), new NotificationService(), new PunishmentsService(), new Render2DService(), new SchedulerService(), new SoundService(), new StateService(), LoggerFactory.getLogger("HolyModeration/Client"));
        eventBusInitialize();
        commandsInitialize();
        ServiceLocator.getLoggerService().logger().info("HolyModerationClient has been initialized");
    }

    private void eventBusInitialize() {
        EventBus eventBus = ServiceLocator.getEventBus();
        registerEventListeners(eventBus);
        ServiceLocator.getLoggerService().logger().info("Eventbus & modules has been initialized");
    }

    private void commandsInitialize() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            LiteralArgumentBuilder<FabricClientCommandSource> hm = ClientCommandManager.literal("hm");

            for (String cmd : ServiceLocator.getChatService().NoArgCommands) {
                hm.then(ClientCommandManager.literal(cmd));
            }

            for (String cmd : ServiceLocator.getChatService().PlayerCommands) {
                hm.then(ClientCommandManager.literal(cmd)
                        .then(ClientCommandManager.argument("player", EntityArgumentType.player())));
            }

            for (String cmd : ServiceLocator.getChatService().OneArgCommands) {
                hm.then(ClientCommandManager.literal(cmd)
                        .then(ClientCommandManager.argument("arg1", StringArgumentType.greedyString())));
            }

            for (String cmd : ServiceLocator.getChatService().TwoArgCommands) {
                hm.then(ClientCommandManager.literal(cmd)
                        .then(ClientCommandManager.argument("arg1", StringArgumentType.string())
                                .then(ClientCommandManager.argument("arg2", StringArgumentType.greedyString()))));
            }

            for (String cmd : ServiceLocator.getChatService().FourArgCommands) {
                hm.then(ClientCommandManager.literal(cmd)
                        .then(ClientCommandManager.argument("arg1", StringArgumentType.string())
                                .then(ClientCommandManager.argument("arg2", StringArgumentType.string())
                                        .then(ClientCommandManager.argument("arg3", StringArgumentType.string())
                                                .then(ClientCommandManager.argument("arg4", StringArgumentType.string()))))));
            }

            dispatcher.register(hm);
            dispatcher.register(ClientCommandManager.literal("frz").then(ClientCommandManager.argument("player", EntityArgumentType.player())));
        });
    }

    public static void registerEventListeners(EventBus eventBus) {
        eventBus.register(new MainGuiModule());
        eventBus.register(new CheckoutsModule());
        eventBus.register(new KeyBindingModule());
        eventBus.register(new MessageModule());
        eventBus.register(new NetSynchronizerModule());
        eventBus.register(new PunishmentsModule());
        eventBus.register(new ReportCopyModule());
        eventBus.register(new SettingsModule());
        eventBus.register(new StateModule());
        eventBus.register(new SpyModule());
        eventBus.register(new TwinksCheckModule());
    }
}