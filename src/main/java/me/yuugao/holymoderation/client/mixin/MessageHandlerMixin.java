package me.yuugao.holymoderation.client.mixin;

import me.yuugao.holymoderation.client.di.DIAccessor;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.eventbus.EventBus;
import me.yuugao.holymoderation.client.util.service.eventbus.EventBusService;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.chat.MessageReceiveEvent;

import net.minecraft.client.network.message.MessageHandler;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MessageHandler.class)
public class MessageHandlerMixin {
    @Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
    public void onGameMessage(Text message, boolean overlay, CallbackInfo ci) {
        EventBus eventBus = DIAccessor.getDI().get(EventBusService.class).getEventBus();
        MinecraftService minecraftService = DIAccessor.getDI().get(MinecraftService.class);

        MessageReceiveEvent event = new MessageReceiveEvent(message);

        eventBus.invokeEvent(event);

        ci.cancel();
        if (!event.isCancelled()) {
            minecraftService.getClient().inGameHud.getChatHud().addMessage(event.getMessage());
        }
    }
}