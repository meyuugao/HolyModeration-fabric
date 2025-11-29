package me.yuugao.holymoderation.client.mixin;

import me.yuugao.holymoderation.client.eventbus.event.ServerDisconnectEvent;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;
import me.yuugao.holymoderation.client.util.serviceLocator.service.Service;

import net.minecraft.client.MinecraftClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("TAIL"))
    private void onDisconnect(CallbackInfo ci) {
        ServiceLocator.getEventBus().invokeEvent(new ServerDisconnectEvent());
    }

    @Inject(method = "onInitFinished", at = @At("TAIL"))
    private void onInitFinished(CallbackInfo ci) {
        ServiceLocator.getRender2DService().initializeShaders();
        ServiceLocator.getLoggerService().getLogger().info("Shaders has been initialized");
    }
}