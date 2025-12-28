package me.yuugao.holymoderation.client;

import me.yuugao.holymoderation.client.config.ConfigManager;
import me.yuugao.holymoderation.client.eventbus.EventBus;
import me.yuugao.holymoderation.client.gui.main.MainGuiModule;
import me.yuugao.holymoderation.client.modules.*;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;
import me.yuugao.holymoderation.client.util.serviceLocator.service.*;

import net.fabricmc.api.ClientModInitializer;

import org.slf4j.LoggerFactory;

import obfuscator.DontObf;
import obfuscator.ObfRule;

public class HolyModerationClient implements ClientModInitializer {
    @Override
    @DontObf(ObfRule.MAP_METHOD)
    public void onInitializeClient() {
        ServiceLocator.initialize(new ConfigManager(), new EventBus(), new ChatService(), new CheckoutsService(), new KeyBindingService(), new MinecraftService(), new NetService(), new NotificationService(), new PunishmentsService(), new Render2DService(), new SchedulerService(), new SoundService(), new StateService(), LoggerFactory.getLogger("HolyModeration/Client"));
        eventBusInitialize();
        ServiceLocator.getLoggerService().getLogger().info("HolyModerationClient has been initialized");
    }

    private void eventBusInitialize() {
        EventBus eventBus = ServiceLocator.getEventBus();
        registerEventListeners(eventBus);
        ServiceLocator.getLoggerService().getLogger().info("Eventbus & modules has been initialized");
    }

    public static void registerEventListeners(EventBus eventBus) {
        eventBus.register(new MainGuiModule());
        eventBus.register(new CheckoutsModule());
        eventBus.register(new KeyBindingModule());
        eventBus.register(new MessageModule());
        eventBus.register(new NetSynchronizerModule());
        eventBus.register(new NotificationsRenderModule());
        eventBus.register(new PunishmentsModule());
        eventBus.register(new ReportCopyModule());
        eventBus.register(new SettingsModule());
        eventBus.register(new StateModule());
        eventBus.register(new SpyModule());
        eventBus.register(new TwinksCheckModule());
    }
}