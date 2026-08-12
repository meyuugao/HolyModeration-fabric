package me.yuugao.holymoderation.client.util.service.eventbus.event.impl.input;

import me.yuugao.holymoderation.client.util.service.eventbus.event.Event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class MouseClickEvent extends Event {
    private final int button;
    private final int x;
    private final int y;
}
