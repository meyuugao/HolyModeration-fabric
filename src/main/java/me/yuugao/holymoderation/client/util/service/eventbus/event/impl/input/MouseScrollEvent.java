package me.yuugao.holymoderation.client.util.service.eventbus.event.impl.input;

import me.yuugao.holymoderation.client.util.service.eventbus.event.Event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class MouseScrollEvent extends Event {
    private final double dx;
    private final double dy;
    private final int x;
    private final int y;
}