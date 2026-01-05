package me.yuugao.holymoderation.client.mixin;

import me.yuugao.holymoderation.client.eventbus.event.MessageReceiveEvent;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MessageHandler.class)
public abstract class MessageHandlerMixin {
    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    public void onGameMessage(Text message, boolean overlay, CallbackInfo ci) {
        MessageReceiveEvent event = new MessageReceiveEvent(message);

        ServiceLocator.getEventBus().invokeEvent(event);

        ci.cancel();
        if (!event.isCancelled()) {
            ServiceLocator.getMinecraftService().getClient().inGameHud.getChatHud().addMessage(event.getMessage());
        }
    }
}