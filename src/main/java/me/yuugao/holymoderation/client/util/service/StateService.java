package me.yuugao.holymoderation.client.util.service;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StateService extends Service {
    private boolean enabled = true;
    private boolean connected = false;
    private boolean isOnHW = false;
    private boolean gameInitCompleted = false;
    private boolean apiInitCompleted = false;
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
    private String moderNickname = StringUtils.EMPTY;
    private String moderLocation = StringUtils.EMPTY;
    private String vkUrl = StringUtils.EMPTY;
    private int rank = 0;
    private Map<String, Object> journalProfile = new HashMap<>();
    private Map<String, Object> journalStats = new HashMap<>();

    public void reset() {
        this.enabled = true;
        this.connected = false;
        this.isOnHW = false;
        this.gameInitCompleted = false;
        this.apiInitCompleted = false;
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
        this.moderNickname = StringUtils.EMPTY;
        this.moderLocation = StringUtils.EMPTY;
        this.vkUrl = StringUtils.EMPTY;
        this.rank = 0;
        journalProfile = new HashMap<>();
        journalStats = new HashMap<>();
    }
}