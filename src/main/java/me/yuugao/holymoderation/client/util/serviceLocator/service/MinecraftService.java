package me.yuugao.holymoderation.client.util.serviceLocator.service;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;

import org.jetbrains.annotations.Nullable;

public class MinecraftService extends Service {
    public MinecraftClient getClient() {
        return MinecraftClient.getInstance();
    }

    @Nullable
    public ClientPlayerEntity getPlayer() {
        return MinecraftClient.getInstance().player;
    }

    @Nullable
    public ClientWorld getWorld() {
        return MinecraftClient.getInstance().world;
    }
}