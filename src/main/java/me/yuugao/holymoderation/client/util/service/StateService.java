package me.yuugao.holymoderation.client.util.service;

import org.apache.commons.lang3.StringUtils;

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
}