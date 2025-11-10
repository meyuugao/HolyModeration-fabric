package me.yuugao.holymoderation.client.mixin;

import me.yuugao.holymoderation.client.HolyModerationClient;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;)V", at = @At("TAIL"))
    private void onDisconnect(Screen screen, CallbackInfo ci) {
        HolyModerationClient.STATE_SERVICE.connected = false;
    }
}