package me.yuugao.holymoderation.client.eventbus.event.impl.chat;

import me.yuugao.holymoderation.client.eventbus.event.Event;

import net.minecraft.text.Text;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageReceiveEvent extends Event {
    private Text message;

    public MessageReceiveEvent(Text message) {
        this.message = message;
    }
}