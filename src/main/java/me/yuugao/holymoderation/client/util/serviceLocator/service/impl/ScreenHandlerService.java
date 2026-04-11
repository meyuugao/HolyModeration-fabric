package me.yuugao.holymoderation.client.util.serviceLocator.service.impl;

import me.yuugao.holymoderation.client.util.serviceLocator.service.Service;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class ScreenHandlerService extends Service {
    public synchronized void clickSlot(int slotIndex, int mouseButton, SlotActionType actionType, @NotNull ClientPlayerEntity player) {
        ScreenHandler handler = player.currentScreenHandler;

        if (handler == null) return;

        Int2ObjectOpenHashMap<ItemStack> before = captureState(handler);
        handler.onSlotClick(slotIndex, mouseButton, actionType, player);
        sendPacket(handler, slotIndex, mouseButton, actionType, before, player);
    }

    private Int2ObjectOpenHashMap<ItemStack> captureState(ScreenHandler handler) {
        Int2ObjectOpenHashMap<ItemStack> state = new Int2ObjectOpenHashMap<>();
        for (Slot slot : handler.slots) {
            state.put(slot.id, slot.getStack().copy());
        }
        return state;
    }

    private void sendPacket(ScreenHandler handler, int slotIndex, int button, SlotActionType actionType,
                            Int2ObjectOpenHashMap<ItemStack> before, ClientPlayerEntity player) {
        Int2ObjectOpenHashMap<ItemStack> modified = new Int2ObjectOpenHashMap<>();
        for (Slot slot : handler.slots) {
            ItemStack now = slot.getStack();
            ItemStack was = before.get(slot.id);

            if (!ItemStack.areEqual(was, now)) {
                modified.put(slot.id, now.copy());
            }
        }

        ClickSlotC2SPacket packet = new ClickSlotC2SPacket(
                handler.syncId,
                handler.getRevision(),
                slotIndex,
                button,
                actionType,
                before.get(slotIndex),
                modified
        );

        player.networkHandler.sendPacket(packet);
    }
}