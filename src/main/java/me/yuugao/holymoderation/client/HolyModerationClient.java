package me.yuugao.holymoderation.client;

import me.yuugao.holymoderation.client.config.ConfigManager;
import me.yuugao.holymoderation.client.eventbus.EventBus;
import me.yuugao.holymoderation.client.modules.*;
import me.yuugao.holymoderation.client.modules.drawable.element.CheckoutsDrawableElement;
import me.yuugao.holymoderation.client.modules.drawable.element.NotificationsDrawableElement;
import me.yuugao.holymoderation.client.modules.drawable.element.SpyDrawableElement;
import me.yuugao.holymoderation.client.modules.drawable.element.WatermarkDrawableElement;
import me.yuugao.holymoderation.client.modules.drawable.render.PositionMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;
import me.yuugao.holymoderation.client.util.serviceLocator.service.*;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.command.argument.EntityArgumentType;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import obfuscator.DontObf;
import obfuscator.ObfRule;

public class HolyModerationClient implements ClientModInitializer {
    @Override
    @DontObf(ObfRule.MAP_METHOD)
    public void onInitializeClient() {
        ServiceLocator.initialize(new ConfigManager(), new EventBus(), new ChatService(), new CheckoutsService(),
                new GoogleSheetsService(), new GuiManagerService(), new InputService(), new MinecraftService(),
                new NetService(), new NotificationsService(), new PunishmentsService(), new Render2DService(),
                new SchedulerService(), new SoundService(), new StateService(),
                (Logger) LogManager.getLogger(HolyModerationClient.class));
        eventBusInitialize();
        commandsInitialize();
    }

    private void eventBusInitialize() {
        EventBus eventBus = ServiceLocator.getEventBus();
        LoggerService loggerService = ServiceLocator.getLoggerService();

        registerEventListeners(eventBus);
        loggerService.info("Eventbus & modules has been initialized.");
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
                        .then(ClientCommandManager.argument("arg1", StringArgumentType.greedyString())
                                .then(ClientCommandManager.argument("arg2", StringArgumentType.greedyString()))));
            }

            for (String cmd : ServiceLocator.getChatService().FourArgCommands) {
                hm.then(ClientCommandManager.literal(cmd)
                        .then(ClientCommandManager.argument("arg1", StringArgumentType.greedyString())
                                .then(ClientCommandManager.argument("arg2", StringArgumentType.greedyString())
                                        .then(ClientCommandManager.argument("arg3", StringArgumentType.greedyString())
                                                .then(ClientCommandManager.argument("arg4", StringArgumentType.greedyString()))))));
            }

            dispatcher.register(hm);
            dispatcher.register(ClientCommandManager.literal("frz")
                    .then(ClientCommandManager.argument("player", EntityArgumentType.player())));
        });
    }

    public static void registerEventListeners(EventBus eventBus) {
        ServiceContext serviceContext = new ServiceContext();

        eventBus.register(new MainGuiModule(serviceContext));
        eventBus.register(new CheckoutsModule(serviceContext, new CheckoutsDrawableElement(serviceContext, PositionMode.CENTER)));
        eventBus.register(new GuiManagerModule(serviceContext));
        eventBus.register(new KeyBindingModule(serviceContext));
        eventBus.register(new MessageModule(serviceContext));
        eventBus.register(new NetModule(serviceContext));
        eventBus.register(new NotificationsModule(serviceContext, new NotificationsDrawableElement(serviceContext, PositionMode.CENTER)));
        eventBus.register(new PunishmentsModule(serviceContext));
        eventBus.register(new ReportCopyModule(serviceContext));
        eventBus.register(new SettingsModule(serviceContext));
        eventBus.register(new StateModule(serviceContext));
        eventBus.register(new SpyModule(serviceContext, new SpyDrawableElement(serviceContext, PositionMode.UP)));
        eventBus.register(new TwinksCheckModule(serviceContext));
        eventBus.register(new WaterMarkModule(serviceContext, new WatermarkDrawableElement(serviceContext, PositionMode.LEFT_UP)));
    }
}