package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.config.ConfigManager;
import me.yuugao.holymoderation.client.eventbus.EventBus;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;
import me.yuugao.holymoderation.client.util.serviceLocator.service.*;

public abstract class Module {
    protected final ConfigManager configManager;
    protected final EventBus eventBus;
    protected final ChatService chatService;
    protected final MinecraftService minecraftService;
    protected final NetService netService;
    protected final Render2DService render2DService;
    protected final SchedulerService schedulerService;
    protected final SoundService soundService;
    protected final StateService stateService;
    protected LoggerService loggerService;

    protected Module() {
        this.configManager = ServiceLocator.getConfigManager();
        this.eventBus = ServiceLocator.getEventBus();
        this.chatService = ServiceLocator.getChatService();
        this.minecraftService = ServiceLocator.getMinecraftService();
        this.netService = ServiceLocator.getNetService();
        this.render2DService = ServiceLocator.getRender2DService();
        this.schedulerService = ServiceLocator.getSchedulerService();
        this.soundService = ServiceLocator.getSoundService();
        this.stateService = ServiceLocator.getStateService();
    }

    public void setLogger(LoggerService holyLogger) {
        if (this.loggerService == null) {
            this.loggerService = holyLogger;
        }
    }
}