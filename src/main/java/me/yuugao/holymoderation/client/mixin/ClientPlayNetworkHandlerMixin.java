package me.yuugao.holymoderation.client.mixin;


import me.yuugao.holymoderation.client.eventbus.EventBus;
import me.yuugao.holymoderation.client.eventbus.event.CommandSendEvent;
import me.yuugao.holymoderation.client.eventbus.event.MessageSendEvent;
import me.yuugao.holymoderation.client.eventbus.event.ServerConnectEvent;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;
import me.yuugao.holymoderation.client.util.serviceLocator.service.MinecraftService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.StateService;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ServerInfo;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onGameJoin", at = @At("TAIL"))
    public void onGameJoin(CallbackInfo ci) {
        MinecraftService minecraftService = ServiceLocator.getMinecraftService();
        EventBus eventBus = ServiceLocator.getEventBus();
        StateService stateService = ServiceLocator.getStateService();

        ClientPlayerEntity player = minecraftService.getPlayer();

        if (player != null) {
            ServerInfo serverInfo = player.networkHandler.getServerInfo();
            if (serverInfo != null) {
                eventBus.invokeEvent(new ServerConnectEvent(serverInfo, stateService.isConnected()));
            }
        }
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    public void sendChatMessage(String content, CallbackInfo ci) {
        EventBus eventBus = ServiceLocator.getEventBus();

        MessageSendEvent event = new MessageSendEvent(content);

        eventBus.invokeEvent(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "sendChatCommand", at = @At("HEAD"), cancellable = true)
    public void sendCommand(String command, CallbackInfo ci) {
        EventBus eventBus = ServiceLocator.getEventBus();

        CommandSendEvent event = new CommandSendEvent(command);

        eventBus.invokeEvent(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}