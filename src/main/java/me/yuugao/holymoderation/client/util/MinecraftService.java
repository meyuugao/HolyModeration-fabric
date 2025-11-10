package me.yuugao.holymoderation.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;

public class MinecraftService {
    public static MinecraftClient getInstance() {
        return MinecraftClient.getInstance();
    }

    public static ClientPlayerEntity getPlayer() {
        return MinecraftClient.getInstance().player;
    }

    public static ClientWorld getWorld() {
        return MinecraftClient.getInstance().world;
    }
}
