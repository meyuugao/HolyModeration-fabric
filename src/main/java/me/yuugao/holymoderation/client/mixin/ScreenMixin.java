package me.yuugao.holymoderation.client.mixin;

import me.yuugao.holymoderation.client.di.DIAccessor;
import me.yuugao.holymoderation.client.util.service.eventbus.EventBus;
import me.yuugao.holymoderation.client.util.service.eventbus.EventBusService;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.render.RenderEvent;
import me.yuugao.obfuscator.DontObf;
import me.yuugao.obfuscator.ObfRule;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {
    @DontObf(ObfRule.MAP_FIELD)
    @Shadow
    @Nullable
    protected MinecraftClient client;

    @Inject(method = "render", at = @At("HEAD"))
    private void onRender(DrawContext context, int mouseX, int mouseY, float tickDelta, CallbackInfo ci) {
        EventBus eventBus = DIAccessor.getDI().get(EventBusService.class).getEventBus();

        eventBus.invokeEvent(new RenderEvent(context, mouseX, mouseY, tickDelta));
    }
}