package me.yuugao.holymoderation.client.eventbus.event;

import lombok.Getter;
import lombok.Setter;

@Getter
public class MessageSendEvent extends Event {
    private final String content;
    @Setter
    private boolean cancelled = false;

    public MessageSendEvent(String content) {
        this.content = content;
    }
}