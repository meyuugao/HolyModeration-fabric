package me.yuugao.holymoderation.client.config;

import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

import java.util.*;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;
import obfuscator.DontObf;
import obfuscator.ObfRule;

@Getter
@Setter
public class Config {
    public Config() {
        //keyBinds.put("open_main_gui", new KeyBindConfig(KeyBindType.SINGLE_PRESS, GLFW.GLFW_KEY_RIGHT_SHIFT));
    }

    private final String currentVersion = "2.9alphafix"; //tip: ИЗМЕНИ ПЕРЕД РЕЛИЗОМ!

    @Expose
    private String apiToken = StringUtils.EMPTY;

    @Expose
    private boolean soundsEnabled = true;
    @Expose
    private int soundsVolume = 70;
    @Expose
    private int spyDelay = 2;
    @Expose
    private boolean copyButtonEnabled = false;
    @Expose
    private String copyButtonText = "§f§l[§a§lcopy§f§l]";
    @Expose
    private String playerMarker = "§d§l[CHECK]";

    @Expose
    private final List<String> textsList = new ArrayList<>();

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
    private boolean autoTpEnabled = true;
    @Expose
    private boolean autoBanEnabled = true;

    @Expose
    private Map<String, KeyBindConfig> keyBinds = new HashMap<>();

    public KeyBindConfig getKeyBind(String action) {
        return keyBinds.get(action);
    }

    public void addKeyBind(String action, KeyBindConfig config) {
        keyBinds.put(action, config);
    }

    public void removeKeyBind(String action) {
        keyBinds.remove(action);
    }

    public void updateKeyBind(String action, KeyBindType type, int mainKey, int... modifiers) {
        KeyBindConfig keyBind = keyBinds.get(action);
        if (keyBind != null) {
            keyBind.setType(type);
            keyBind.setMainKey(mainKey);
            keyBind.getModifierKeys().clear();
            for (int mod : modifiers) {
                keyBind.getModifierKeys().add(mod);
            }
        }
    }

    @Getter
    @Setter
    public static class KeyBindConfig {
        @Expose
        @DontObf(ObfRule.MAP_FIELD)
        private int mainKey;
        @Expose
        @DontObf(ObfRule.MAP_FIELD)
        private Set<Integer> modifierKeys = new HashSet<>();
        @Expose
        @DontObf(ObfRule.MAP_FIELD)
        private KeyBindType type;

        public KeyBindConfig(KeyBindType type, int mainKey, int... modifiers) {
            this.type = type;
            this.mainKey = mainKey;
            for (int mod : modifiers) {
                this.modifierKeys.add(mod);
            }
        }
    }

    @DontObf({ObfRule.MAP_METHOD, ObfRule.MAP_FIELD})
    public enum KeyBindType {
        SINGLE_PRESS,
        HOLD
    }
}