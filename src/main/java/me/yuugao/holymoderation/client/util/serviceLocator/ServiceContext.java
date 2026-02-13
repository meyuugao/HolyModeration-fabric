package me.yuugao.holymoderation.client.util.serviceLocator;

import me.yuugao.holymoderation.client.config.manager.ConfigManager;
import me.yuugao.holymoderation.client.eventbus.EventBus;
import me.yuugao.holymoderation.client.util.serviceLocator.service.*;

import lombok.Getter;

@Getter
public class ServiceContext {
    private final ConfigManager configManager;
    private final EventBus eventBus;
    private final ChatService chatService;
    private final CheckoutsService checkoutsService;
    private final GoogleSheetsService googleSheetsService;
    private final GuiManagerService guiManagerService;
    private final InputService inputService;
    private final MinecraftService minecraftService;
    private final NetService netService;
    private final NotificationsService notificationsService;
    private final PunishmentsService punishmentsService;
    private final Render2DService render2DService;
    private final SchedulerService schedulerService;
    private final SoundService soundService;
    private final SpyService spyService;
    private final StateService stateService;
    private LoggerService loggerService;

    public ServiceContext() {
        this.configManager = ServiceLocator.getConfigManager();
        this.eventBus = ServiceLocator.getEventBus();
        this.chatService = ServiceLocator.getChatService();
        this.checkoutsService = ServiceLocator.getCheckoutsService();
        this.googleSheetsService = ServiceLocator.getGoogleSheetsService();
        this.guiManagerService = ServiceLocator.getGuiManagerService();
        this.inputService = ServiceLocator.getInputService();
        this.minecraftService = ServiceLocator.getMinecraftService();
        this.netService = ServiceLocator.getNetService();
        this.notificationsService = ServiceLocator.getNotificationsService();
        this.punishmentsService = ServiceLocator.getPunishmentsService();
        this.render2DService = ServiceLocator.getRender2DService();
        this.schedulerService = ServiceLocator.getSchedulerService();
        this.soundService = ServiceLocator.getSoundService();
        this.spyService = ServiceLocator.getSpyService();
        this.stateService = ServiceLocator.getStateService();
    }

    public void setLogger(LoggerService loggerService) {
        if (this.loggerService == null) {
            this.loggerService = loggerService;
        }
    }
}