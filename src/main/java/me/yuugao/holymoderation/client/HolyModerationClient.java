package me.yuugao.holymoderation.client;

import me.yuugao.holymoderation.client.config.ConfigManager;
import me.yuugao.holymoderation.client.eventbus.EventBus;
import me.yuugao.holymoderation.client.modules.*;
import me.yuugao.holymoderation.client.modules.maingui.MainGuiModule;
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
        ServiceLocator.initialize(new ConfigManager(), new EventBus(), new ChatService(), new KeyBindingService(), new MinecraftService(), new NetService(), new Render2DService(), new SchedulerService(), new SoundService(), new StateService(), LoggerFactory.getLogger("HolyModeration/Client"));
        eventBusInitialize();
        ServiceLocator.getLoggerService().getLogger().info("HolyModerationClient has been initialized");
    }

    private void eventBusInitialize() {
        EventBus eventBus = ServiceLocator.getEventBus();
        registerEventListeners(eventBus);
        ServiceLocator.getLoggerService().getLogger().info("Eventbus & modules has been initialized");
    }

    public static void registerEventListeners(EventBus gnida) {
        gnida.register(new MainGuiModule());
        gnida.register(new KeyBindingModule());
        gnida.register(new NetSynchronizerModule());
        gnida.register(new SettingsModule());
        gnida.register(new StateModule());
        gnida.register(new SpyModule());
    }
}