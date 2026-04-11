package me.yuugao.holymoderation.client.modules.impl;

import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.impl.input.KeyPressEvent;
import me.yuugao.holymoderation.client.modules.Module;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.InputService;

public class KeyBindingModule extends Module {
    public KeyBindingModule(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Subscribe
    public void onKeyPress(KeyPressEvent event) {
        InputService inputService = serviceContext.getInputService();

        inputService.updatePressedKeys(event.getKey(), event.getAction());
    }
}