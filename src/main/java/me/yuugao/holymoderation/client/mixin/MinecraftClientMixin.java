package me.yuugao.holymoderation.client.mixin;

import me.yuugao.holymoderation.client.di.DIAccessor;
import me.yuugao.holymoderation.client.util.service.LoggerService;
import me.yuugao.holymoderation.client.util.service.Render2DService;
import me.yuugao.holymoderation.client.util.service.eventbus.EventBus;
import me.yuugao.holymoderation.client.util.service.eventbus.EventBusService;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.connection.ServerDisconnectEvent;

import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    public abstract ResourceManager getResourceManager();

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("TAIL"))
    public void onDisconnect(CallbackInfo ci) {
        EventBus eventBus = DIAccessor.getDI().get(EventBusService.class).getEventBus();

        eventBus.invokeEvent(new ServerDisconnectEvent());
    }

    @Inject(method = "onInitFinished", at = @At("TAIL"))
    private void onInitFinished(CallbackInfo ci) {
        Render2DService render2DService = DIAccessor.getDI().get(Render2DService.class);
        LoggerService loggerService = DIAccessor.getDI().get(LoggerService.class);

        render2DService.initializeShaders(getResourceManager());
        loggerService.info("HolyModeration has been initialized.");
    }
}