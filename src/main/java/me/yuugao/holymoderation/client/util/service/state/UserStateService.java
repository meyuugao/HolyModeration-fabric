package me.yuugao.holymoderation.client.util.service.state;

import me.yuugao.holymoderation.client.di.annotations.Singleton;

import org.apache.commons.lang3.StringUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Singleton
public class UserStateService {
    private String userNickname = StringUtils.EMPTY;
    private String userLocation = StringUtils.EMPTY;

    private boolean connected = false;
    private boolean isOnHW = false;
    private boolean gameInitCompleted = false;
    private boolean inHub = false;

    private boolean vanishEnabled = false;
    private boolean flyEnabled = false;
    private boolean gm3Enabled = false;
    private boolean hacAlertsEnabled = false;
    private boolean godEnabled = false;

    public void reset() {
        this.userNickname = StringUtils.EMPTY;
        this.userLocation = StringUtils.EMPTY;
        this.connected = false;
        this.isOnHW = false;
        this.gameInitCompleted = false;
        this.inHub = false;
        this.vanishEnabled = false;
        this.flyEnabled = false;
        this.gm3Enabled = false;
        this.hacAlertsEnabled = false;
        this.godEnabled = false;
    }
}