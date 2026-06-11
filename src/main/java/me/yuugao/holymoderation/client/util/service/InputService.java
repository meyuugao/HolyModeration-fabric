package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.config.impl.KeyBindsConfig;
import me.yuugao.holymoderation.client.util.service.eventbus.EventBus;
import me.yuugao.holymoderation.client.util.service.eventbus.EventBusService;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.input.MouseScrollEvent;

import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class InputService {
    private final ConfigManagerService configManagerService;
    private final EventBusService eventBusService;

    private final Set<Integer> pressedKeys = new HashSet<>();
    private final Map<String, Boolean> keyStates = new HashMap<>();

    private final Set<Integer> pressedMouseButtons = new HashSet<>();
    private final Map<Integer, Boolean> mouseButtonStates = new HashMap<>();

    public void updatePressedKeys(int keyCode, int action) {
        if (action == GLFW.GLFW_PRESS) {
            pressedKeys.add(keyCode);
        } else if (action == GLFW.GLFW_RELEASE) {
            pressedKeys.remove(keyCode);
        }
        updateKeyStates();
    }

    private void updateKeyStates() {
        KeyBindsConfig keyBindsConfig = configManagerService.getKeyBindsConfig();

        for (Map.Entry<String, KeyBindsConfig.KeyBindConfig> entry : keyBindsConfig.getKeyBinds().entrySet()) {
            String actionName = entry.getKey();
            KeyBindsConfig.KeyBindConfig keyBind = entry.getValue();

            boolean mainKeyPressed = pressedKeys.contains(keyBind.getMainKey());
            boolean modifiersPressed = keyBind.getModifierKeys() != null && pressedKeys.containsAll(keyBind.getModifierKeys());
            boolean combinationPressed = mainKeyPressed && modifiersPressed;

            if (keyBind.getType() == KeyBindsConfig.KeyBindType.SINGLE_PRESS) {
                if (combinationPressed && !keyStates.getOrDefault(actionName, false)) {
                    keyStates.put(actionName, true);
                }
            } else {
                keyStates.put(actionName, combinationPressed);
            }
        }
    }

    public boolean isKeyBindHeld(String actionName) {
        KeyBindsConfig keyBindsConfig = configManagerService.getKeyBindsConfig();
        KeyBindsConfig.KeyBindConfig keyBind = keyBindsConfig.getKeyBind(actionName);
        if (keyBind == null) return false;
        if (keyBind.getType() != KeyBindsConfig.KeyBindType.HOLD) return false;

        boolean mainKeyPressed = pressedKeys.contains(keyBind.getMainKey());
        boolean modifiersPressed = keyBind.getModifierKeys() != null && pressedKeys.containsAll(keyBind.getModifierKeys());
        return mainKeyPressed && modifiersPressed;
    }

    public boolean wasKeyBindPressed(String actionName) {
        KeyBindsConfig keyBindsConfig = configManagerService.getKeyBindsConfig();
        KeyBindsConfig.KeyBindConfig keyBind = keyBindsConfig.getKeyBind(actionName);
        if (keyBind == null) return false;

        if (keyBind.getType() == KeyBindsConfig.KeyBindType.SINGLE_PRESS) {
            boolean pressed = keyStates.getOrDefault(actionName, false);
            if (pressed) keyStates.put(actionName, false);
            return pressed;
        } else if (keyBind.getType() == KeyBindsConfig.KeyBindType.HOLD) {
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

    public void updateScroll(double dx, double dy, double x, double y) {
        EventBus eventBus = eventBusService.getEventBus();
        eventBus.invokeEvent(new MouseScrollEvent(dx, dy, (int) x, (int) y));
    }
}