package me.yuugao.holymoderation.client.mixin;

import me.yuugao.holymoderation.client.di.DIAccessor;
import me.yuugao.holymoderation.client.util.service.eventbus.EventBus;
import me.yuugao.holymoderation.client.util.service.eventbus.EventBusService;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.render.RenderEvent;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderTickCounter;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Shadow
    @Final
    private MinecraftClient client;

    @Inject(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/render/GuiRenderer;render(Lcom/mojang/blaze3d/buffers/GpuBufferSlice;)V",
                    shift = At.Shift.AFTER
            )
    )
    private void onRender(RenderTickCounter tickCounter, boolean tick, CallbackInfo ci, @Local DrawContext drawContext) {
        if (client.world != null) {
            int mouseX = (int) client.mouse.getScaledX(client.getWindow());
            int mouseY = (int) client.mouse.getScaledY(client.getWindow());
            EventBus eventBus = DIAccessor.getDI().get(EventBusService.class).getEventBus();

            eventBus.invokeEvent(new RenderEvent(drawContext, mouseX, mouseY, tickCounter.getTickProgress(false)));
        }
    }
}
