package me.yuugao.holymoderation.client.eventbus.event;

import lombok.Getter;

@Getter
public class MessageSendEvent extends Event {
    private final String content;

    public MessageSendEvent(String content) {
        this.content = content;
    }
}