package me.yuugao.holymoderation.client.util;

import org.apache.commons.lang3.StringUtils;

public class State {
    public boolean enabled = true;
    public boolean isOnHW = false;
    public boolean gameInitCompleted = false;
    public boolean apiInitCompleted = false;
    public boolean inHub = false;
    public boolean vanishEnabled = false;
    public boolean flyEnabled = false;
    public boolean gm3Enabled = false;
    public boolean hacAlertsEnabled = false;
    public boolean godEnabled = false;
    public String player = StringUtils.EMPTY;
    public String spyPlayer = StringUtils.EMPTY;
    public String spyPlayerStatus = StringUtils.EMPTY;
    public String spyPlayerActivity = StringUtils.EMPTY;
    public String moderNickname = StringUtils.EMPTY;
    public String moderLocation = StringUtils.EMPTY;
    public String vkUrl = StringUtils.EMPTY;
    public int rank = 0;
}