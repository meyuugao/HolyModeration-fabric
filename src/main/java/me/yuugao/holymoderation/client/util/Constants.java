package me.yuugao.holymoderation.client.util;

import net.minecraft.client.MinecraftClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Constants {
    public static final MinecraftClient MC = MinecraftClient.getInstance();
    public static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);
    public static final Map<Integer, String> RANKS = new HashMap<>() {
        //tip: заполнить
    };
}
