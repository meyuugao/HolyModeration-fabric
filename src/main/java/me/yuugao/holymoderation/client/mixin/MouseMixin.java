package me.yuugao.holymoderation.client.mixin;

import me.yuugao.holymoderation.client.di.DIAccessor;
import me.yuugao.holymoderation.client.util.service.InputService;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.eventbus.EventBus;
import me.yuugao.holymoderation.client.util.service.eventbus.EventBusService;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.input.MouseClickEvent;

import net.minecraft.client.Mouse;
import net.minecraft.client.input.MouseInput;

import org.lwjgl.glfw.GLFW;
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
    private void onMouseButton(long window, MouseInput mouseInput, int action, CallbackInfo ci) {
        int button = mouseInput.button();
        InputService inputService = DIAccessor.getDI().get(InputService.class);
        inputService.updateMouseButton(button, action);

        if (action == GLFW.GLFW_PRESS) {
            MinecraftService minecraftService = DIAccessor.getDI().get(MinecraftService.class);
            double guiScale = minecraftService.getClient().getWindow().getScaleFactor();
            int x = (int) (getX() / guiScale);
            int y = (int) (getY() / guiScale);

            EventBus eventBus = DIAccessor.getDI().get(EventBusService.class).getEventBus();
            eventBus.invokeEvent(new MouseClickEvent(button, x, y));
        }
    }

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        MinecraftService minecraftService = DIAccessor.getDI().get(MinecraftService.class);
        InputService inputService = DIAccessor.getDI().get(InputService.class);

        double guiScale = minecraftService.getClient().getWindow().getScaleFactor();

        if (inputService.updateScroll(horizontal, vertical, getX() / guiScale, getY() / guiScale)) {
            ci.cancel();
        }
    }
}