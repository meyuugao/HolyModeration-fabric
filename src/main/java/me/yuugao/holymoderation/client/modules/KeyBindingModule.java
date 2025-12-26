package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.KeyPressEvent;

public class KeyBindingModule extends Module {
    @Subscribe
    public void onKeyPress(KeyPressEvent event) {
        serviceContext.getKeyBindingService().updatePressedKeys(event.getKey(), event.getAction());
    }
}