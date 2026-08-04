package me.yuugao.holymoderation.client.mixin;

import me.yuugao.holymoderation.client.di.DIAccessor;
import me.yuugao.holymoderation.client.util.service.InputService;
import me.yuugao.holymoderation.client.util.service.MinecraftService;

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
        InputService inputService = DIAccessor.getDI().get(InputService.class);
        inputService.updateMouseButton(button, action);
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"))
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        MinecraftService minecraftService = DIAccessor.getDI().get(MinecraftService.class);
        InputService inputService = DIAccessor.getDI().get(InputService.class);

        double guiScale = minecraftService.getClient().getWindow().getScaleFactor();

        inputService.updateScroll(horizontal, vertical, getX() / guiScale, getY() / guiScale);
    }
}