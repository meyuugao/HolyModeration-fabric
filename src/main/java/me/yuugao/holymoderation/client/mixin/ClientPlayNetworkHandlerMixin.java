package me.yuugao.holymoderation.client.mixin;

import me.yuugao.holymoderation.client.di.DIAccessor;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.eventbus.EventBus;
import me.yuugao.holymoderation.client.util.service.eventbus.EventBusService;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.chat.CommandSendEvent;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.chat.MessageSendEvent;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.connection.ServerConnectEvent;
import me.yuugao.holymoderation.client.util.service.state.UserStateService;

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
        ClientPlayerEntity player = DIAccessor.getDI().get(MinecraftService.class).getPlayer();

        if (player != null) {
            ServerInfo serverInfo = player.networkHandler.getServerInfo();
            if (serverInfo != null) {
                DIAccessor.getDI().get(EventBusService.class).getEventBus().invokeEvent(new ServerConnectEvent(
                        serverInfo, DIAccessor.getDI().get(UserStateService.class).isConnected()));
            }
        }
    }

    @Inject(method = "sendChatMessage", at = @At("HEAD"), cancellable = true)
    public void sendChatMessage(String content, CallbackInfo ci) {
        EventBus eventBus = DIAccessor.getDI().get(EventBusService.class).getEventBus();

        MessageSendEvent event = new MessageSendEvent(content);

        eventBus.invokeEvent(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }

    @Inject(method = "sendChatCommand", at = @At("HEAD"), cancellable = true)
    public void sendCommand(String command, CallbackInfo ci) {
        EventBus eventBus = DIAccessor.getDI().get(EventBusService.class).getEventBus();

        CommandSendEvent event = new CommandSendEvent(command);

        eventBus.invokeEvent(event);

        if (event.isCancelled()) {
            ci.cancel();
        }
    }
}