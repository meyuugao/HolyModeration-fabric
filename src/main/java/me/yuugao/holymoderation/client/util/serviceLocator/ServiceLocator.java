package me.yuugao.holymoderation.client.util.serviceLocator;

import me.yuugao.holymoderation.client.config.ConfigManager;
import me.yuugao.holymoderation.client.eventbus.EventBus;
import me.yuugao.holymoderation.client.util.serviceLocator.service.*;

import org.slf4j.Logger;

import lombok.Getter;

public class ServiceLocator {
    @Getter
    private static ConfigManager configManager;
    @Getter
    private static EventBus eventBus;
    @Getter
    private static ChatService chatService;
    @Getter
    private static KeyBindingService keyBindingService;
    @Getter
    private static MinecraftService minecraftService;
    @Getter
    private static NetService netService;
    @Getter
    private static Render2DService render2DService;
    @Getter
    private static SchedulerService schedulerService;
    @Getter
    private static SoundService soundService;
    @Getter
    private static StateService stateService;
    @Getter
    private static LoggerService loggerService;

    public static void initialize(ConfigManager configManager, EventBus eventBus, ChatService chatService, KeyBindingService keyBindingService, MinecraftService minecraftService, NetService netService, Render2DService render2DService, SchedulerService schedulerService, SoundService soundService, StateService stateService, org.slf4j.Logger logger) {
        ServiceLocator.configManager = configManager;
        ServiceLocator.eventBus = eventBus;
        ServiceLocator.chatService = chatService;
        ServiceLocator.keyBindingService = keyBindingService;
        ServiceLocator.minecraftService = minecraftService;
        ServiceLocator.netService = netService;
        ServiceLocator.render2DService = render2DService;
        ServiceLocator.schedulerService = schedulerService;
        ServiceLocator.soundService = soundService;
        ServiceLocator.stateService = stateService;
        logger.info("Base services has been initialized");
        initializeLoggerService(logger);
    }

    private static void initializeLoggerService(Logger logger) {
        loggerService = new LoggerService(logger, chatService, soundService);
        eventBus.setLogger(loggerService);
        chatService.setLogger(loggerService);
        keyBindingService.setLogger(loggerService);
        minecraftService.setLogger(loggerService);
        netService.setLogger(loggerService);
        render2DService.setLogger(loggerService);
        schedulerService.setLogger(loggerService);
        soundService.setLogger(loggerService);
        stateService.setLogger(loggerService);
        loggerService.getLogger().info("Logger service has been initialized");
    }
}