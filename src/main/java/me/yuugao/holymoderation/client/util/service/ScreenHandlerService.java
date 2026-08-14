package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.screen.slot.SlotActionType;

import org.jetbrains.annotations.NotNull;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class ScreenHandlerService {
    private final MinecraftService minecraftService;

    public synchronized void clickSlot(int slotIndex, int mouseButton, SlotActionType actionType, @NotNull ClientPlayerEntity player) {
        if (player.currentScreenHandler == null) return;
        if (minecraftService.getClient().interactionManager == null) return;

        minecraftService.getClient().interactionManager.clickSlot(
                player.currentScreenHandler.syncId, slotIndex, mouseButton, actionType, player);
    }
}
