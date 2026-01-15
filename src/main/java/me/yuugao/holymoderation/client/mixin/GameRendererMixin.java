package me.yuugao.holymoderation.client.mixin;

import me.yuugao.holymoderation.client.eventbus.event.HudRenderEvent;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/systems/RenderSystem;clear(IZ)V",
                    shift = At.Shift.AFTER,
                    ordinal = 1
            )
    )
    private void onRender(float tickDelta, long startTime, boolean tick, CallbackInfo ci, @Local DrawContext drawContext) {
        ServiceLocator.getEventBus().invokeEvent(new HudRenderEvent(drawContext, tickDelta));
    }
}