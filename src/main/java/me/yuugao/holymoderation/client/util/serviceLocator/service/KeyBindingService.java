package me.yuugao.holymoderation.client.util.serviceLocator.service;

import me.yuugao.holymoderation.client.config.Config;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class KeyBindingService extends Service {
    private final Set<Integer> pressedKeys = new HashSet<>();
    private final Map<String, Boolean> keyStates = new HashMap<>();

    public void updatePressedKeys(int keyCode, int action) {
        if (action == GLFW.GLFW_PRESS) {
            pressedKeys.add(keyCode);
        } else if (action == GLFW.GLFW_RELEASE) {
            pressedKeys.remove(keyCode);
        }
        updateKeyStates();
    }

    private void updateKeyStates() {
        Config config = ServiceLocator.getConfigManager().getConfig();

        for (Map.Entry<String, Config.KeyBindConfig> entry : config.getKeyBinds().entrySet()) {
            String actionName = entry.getKey();
            Config.KeyBindConfig keyBind = entry.getValue();

            boolean mainKeyPressed = pressedKeys.contains(keyBind.getMainKey());
            boolean modifiersPressed = keyBind.getModifierKeys() != null && pressedKeys.containsAll(keyBind.getModifierKeys());
            boolean combinationPressed = mainKeyPressed && modifiersPressed;

            if (keyBind.getType() == Config.KeyBindType.SINGLE_PRESS) {
                if (combinationPressed && !keyStates.getOrDefault(actionName, false)) {
                    keyStates.put(actionName, true);
                }
            } else {
                keyStates.put(actionName, combinationPressed);
            }
        }
    }

    public boolean isKeyHeld(String actionName) {
        Config.KeyBindConfig keyBind = ServiceLocator.getConfigManager().getConfig().getKeyBind(actionName);
        return keyBind != null &&
                keyBind.getType() == Config.KeyBindType.HOLD &&
                keyStates.getOrDefault(actionName, false);
    }

    public boolean wasKeyPressed(String actionName) {
        boolean wasPressed = keyStates.getOrDefault(actionName, false);
        if (wasPressed) {
            keyStates.put(actionName, false);
            return true;
        }
        return false;
    }
}
