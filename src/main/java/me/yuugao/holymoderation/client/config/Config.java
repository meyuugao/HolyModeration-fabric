package me.yuugao.holymoderation.client.config;

import me.yuugao.holymoderation.client.obfuscation.StringObfuscator;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Config {
    public Config() {
        keyBinds.put("open_main_gui", new KeyBindConfig(KeyBindType.SINGLE_PRESS, GLFW.GLFW_KEY_RIGHT_SHIFT));
    }

    private final String currentVersion = "2.9alphafix";

    @Expose
    private String apiToken = StringUtils.EMPTY;
    
    public String getApiToken() {
        return StringObfuscator.decrypt(apiToken);
    }
    
    public void setApiToken(String token) {
        this.apiToken = StringObfuscator.encrypt(token);
    }
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
        private int mainKey;
        @Expose
        private Set<Integer> modifierKeys = new HashSet<>();
        @Expose
        private KeyBindType type = KeyBindType.SINGLE_PRESS;

        public KeyBindConfig() {
        }

        public KeyBindConfig(KeyBindType type, int mainKey, int... modifiers) {
            this.mainKey = mainKey;
            for (int mod : modifiers) {
                this.modifierKeys.add(mod);
            }
        }
    }

    public enum KeyBindType {
        SINGLE_PRESS,
        HOLD
    }
}