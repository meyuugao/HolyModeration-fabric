package me.yuugao.holymoderation.client.mixin;

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.gui.screen.Screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ScreenEvents.class)
public class ScreenEventsMixin {
    private static final Event<ScreenEvents.AfterRender> EMPTY_AFTER_RENDER =
            EventFactory.createArrayBacked(ScreenEvents.AfterRender.class,
                    callbacks -> (screen, context, mouseX, mouseY, delta) -> {
                    });

    private static final Event<ScreenEvents.BeforeRender> EMPTY_BEFORE_RENDER =
            EventFactory.createArrayBacked(ScreenEvents.BeforeRender.class,
                    callbacks -> (screen, context, mouseX, mouseY, delta) -> {
                    });

    @Inject(method = "beforeRender", at = @At("HEAD"), cancellable = true)
    private static void onBeforeRender(Screen screen, CallbackInfoReturnable<Event<ScreenEvents.BeforeRender>> cir) {
        if (screen == null) {
            cir.setReturnValue(EMPTY_BEFORE_RENDER);
        }
    }

    @Inject(method = "afterRender", at = @At("HEAD"), cancellable = true)
    private static void onAfterRender(Screen screen, CallbackInfoReturnable<Event<ScreenEvents.AfterRender>> cir) {
        if (screen == null) {
            cir.setReturnValue(EMPTY_AFTER_RENDER);
        }
    }
}