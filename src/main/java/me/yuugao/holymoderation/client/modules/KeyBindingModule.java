package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.KeyPressEvent;

public class KeyBindingModule extends Module {
    @Subscribe
    public void onKeyPress(KeyPressEvent event) {
        keyBindingService.updatePressedKeys(event.getKey(), event.getAction());
    }
}