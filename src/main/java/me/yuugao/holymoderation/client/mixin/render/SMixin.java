package me.yuugao.holymoderation.client.mixin.render;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.network.ClientPlayerEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Objects;

@Mixin(HandledScreen.class)
public class SMixin {
    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void a(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button == 1) {
            ClientPlayerEntity player = ServiceLocator.getMinecraftService().getPlayer();
            if (player != null) {
                System.out.println(Objects.requireNonNull(ServiceLocator.getMinecraftService().getClient().currentScreen).getTitle().getString().equals("Жалобы на игроков"));
            }
        }
    }
}