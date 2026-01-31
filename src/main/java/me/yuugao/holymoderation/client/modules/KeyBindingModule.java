package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.KeyPressEvent;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.InputService;

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