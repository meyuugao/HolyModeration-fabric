package me.yuugao.holymoderation.client.mixin.render;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.Text;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(HandledScreen.class)
public class SMixin {
    @Inject(method = "mouseClicked", at = @At("HEAD"))
    private void a(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (button == 1) {
            ClientPlayerEntity player = ServiceLocator.getMinecraftService().getPlayer();
            if (player != null) {
                Screen screen = ServiceLocator.getMinecraftService().getClient().currentScreen;
                if (screen instanceof GenericContainerScreen genericContainerScreen
                        && screen.getTitle().getString().equals("Жалобы на игроков")) {
                    genericContainerScreen.getScreenHandler().slots.forEach(slot -> {
                        List<Text> tooltip = slot.getStack().getTooltip(player, TooltipContext.BASIC);
                        if (tooltip.size() > 10) {
                            System.out.println(tooltip.get(10).getString());
                        }
                    });
                }
            }
        }
    }
}