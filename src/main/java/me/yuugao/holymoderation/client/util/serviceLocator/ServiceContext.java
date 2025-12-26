package me.yuugao.holymoderation.client.util.serviceLocator;

import me.yuugao.holymoderation.client.config.ConfigManager;
import me.yuugao.holymoderation.client.eventbus.EventBus;
import me.yuugao.holymoderation.client.util.serviceLocator.service.*;

import lombok.Getter;

@Getter
public class ServiceContext {
    private final ConfigManager configManager;
    private final EventBus eventBus;
    private final ChatService chatService;
    private final KeyBindingService keyBindingService;
    private final MinecraftService minecraftService;
    private final NetService netService;
    private final Render2DService render2DService;
    private final SchedulerService schedulerService;
    private final SoundService soundService;
    private final StateService stateService;
    private LoggerService loggerService;

    public ServiceContext() {
        this.configManager = ServiceLocator.getConfigManager();
        this.eventBus = ServiceLocator.getEventBus();
        this.chatService = ServiceLocator.getChatService();
        this.keyBindingService = ServiceLocator.getKeyBindingService();
        this.minecraftService = ServiceLocator.getMinecraftService();
        this.netService = ServiceLocator.getNetService();
        this.render2DService = ServiceLocator.getRender2DService();
        this.schedulerService = ServiceLocator.getSchedulerService();
        this.soundService = ServiceLocator.getSoundService();
        this.stateService = ServiceLocator.getStateService();
    }

    public void setLogger(LoggerService loggerService) {
        if (this.loggerService == null) {
            this.loggerService = loggerService;
        }
    }
}