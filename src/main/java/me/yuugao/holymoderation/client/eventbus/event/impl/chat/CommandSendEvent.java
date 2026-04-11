package me.yuugao.holymoderation.client.eventbus.event.impl.chat;

import me.yuugao.holymoderation.client.eventbus.event.Event;

import lombok.Getter;

@Getter
public class CommandSendEvent extends Event {
    private final String command;

    public CommandSendEvent(String command) {
        this.command = command;
    }
}