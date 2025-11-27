package me.yuugao.holymoderation.client;

import me.yuugao.holymoderation.client.config.ConfigManager;
import me.yuugao.holymoderation.client.eventbus.EventBus;
import me.yuugao.holymoderation.client.eventbus.event.HudRenderEvent;
import me.yuugao.holymoderation.client.modules.NetSynchronizerModule;
import me.yuugao.holymoderation.client.modules.SettingsModule;
import me.yuugao.holymoderation.client.modules.SpyModule;
import me.yuugao.holymoderation.client.modules.StateModule;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;
import me.yuugao.holymoderation.client.util.serviceLocator.service.*;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import org.slf4j.LoggerFactory;

public class HolyModerationClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ServiceLocator.initialize(new ConfigManager(), new EventBus(), new ChatService(), new MinecraftService(), new NetService(), new Render2DService(), new SchedulerService(), new SoundService(), new StateService(), LoggerFactory.getLogger("HolyModeration/Client"));
        eventBusInitialize();
        ServiceLocator.getLoggerService().getLogger().info("HolyModerationClient has been initialized");

        HudRenderCallback.EVENT.register((drawContext, tickDelta) -> {
            ServiceLocator.getEventBus().invokeEvent(new HudRenderEvent(drawContext, tickDelta));
        });
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
        eventBus.register(new SpyModule());
    }
}