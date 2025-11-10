package me.yuugao.holymoderation.client.eventbus.event;

import net.minecraft.text.Text;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageModifyEvent extends Event {
    private Text message;
    private final boolean overlay;

    public MessageModifyEvent(Text message, boolean overlay) {
        this.message = message;
        this.overlay = overlay;
    }
}