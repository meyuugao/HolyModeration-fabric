package me.yuugao.holymoderation.client.mixin;

import me.yuugao.holymoderation.client.eventbus.EventBus;
import me.yuugao.holymoderation.client.eventbus.event.impl.connection.ServerDisconnectEvent;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.LoggerService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.Render2DService;

import net.minecraft.client.MinecraftClient;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("TAIL"))
    public void onDisconnect(CallbackInfo ci) {
        EventBus eventBus = ServiceLocator.getEventBus();

        eventBus.invokeEvent(new ServerDisconnectEvent());
    }

    @Inject(method = "onInitFinished", at = @At("TAIL"))
    private void onInitFinished(CallbackInfo ci) {
        Render2DService render2DService = ServiceLocator.getRender2DService();
        LoggerService loggerService = ServiceLocator.getLoggerService();

        render2DService.initializeShaders();
        loggerService.info("HolyModeration has been initialized.");
    }
}