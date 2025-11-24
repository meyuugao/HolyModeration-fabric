package me.yuugao.holymoderation.client;

import me.yuugao.holymoderation.client.config.ConfigManager;
import me.yuugao.holymoderation.client.eventbus.EventBus;
import me.yuugao.holymoderation.client.modules.NetSynchronizerModule;
import me.yuugao.holymoderation.client.modules.SettingsModule;
import me.yuugao.holymoderation.client.modules.StateModule;
import me.yuugao.holymoderation.client.util.service.*;

import net.fabricmc.api.ClientModInitializer;

import org.slf4j.LoggerFactory;

public class HolyModerationClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ServiceLocator.initialize(new ConfigManager(), new EventBus(), new MinecraftService(), new SchedulerService(), new StateService(), new ChatService(), new NetService(), new SoundService(), LoggerFactory.getLogger("HolyModeration/Client"));
        eventBusInitialize();
        ServiceLocator.getLoggerService().getLogger().info("HolyModerationClient has been initialized");
    }

    private void eventBusInitialize() {
        EventBus eventBus = ServiceLocator.getEventBus();
        eventBus.setLogger(ServiceLocator.getLoggerService());
        registerEventListeners(eventBus);
        ServiceLocator.getLoggerService().getLogger().info("Eventbus & modules has been initialized");
    }

    public static void registerEventListeners(EventBus eventBus) {
        eventBus.register(new NetSynchronizerModule());
        eventBus.register(new SettingsModule());
        eventBus.register(new StateModule());
    }
}