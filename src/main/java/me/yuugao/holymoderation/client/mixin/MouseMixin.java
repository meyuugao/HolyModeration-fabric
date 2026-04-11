package me.yuugao.holymoderation.client.mixin;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.InputService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.MinecraftService;

import net.minecraft.client.Mouse;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class MouseMixin {
    @Shadow
    public abstract double getX();

    @Shadow
    public abstract double getY();

    @Inject(method = "onMouseButton", at = @At("HEAD"))
    private void onMouseButton(long window, int button, int action, int mods, CallbackInfo ci) {
        InputService inputService = ServiceLocator.getInputService();
        inputService.updateMouseButton(button, action);
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"))
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        MinecraftService minecraftService = ServiceLocator.getMinecraftService();
        double guiScale = minecraftService.getClient().getWindow().getScaleFactor();

        InputService inputService = ServiceLocator.getInputService();
        inputService.updateScroll(horizontal, vertical, getX() / guiScale, getY() / guiScale);
    }
}