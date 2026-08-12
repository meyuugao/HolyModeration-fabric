package me.yuugao.holymoderation.client.util.service.config.impl;

import me.yuugao.holymoderation.client.util.service.config.Config;

import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

@Getter
public class KeyBindsConfig extends Config {
    @Expose
    private final Map<String, KeyBindConfig> keyBinds = new HashMap<>();

    public KeyBindsConfig() {
        super("keyBinds");
        keyBinds.put("open_main_gui", new KeyBindConfig(KeyBindType.SINGLE_PRESS, GLFW.GLFW_KEY_RIGHT_SHIFT));
    }

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

    public enum KeyBindType {
        SINGLE_PRESS,
        HOLD
    }

    @Getter
    @Setter
    public static class KeyBindConfig {
        @Expose
        private final Set<Integer> modifierKeys = new HashSet<>();
        @Expose
        private int mainKey;
        @Expose
        private KeyBindType type;

        public KeyBindConfig(KeyBindType type, int mainKey, int... modifiers) {
            this.type = type;
            this.mainKey = mainKey;
            for (int mod : modifiers) {
                this.modifierKeys.add(mod);
            }
        }
    }
}