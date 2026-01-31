package me.yuugao.holymoderation.client.util.serviceLocator.service;

import me.yuugao.holymoderation.client.config.Config;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class InputService extends Service {
    private final Set<Integer> pressedKeys = new HashSet<>();
    private final Map<String, Boolean> keyStates = new HashMap<>();

    private final Set<Integer> pressedMouseButtons = new HashSet<>();
    private final Map<Integer, Boolean> mouseButtonStates = new HashMap<>();
    private double scrollDeltaX = 0;
    private double scrollDeltaY = 0;

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

    public boolean isKeyBindHeld(String actionName) {
        Config config = ServiceLocator.getConfigManager().getConfig();

        Config.KeyBindConfig keyBind = config.getKeyBind(actionName);
        if (keyBind == null) return false;
        if (keyBind.getType() != Config.KeyBindType.HOLD) return false;

        boolean mainKeyPressed = pressedKeys.contains(keyBind.getMainKey());
        boolean modifiersPressed = keyBind.getModifierKeys() != null && pressedKeys.containsAll(keyBind.getModifierKeys());

        return mainKeyPressed && modifiersPressed;
    }

    public boolean wasKeyBindPressed(String actionName) {
        Config config = ServiceLocator.getConfigManager().getConfig();

        Config.KeyBindConfig keyBind = config.getKeyBind(actionName);
        if (keyBind == null) return false;

        if (keyBind.getType() == Config.KeyBindType.SINGLE_PRESS) {
            boolean pressed = keyStates.getOrDefault(actionName, false);
            if (pressed) keyStates.put(actionName, false);
            return pressed;
        } else if (keyBind.getType() == Config.KeyBindType.HOLD) {
            return isKeyBindHeld(actionName);
        }
        return false;
    }

    public void updateMouseButton(int button, int action) {
        if (action == GLFW.GLFW_PRESS) {
            pressedMouseButtons.add(button);
            mouseButtonStates.put(button, true);
        } else if (action == GLFW.GLFW_RELEASE) {
            pressedMouseButtons.remove(button);
        }
    }

    public boolean isMouseButtonHeld(int button) {
        return pressedMouseButtons.contains(button);
    }

    public boolean wasMouseButtonPressed(int button) {
        boolean wasPressed = mouseButtonStates.getOrDefault(button, false);
        if (wasPressed) mouseButtonStates.put(button, false);
        return wasPressed;
    }

    public void updateScroll(double dx, double dy) {
        scrollDeltaX += dx;
        scrollDeltaY += dy;
    }

    public double getScrollDeltaX() {
        double dx = scrollDeltaX;
        scrollDeltaX = 0;
        return dx;
    }

    public double getScrollDeltaY() {
        double dy = scrollDeltaY;
        scrollDeltaY = 0;
        return dy;
    }
}