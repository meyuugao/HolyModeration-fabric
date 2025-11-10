package me.yuugao.holymoderation.client.config;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.annotations.Expose;

public class Config {
    public final String CURRENT_VERSION = "2.9fabric";

    @Expose
    public String apiToken = StringUtils.EMPTY;
    @Expose
    public boolean soundsEnabled = true;
    @Expose
    public int spyDelay = 1;
    @Expose
    public boolean copyButtonEnabled = false;
    @Expose
    public String copyButtonText = "§f§l[§a§lcopy§f§l]";
    @Expose
    public String playerMarker = "§d§l[CHECK]";

    @Expose
    public String texts = StringUtils.EMPTY;

    @Expose
    public boolean autoVanishEnabled = false;
    @Expose
    public boolean autoFlyEnabled = false;
    @Expose
    public boolean autoGm3Enabled = false;
    @Expose
    public boolean autoHacAlertsEnabled = false;
    @Expose
    public boolean autoGodEnabled = false;

    @Expose
    public boolean dupeIpEnabled = false;
    @Expose
    public boolean autoAnyDeskEnabled = true;
    @Expose
    public boolean autoTpEnabled = true;
    @Expose
    public boolean autoBanEnabled = true;
}