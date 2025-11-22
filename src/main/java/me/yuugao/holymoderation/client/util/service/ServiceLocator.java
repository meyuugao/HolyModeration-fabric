package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.config.ConfigManager;
import me.yuugao.holymoderation.client.eventbus.EventBus;

import org.slf4j.Logger;

import lombok.Getter;

public class ServiceLocator {
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
    private static Logger logger;

    public static void initialize(ConfigManager configManager, EventBus eventBus, MinecraftService minecraftService, SchedulerService schedulerService, StateService stateService, Logger logger) {
        ServiceLocator.configManager = configManager;
        ServiceLocator.eventBus = eventBus;
        ServiceLocator.minecraftService = minecraftService;
        ServiceLocator.schedulerService = schedulerService;
        ServiceLocator.stateService = stateService;
        ServiceLocator.logger = logger;
    }
}