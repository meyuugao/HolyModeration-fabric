package me.yuugao.holymoderation.client.util.serviceLocator.service;

import me.yuugao.holymoderation.client.HolyModerationClient;
import me.yuugao.holymoderation.client.eventbus.EventBus;
import me.yuugao.holymoderation.client.modules.GuiManagerModule;
import me.yuugao.holymoderation.client.modules.StateModule;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StateService extends Service {
    private boolean enabled = true;
    private boolean blocked = false;
    private boolean connected = false;
    private boolean isOnHW = false;
    private boolean gameInitCompleted = false;
    private boolean inHub = false;
    private boolean vanishEnabled = false;
    private boolean flyEnabled = false;
    private boolean gm3Enabled = false;
    private boolean hacAlertsEnabled = false;
    private boolean godEnabled = false;
    private String player = StringUtils.EMPTY;
    private String spyPlayer = StringUtils.EMPTY;
    private String spyPlayerStatus = StringUtils.EMPTY;
    private String spyPlayerActivity = StringUtils.EMPTY;
    private String userNickname = StringUtils.EMPTY;
    private String userLocation = StringUtils.EMPTY;
    private String vkUrl = StringUtils.EMPTY;
    private boolean checkingTwinks = false;
    private int rank = 0;

    public void reset() {
        this.connected = false;
        this.isOnHW = false;
        this.gameInitCompleted = false;
        this.inHub = false;
        this.vanishEnabled = false;
        this.flyEnabled = false;
        this.gm3Enabled = false;
        this.hacAlertsEnabled = false;
        this.godEnabled = false;
        this.player = StringUtils.EMPTY;
        this.spyPlayer = StringUtils.EMPTY;
        this.spyPlayerStatus = StringUtils.EMPTY;
        this.spyPlayerActivity = StringUtils.EMPTY;
        this.userNickname = StringUtils.EMPTY;
        this.userLocation = StringUtils.EMPTY;
        this.vkUrl = StringUtils.EMPTY;
        this.rank = 0;
        ServiceLocator.getGuiManagerService().clearDrawableModules();
    }

    public void unregisterEventListeners() {
        EventBus eventBus = ServiceLocator.getEventBus();
        ServiceContext ctx = new ServiceContext();
        eventBus.clear();
        eventBus.register(new StateModule(ctx));
        eventBus.register(new GuiManagerModule(ctx));
    }

    public void registerEventListeners() {
        HolyModerationClient.registerEventListeners(ServiceLocator.getEventBus());
    }

    public void disable() {
        enabled = false;
        unregisterEventListeners();
    }

    public void enable() {
        enabled = true;
        registerEventListeners();
    }

    public void block() {
        blocked = true;
        unregisterEventListeners();
    }

    public void unblock() {
        blocked = false;
        registerEventListeners();
    }
}