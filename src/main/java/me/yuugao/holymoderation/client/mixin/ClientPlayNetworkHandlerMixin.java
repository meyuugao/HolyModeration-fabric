package me.yuugao.holymoderation.client.mixin;


import me.yuugao.holymoderation.client.eventbus.event.MessageModifyEvent;
import me.yuugao.holymoderation.client.eventbus.event.MessageReceiveEvent;
import me.yuugao.holymoderation.client.eventbus.event.MessageSendEvent;
import me.yuugao.holymoderation.client.eventbus.event.ServerConnectEvent;
import me.yuugao.holymoderation.client.util.service.ServiceLocator;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
    @Inject(method = "onGameJoin", at = @At("TAIL"))
    private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
        ClientPlayerEntity player = ServiceLocator.getMinecraftService().getPlayer();
        if (player != null) {
            ServerInfo serverInfo = player.networkHandler.getServerInfo();
            if (serverInfo != null) {
                ServiceLocator.getEventBus().invokeEvent(new ServerConnectEvent(serverInfo, ServiceLocator.getStateService().isConnected()));
            }
        }
    }

    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    private void onGameMessageModify(GameMessageS2CPacket packet, CallbackInfo ci) {
        MessageModifyEvent event = new MessageModifyEvent(packet.content(), packet.overlay());

        ServiceLocator.getEventBus().invokeEvent(event);

        ci.cancel();
        ServiceLocator.getMinecraftService().getClient().inGameHud.getChatHud().addMessage(event.getMessage());
    }

    @Inject(method = "onGameMessage", at = @At("TAIL"))
    private void onGameMessageReceive(GameMessageS2CPacket packet, CallbackInfo ci) {
        ServiceLocator.getEventBus().invokeEvent(new MessageReceiveEvent(packet.content()));
    }

    @Inject(method = "sendChatMessage", at = @At("TAIL"))
    private void sendChatMessage(String content, CallbackInfo ci) {
        ServiceLocator.getEventBus().invokeEvent(new MessageSendEvent(content));
    }
}