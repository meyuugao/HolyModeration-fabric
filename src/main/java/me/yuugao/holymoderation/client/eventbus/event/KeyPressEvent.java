package me.yuugao.holymoderation.client.eventbus.event;

import lombok.Getter;

@Getter
public class KeyPressEvent extends Event {
    private final long window;
    private final int key;
    private final int scancode;
    private final int action;
    private final int modifiers;

    public KeyPressEvent(long window, int key, int scancode, int action, int modifiers) {
        this.window = window;
        this.key = key;
        this.scancode = scancode;
        this.action = action;
        this.modifiers = modifiers;
    }
}