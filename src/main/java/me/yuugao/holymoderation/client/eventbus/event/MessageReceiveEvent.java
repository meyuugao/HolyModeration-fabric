package me.yuugao.holymoderation.client.eventbus.event;

import net.minecraft.text.Text;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageReceiveEvent extends Event {
    private Text message;
    private boolean cancelled = false;

    public MessageReceiveEvent(Text message) {
        this.message = message;
    }
}