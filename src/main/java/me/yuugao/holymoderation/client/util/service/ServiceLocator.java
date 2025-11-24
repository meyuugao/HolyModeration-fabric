package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.config.ConfigManager;
import me.yuugao.holymoderation.client.eventbus.EventBus;
import me.yuugao.holymoderation.client.util.logger.HolyLogger;

import org.slf4j.Logger;

import lombok.Getter;

public class ServiceLocator {
    //tip: сделать так чтобы можно было ребутнуть любой сервис
    @Getter
    private static ConfigManager configManager;
    @Getter
    private static EventBus eventBus;
    @Getter
    private static MinecraftService minecraftService;
    @Getter
    private static SchedulerService schedulerService;
    @Getter
    private static StateService stateService;
    @Getter
    private static ChatService chatService;
    @Getter
    private static NetService netService;
    @Getter
    private static SoundService soundService;
    @Getter
    private static HolyLogger loggerService;

    public static void initialize(ConfigManager configManager, EventBus eventBus, MinecraftService minecraftService, SchedulerService schedulerService, StateService stateService, ChatService chatService, NetService netService, SoundService soundService, org.slf4j.Logger logger) {
        ServiceLocator.configManager = configManager;
        ServiceLocator.eventBus = eventBus;
        ServiceLocator.minecraftService = minecraftService;
        ServiceLocator.schedulerService = schedulerService;
        ServiceLocator.stateService = stateService;
        ServiceLocator.chatService = chatService;
        ServiceLocator.netService = netService;
        ServiceLocator.soundService = soundService;
        logger.info("Base services has been initialized");
        initializeLoggerService(logger);
    }

    private static void initializeLoggerService(Logger logger) {
        loggerService = new HolyLogger(logger, chatService, soundService);
        minecraftService.setLogger(loggerService);
        schedulerService.setLogger(loggerService);
        stateService.setLogger(loggerService);
        chatService.setLogger(loggerService);
        netService.setLogger(loggerService);
        soundService.setLogger(loggerService);
        loggerService.getLogger().info("Logger service has been initialized");
    }
}