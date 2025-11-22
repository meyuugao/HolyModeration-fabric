package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.config.ConfigManager;
import me.yuugao.holymoderation.client.eventbus.EventBus;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.SchedulerService;
import me.yuugao.holymoderation.client.util.service.ServiceLocator;
import me.yuugao.holymoderation.client.util.service.StateService;

import org.slf4j.Logger;

public abstract class Module {
    protected final ConfigManager configManager;
    protected final EventBus eventBus;
    protected final MinecraftService minecraftService;
    protected final SchedulerService schedulerService;
    protected final StateService stateService;
    protected final Logger logger;

    protected Module() {
        this.configManager = ServiceLocator.getConfigManager();
        this.eventBus = ServiceLocator.getEventBus();
        this.minecraftService = ServiceLocator.getMinecraftService();
        this.schedulerService = ServiceLocator.getSchedulerService();
        this.stateService = ServiceLocator.getStateService();
        this.logger = ServiceLocator.getLogger();
    }
}
