package me.yuugao.holymoderation.client.config;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Config {
    public Config() {
    }

    private final String currentVersion = "2.9alphafix";

    @Expose
    private String apiToken = StringUtils.EMPTY;
    @Expose
    private boolean soundsEnabled = true;
    @Expose
    private int spyDelay = 1;
    @Expose
    private boolean copyButtonEnabled = false;
    @Expose
    private String copyButtonText = "§f§l[§a§lcopy§f§l]";
    @Expose
    private String playerMarker = "§d§l[CHECK]";

    @Expose
    private String texts = StringUtils.EMPTY;

    @Expose
    private boolean autoVanishEnabled = false;
    @Expose
    private boolean autoFlyEnabled = false;
    @Expose
    private boolean autoGm3Enabled = false;
    @Expose
    private boolean autoHacAlertsEnabled = false;
    @Expose
    private boolean autoGodEnabled = false;

    @Expose
    private boolean dupeIpEnabled = false;
    @Expose
    private boolean autoAnyDeskEnabled = true;
    @Expose
    private boolean autoTpEnabled = true;
    @Expose
    private boolean autoBanEnabled = true;
}