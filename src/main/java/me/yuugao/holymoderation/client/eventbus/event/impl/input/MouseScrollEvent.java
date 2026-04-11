package me.yuugao.holymoderation.client.eventbus.event.impl.input;

import me.yuugao.holymoderation.client.eventbus.event.Event;

import lombok.Getter;

@Getter
public class MouseScrollEvent extends Event {
    private final double dx;
    private final double dy;
    private final int x;
    private final int y;

    public MouseScrollEvent(double dx, double dy, int x, int y) {
        this.dx = dx;
        this.dy = dy;
        this.x = x;
        this.y = y;
    }
}