package me.yuugao.holymoderation.client.util.service.eventbus.event.impl.input;

import me.yuugao.holymoderation.client.util.service.eventbus.event.Event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class KeyPressEvent extends Event {
    private final long window;
    private final int key;
    private final int scancode;
    private final int action;
    private final int modifiers;
}