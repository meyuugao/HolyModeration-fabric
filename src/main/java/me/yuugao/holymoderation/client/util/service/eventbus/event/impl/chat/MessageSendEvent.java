package me.yuugao.holymoderation.client.util.service.eventbus.event.impl.chat;

import me.yuugao.holymoderation.client.util.service.eventbus.event.Event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class MessageSendEvent extends Event {
    private final String content;
}