package me.yuugao.holymoderation.client.mixin;

import me.yuugao.holymoderation.client.eventbus.event.HudRenderEvent;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(method = "render", at = @At("TAIL"))
    public void onRender(DrawContext drawContext, float tickDelta, CallbackInfo ci) {
        ServiceLocator.getEventBus().invokeEvent(new HudRenderEvent(drawContext, tickDelta));
    }
}
