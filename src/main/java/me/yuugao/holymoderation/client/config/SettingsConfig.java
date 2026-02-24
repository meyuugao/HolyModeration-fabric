package me.yuugao.holymoderation.client.config;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SettingsConfig extends Config {
    @Expose
    private final List<String> textsList = new ArrayList<>();
    @Expose
    private boolean soundsEnabled = true;
    @Expose
    private int soundsVolume = 70;

    @Expose
    private boolean copyButtonEnabled = false;
    @Expose
    private String copyButtonText = "§f§l[§a§lcopy§f§l]";
    @Expose
    private String playerMarker = "§d§l[CHECK]";
    @Expose
    private boolean autoVanishEnabled = true;
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
    private boolean autoCheckoutTpEnabled = true;
    @Expose
    private boolean autoBanEnabled = true;
    @Expose
    private int spyDelay = 2;
    @Expose
    private boolean autoSpyTpEnabled = false;
    public SettingsConfig() {
        super("settings");
    }
}