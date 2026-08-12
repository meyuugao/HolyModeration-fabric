package me.yuugao.holymoderation.client.util.service.eventbus.event;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public abstract class Event {
    private boolean cancelled = false;
}