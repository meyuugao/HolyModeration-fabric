package me.yuugao.holymoderation.client.mixin;

import me.yuugao.holymoderation.client.di.DIAccessor;
import me.yuugao.holymoderation.client.util.service.eventbus.EventBus;
import me.yuugao.holymoderation.client.util.service.eventbus.EventBusService;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.input.KeyPressEvent;

import net.minecraft.client.Keyboard;
import net.minecraft.client.input.KeyInput;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Keyboard.class)
public class KeyBoardMixin {
    @Inject(method = "onKey", at = @At("HEAD"))
    public void onKey(long window, int action, KeyInput keyInput, CallbackInfo ci) {
        EventBus eventBus = DIAccessor.getDI().get(EventBusService.class).getEventBus();

        eventBus.invokeEvent(new KeyPressEvent(window, keyInput.key(), keyInput.scancode(), action, keyInput.modifiers()));
    }
}