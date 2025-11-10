package me.yuugao.holymoderation.client.eventbus.event;

import net.minecraft.text.Text;

import lombok.Getter;

@Getter
public class MessageReceiveEvent extends Event {
    private final Text content;

    public MessageReceiveEvent(Text content) {
        this.content = content;
    }
}