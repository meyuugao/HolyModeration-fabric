package me.yuugao.holymoderation.client.util.service.eventbus.event.impl.chat;

import me.yuugao.holymoderation.client.util.service.eventbus.event.Event;

import net.minecraft.text.Text;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class MessageReceiveEvent extends Event {
    private Text message;
}