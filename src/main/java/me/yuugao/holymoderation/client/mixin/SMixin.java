package me.yuugao.holymoderation.client.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public class SMixin {
    @Inject(method = "render", at = @At("HEAD"))
    public void a(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        new Exception().printStackTrace();
    }
}
