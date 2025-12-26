package me.yuugao.holymoderation.client.mixin;


import me.yuugao.holymoderation.client.eventbus.event.MessageReceiveEvent;
import me.yuugao.holymoderation.client.eventbus.event.MessageSendEvent;
import me.yuugao.holymoderation.client.eventbus.event.ServerConnectEvent;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onGameJoin(CallbackInfo ci) {
        ClientPlayerEntity player = ServiceLocator.getMinecraftService().getPlayer();
        if (player != null) {
            ServerInfo serverInfo = player.networkHandler.getServerInfo();
            if (serverInfo != null) {
                ServiceLocator.getEventBus().invokeEvent(new ServerConnectEvent(serverInfo, ServiceLocator.getStateService().isConnected()));
            }
        }
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    private void sendChatMessage(String content, CallbackInfo ci) {
        MessageSendEvent event = new MessageSendEvent(content);

        ServiceLocator.getEventBus().invokeEvent(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}