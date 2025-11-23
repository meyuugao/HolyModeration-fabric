package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.config.ConfigManager;
import me.yuugao.holymoderation.client.eventbus.EventBus;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.SchedulerService;
import me.yuugao.holymoderation.client.util.service.ServiceLocator;
import me.yuugao.holymoderation.client.util.service.StateService;
import me.yuugao.holymoderation.client.util.logger.HolyLogger;

public abstract class Module {
    protected final ConfigManager configManager;
    protected final EventBus eventBus;
    protected final MinecraftService minecraftService;
    protected final SchedulerService schedulerService;
    protected final StateService stateService;
    protected HolyLogger holyLogger;

    protected Module() {
        this.configManager = ServiceLocator.getConfigManager();
        this.eventBus = ServiceLocator.getEventBus();
        this.minecraftService = ServiceLocator.getMinecraftService();
        this.schedulerService = ServiceLocator.getSchedulerService();
        this.stateService = ServiceLocator.getStateService();
    }

    public void setLogger(HolyLogger holyLogger) {
        if (this.holyLogger == null) {
            this.holyLogger = holyLogger;
        }
    }
}