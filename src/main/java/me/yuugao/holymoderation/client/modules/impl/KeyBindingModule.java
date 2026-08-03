package me.yuugao.holymoderation.client.modules.impl;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.service.InputService;
import me.yuugao.holymoderation.client.util.service.eventbus.Subscribe;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.input.KeyPressEvent;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class KeyBindingModule {
    private final InputService inputService;

    @Subscribe
    public void onKeyPress(KeyPressEvent event) {
        inputService.updatePressedKeys(event.getKey(), event.getAction());
    }
}