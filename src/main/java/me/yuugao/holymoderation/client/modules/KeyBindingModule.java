package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.KeyPressEvent;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

public class KeyBindingModule extends Module {
    public KeyBindingModule(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Subscribe
    public void onKeyPress(KeyPressEvent event) {
        serviceContext.getInputService().updatePressedKeys(event.getKey(), event.getAction());
    }
}