package me.yuugao.holymoderation.client.util.serviceLocator;

import me.yuugao.holymoderation.client.config.manager.ConfigManager;
import me.yuugao.holymoderation.client.eventbus.EventBus;
import me.yuugao.holymoderation.client.util.serviceLocator.service.*;

import org.apache.logging.log4j.core.Logger;

import lombok.Getter;

public class ServiceLocator {
    @Getter
    private static ConfigManager configManager;
    @Getter
    private static EventBus eventBus;
    @Getter
    private static ChatService chatService;
    @Getter
    private static CheckoutsService checkoutsService;
    @Getter
    private static GoogleSheetsService googleSheetsService;
    @Getter
    private static GuiManagerService guiManagerService;
    @Getter
    private static InputService inputService;
    @Getter
    private static MinecraftService minecraftService;
    @Getter
    private static NetService netService;
    @Getter
    private static NotificationsService notificationsService;
    @Getter
    private static PunishmentsService punishmentsService;
    @Getter
    private static Render2DService render2DService;
    @Getter
    private static SchedulerService schedulerService;
    @Getter
    private static SoundService soundService;
    @Getter
    private static SpyService spyService;
    @Getter
    private static StateService stateService;
    @Getter
    private static LoggerService loggerService;

    public static void initialize(ConfigManager configManager, EventBus eventBus, ChatService chatService,
                                  CheckoutsService checkoutsService, GoogleSheetsService googleSheetsService,
                                  GuiManagerService guiManagerService, InputService inputService,
                                  MinecraftService minecraftService, NetService netService,
                                  NotificationsService notificationsService, PunishmentsService punishmentsService,
                                  Render2DService render2DService, SchedulerService schedulerService,
                                  SoundService soundService, SpyService spyService,
                                  StateService stateService, Logger logger) {
        ServiceLocator.configManager = configManager;
        ServiceLocator.eventBus = eventBus;
        ServiceLocator.chatService = chatService;
        ServiceLocator.checkoutsService = checkoutsService;
        ServiceLocator.googleSheetsService = googleSheetsService;
        ServiceLocator.guiManagerService = guiManagerService;
        ServiceLocator.inputService = inputService;
        ServiceLocator.minecraftService = minecraftService;
        ServiceLocator.netService = netService;
        ServiceLocator.notificationsService = notificationsService;
        ServiceLocator.punishmentsService = punishmentsService;
        ServiceLocator.render2DService = render2DService;
        ServiceLocator.schedulerService = schedulerService;
        ServiceLocator.soundService = soundService;
        ServiceLocator.spyService = spyService;
        ServiceLocator.stateService = stateService;
        logger.info("Base services has been initialized.");
        initializeLoggerService(logger);
    }

    private static void initializeLoggerService(Logger logger) {
        loggerService = new LoggerService(logger);
        eventBus.setLogger(loggerService);
        chatService.setLoggerService(loggerService);
        checkoutsService.setLoggerService(loggerService);
        googleSheetsService.setLoggerService(loggerService);
        guiManagerService.setLoggerService(loggerService);
        inputService.setLoggerService(loggerService);
        minecraftService.setLoggerService(loggerService);
        netService.setLoggerService(loggerService);
        notificationsService.setLoggerService(loggerService);
        punishmentsService.setLoggerService(loggerService);
        render2DService.setLoggerService(loggerService);
        schedulerService.setLoggerService(loggerService);
        soundService.setLoggerService(loggerService);
        spyService.setLoggerService(loggerService);
        stateService.setLoggerService(loggerService);
        loggerService.info("Logger service has been initialized.");
    }
}