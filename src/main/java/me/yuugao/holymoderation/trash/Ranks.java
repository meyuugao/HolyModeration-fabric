package me.yuugao.holymoderation.trash;

import static me.yuugao.holymoderation.client.util.Colors.*;


import java.util.HashMap;
import java.util.Map;

public class Ranks {
    public static final Map<Integer, String> RANKS = new HashMap<>() {
        {
            put(1, AQUA + BOLD + "Стажёр");
            put(2, YELLOW + BOLD + "Мл. Сотрудник");
            put(3, GOLD + BOLD + "Сотрудник");
            put(4, GOLD + BOLD + "Сотрудник+");
            put(5, GOLD + BOLD + "Вед. Сотрудник");
            put(6, GRAY + BOLD + "Спектатор");
            put(7, RED + BOLD + "Ст. Сотрудник");
            put(8, RED + BOLD + "Админ");
            put(9, RED + BOLD + "Куратор");
        }
    };
}