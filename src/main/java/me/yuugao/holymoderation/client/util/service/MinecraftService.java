package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.di.annotations.Singleton;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;

import org.jetbrains.annotations.Nullable;

@Singleton
public class MinecraftService {
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