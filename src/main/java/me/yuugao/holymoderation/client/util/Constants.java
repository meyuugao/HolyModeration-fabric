package me.yuugao.holymoderation.client.util;

import static me.yuugao.holymoderation.client.util.Colors.*;


import net.minecraft.client.MinecraftClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Constants {
    public static final MinecraftClient MC = MinecraftClient.getInstance();
    public static final ScheduledExecutorService SCHEDULER = Executors.newScheduledThreadPool(1);
    public static final Map<Integer, String> RANKS = new HashMap<>() {
        {
            put(1, AQUA + BOLD + "Стажёр");
            put(2, YELLOW + BOLD + "Мл. Сотрудник");
            put(3, GOLD + BOLD + "Сотрудник");
            put(4, GOLD + BOLD + "Сотрудник+"); //tip: уточни у nnt
            put(5, GOLD + BOLD + "Вед. Сотрудник");
            put(6, GRAY + BOLD + "Спектатор");
            put(7, RED + BOLD + "Ст. Сотрудник");
            put(8, RED + BOLD + "Админ");
            put(9, RED + BOLD + "Куратор");
        }
    };
}
