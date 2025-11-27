package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.MessageSendEvent;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class SettingsModule extends Module {
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

    private final String[] settingsCommands = {".textslist", ".textsclear", ".dupeip", ".autocopy", ".autotp", ".vanish",
            ".autoban", ".copy", ".fly", ".gm3", ".hacalerts", ".god", ".me", ".stats", ".textadd", ".textremove",
            ".textedit", ".setcopy", ".setmarker", ".setspydelay", ".sounds"};
    private final String[] settingsWithoutArguments = {".textslist", ".textsclear", ".dupeip", ".autocopy", ".autotp",
            ".vanish", ".autoban", ".copy", ".fly", ".gm3", ".hacalerts", ".god", ".me", ".stats", ".sounds"};
    private final String[] settingsWithOneArgument = {".textadd", ".textremove", ".setcopy", ".setmarker", ".setspydelay"};
    private final String[] settingsWithTwoArguments = {".textedit"};

    @Subscribe
    public void onMessageSend(MessageSendEvent event) {
        String message = event.getContent();
        String command = message.split(" ")[0];

        if (chatService.isArrayContains(settingsCommands, command)) {
            event.setCancelled(true);
            String[] messageSplit;
            String[] textsArray = configManager.getConfig().getTexts().isEmpty() ? new String[]{} : configManager.getConfig().getTexts().split("%%");
            if (chatService.isArrayContains(settingsWithoutArguments, command)) {
                switch (command) {
                    case (".textslist"): {
                        loggerService.printSuccess("Список ваших текстов:");
                        if (configManager.getConfig().getTexts().isEmpty()) {
                            loggerService.printError("У вас нет настроенных текстов.");
                        } else {
                            for (int i = 0; i < textsArray.length; i++) {
                                chatService.clientMessage(AQUA + BOLD + (i + 1) + WHITE + BOLD + ". " + textsArray[i]);
                            }
                        }
                        break;
                    }
                    case (".textsclear"): {
                        configManager.getConfig().setTexts(StringUtils.EMPTY);
                        loggerService.printSuccess("Вы успешно очистили все тексты.");
                        break;
                    }
                    case (".dupeip"): {
                        configManager.getConfig().setDupeIpEnabled(!configManager.getConfig().isDupeIpEnabled());
                        loggerService.printSuccess("Автоматический /dupeip " + (configManager.getConfig().isDupeIpEnabled() ? "включён" : "выключен") + ".");
                        break;
                    }
                    case (".autocopy"): {
                        configManager.getConfig().setAutoAnyDeskEnabled(!configManager.getConfig().isAutoAnyDeskEnabled());
                        loggerService.printSuccess("Автоматическое копирование айди AnyDesk " + (configManager.getConfig().isAutoAnyDeskEnabled() ? "включено" : "выключено") + ".");
                        break;
                    }
                    case (".autotp"): {
                        configManager.getConfig().setAutoTpEnabled(!configManager.getConfig().isAutoTpEnabled());
                        loggerService.printSuccess("Атоматический телепорт на /warp logo " + (configManager.getConfig().isAutoTpEnabled() ? "включён" : "выключен") + ".");
                        break;
                    }
                    case (".autoban"): {
                        configManager.getConfig().setAutoBanEnabled(!configManager.getConfig().isAutoBanEnabled());
                        loggerService.printSuccess("Автоманический бан игрока при ливе с проверки " + (configManager.getConfig().isAutoBanEnabled() ? "включён" : "выключен") + ".");
                        break;
                    }
                    case (".vanish"): {
                        configManager.getConfig().setAutoVanishEnabled(!configManager.getConfig().isAutoVanishEnabled());
                        loggerService.printSuccess("Автоматический ваниш " + (configManager.getConfig().isAutoVanishEnabled() ? "включён" : "выключен") + ".");
                        break;
                    }
                    case (".fly"): {
                        configManager.getConfig().setAutoFlyEnabled(!configManager.getConfig().isAutoFlyEnabled());
                        loggerService.printSuccess("Автоматический флай " + (configManager.getConfig().isAutoFlyEnabled() ? "включён" : "выключен") + ".");
                        break;
                    }
                    case (".gm3"): {
                        configManager.getConfig().setAutoGm3Enabled(!configManager.getConfig().isAutoGm3Enabled());
                        loggerService.printSuccess("Автоматический гм3 " + (configManager.getConfig().isAutoGm3Enabled() ? "включён" : "выключен") + ".");
                        break;
                    }
                    case (".hacalerts"): {
                        configManager.getConfig().setAutoHacAlertsEnabled(!configManager.getConfig().isAutoHacAlertsEnabled());
                        loggerService.printSuccess("Автоматический hac alerts " + (configManager.getConfig().isAutoHacAlertsEnabled() ? "включён" : "выключен") + ".");
                        break;
                    }
                    case (".god"): {
                        configManager.getConfig().setAutoGodEnabled(!configManager.getConfig().isAutoGodEnabled());
                        loggerService.printSuccess("Автоматический god " + (configManager.getConfig().isAutoGodEnabled() ? "включён" : "выключен") + ".");
                        break;
                    }
                    case (".me"): {
                        CompletableFuture.runAsync(() -> {
                            try {
                                Map<String, Object> profile = stateService.getJournalProfile();
                                loggerService.printSuccess("ИНФОРМАЦИЯ О МОДЕРАТОРЕ");
                                chatService.clientMessage(WHITE + BOLD + "Ваш никнейм: " + AQUA + BOLD + profile.get("nickname").toString());
                                chatService.clientMessage(WHITE + BOLD + "Ваша должность: " + RANKS.get((int) Double.parseDouble(profile.get("rank").toString())));
                                chatService.clientMessage("вк " + profile.get("fullname").toString() + " id" + (long) Double.parseDouble(profile.get("idVk").toString()));
                                chatService.clientMessage(WHITE + BOLD + "Ваш баланс: " + GREEN + BOLD + (int) Double.parseDouble(profile.get("neponyatki").toString()));
                                chatService.clientMessage(WHITE + BOLD + "Количество выговоров: " + RED + BOLD + (int) Double.parseDouble(profile.get("reprimands").toString()));
                                chatService.clientMessage(WHITE + BOLD + "Количество предупреждений: " + GOLD + BOLD + (int) Double.parseDouble(profile.get("warns").toString()));
                                chatService.clientMessage(WHITE + BOLD + "Режим: " + YELLOW + BOLD + profile.get("anarchyMode"));
                            } catch (Exception e) {
                                loggerService.printException("Исключение в SettingsManager/onMessageSend: " + e);
                            }
                        });
                        break;
                    }
                    case (".stats"): {
                        try {
                            Map<String, Object> stats = stateService.getJournalStats();
                            loggerService.printSuccess("СТАТИСТИКА МОДЕРАТОРА");
                            Map<String, Object> revisesAll = (Map<String, Object>) stats.get("revisesAll");
                            Map<String, Object> revisesMonth = (Map<String, Object>) stats.get("revisesMonth");
                            Map<String, Object> revisesWeek = (Map<String, Object>) stats.get("revisesWeek");
                            Map<String, Object> revisesToday = (Map<String, Object>) stats.get("revisesToday");
                            if (revisesAll != null && revisesMonth != null && revisesWeek != null && revisesToday != null) {
                                chatService.clientMessage(LIGHT_PURPLE + BOLD + "СТАТИСТИКА ПРОВЕРОК");
                                chatService.clientMessage(WHITE + BOLD + "Проверок за всё время: " +
                                        AQUA + BOLD + (int) Double.parseDouble(revisesAll.get("total").toString()) +
                                        " (лайт: " + (int) Double.parseDouble(revisesAll.get("lite").toString()) +
                                        ", лайт 1.20: " + (int) Double.parseDouble(revisesAll.get("lite120").toString()) +
                                        ", классик: " + (int) Double.parseDouble(revisesAll.get("classic").toString()) + ")");
                                chatService.clientMessage(WHITE + BOLD + "Проверок за последний месяц: " + AQUA + BOLD + (int) Double.parseDouble(revisesMonth.get("total").toString())
                                        + " (лайт: " + (int) Double.parseDouble(revisesMonth.get("lite").toString()) +
                                        ", лайт 1.20: " + (int) Double.parseDouble(revisesMonth.get("lite120").toString()) +
                                        ", классик: " + (int) Double.parseDouble(revisesMonth.get("classic").toString()) + ")");
                                chatService.clientMessage(WHITE + BOLD + "Проверок за последнюю неделю: " + AQUA + BOLD + (int) Double.parseDouble(revisesWeek.get("total").toString())
                                        + " (лайт: " + (int) Double.parseDouble(revisesWeek.get("lite").toString()) +
                                        ", лайт 1.20: " + (int) Double.parseDouble(revisesWeek.get("lite120").toString()) +
                                        ", классик: " + (int) Double.parseDouble(revisesWeek.get("classic").toString()) + ")");
                                chatService.clientMessage(WHITE + BOLD + "Проверок за сегодня: " + AQUA + BOLD + (int) Double.parseDouble(revisesToday.get("total").toString())
                                        + " (лайт: " + (int) Double.parseDouble(revisesToday.get("lite").toString()) +
                                        ", лайт 1.20: " + (int) Double.parseDouble(revisesToday.get("lite120").toString()) +
                                        ", классик: " + (int) Double.parseDouble(revisesToday.get("classic").toString()) + ")");
                            }
                            chatService.clientMessage(LIGHT_PURPLE + BOLD + "СТАТИСТИКА МУТОВ И ГАРАНТОВ");
                            chatService.clientMessage(WHITE + BOLD + "Мутов за всё время: " + AQUA + BOLD + (int) Double.parseDouble(stats.get("mutesAll").toString()));
                            chatService.clientMessage(WHITE + BOLD + "Мутов за последний месяц: " + AQUA + BOLD + (int) Double.parseDouble(stats.get("mutesMonth").toString()));
                            chatService.clientMessage(WHITE + BOLD + "Мутов за сегодня: " + AQUA + BOLD + (int) Double.parseDouble(stats.get("mutesToday").toString()));
                            chatService.clientMessage(WHITE + BOLD + "Гарантов за всё время: " + AQUA + BOLD + (int) Double.parseDouble(stats.get("gaurantsAll").toString()));
                            chatService.clientMessage(WHITE + BOLD + "Гарантов за последний месяц: " + AQUA + BOLD + (int) Double.parseDouble(stats.get("gaurantsMonth").toString()));
                            chatService.clientMessage(WHITE + BOLD + "Гарантов за сегодня: " + AQUA + BOLD + (int) Double.parseDouble(stats.get("gaurantsToday").toString()));
                        } catch (Exception e) {
                            loggerService.printException("Исключение в SettingsManager/onMessageSend: " + e);
                        }
                        break;
                    }
                    case (".copy"): {
                        configManager.getConfig().setCopyButtonEnabled(!configManager.getConfig().isCopyButtonEnabled());
                        loggerService.printSuccess("Кнопка копирования " + (configManager.getConfig().isCopyButtonEnabled() ? "включена" : "выключена") + ".");
                        break;
                    }
                    case (".sounds"): {
                        configManager.getConfig().setSoundsEnabled(!configManager.getConfig().isSoundsEnabled());
                        loggerService.printSuccess("Звуки мода " + (configManager.getConfig().isSoundsEnabled() ? "включены" : "выключены") + ".");
                        break;
                    }
                }
            } else if (chatService.isArrayContains(settingsWithOneArgument, command)) {
                messageSplit = message.split(" ", 2);
                switch (command) {
                    case (".textadd"): {
                        if (messageSplit.length == 1) {
                            loggerService.printError("Вы не указали текст.");
                            return;
                        }
                        String text = messageSplit[1].replace("&", "§");
                        if (text.contains("%%")) {
                            loggerService.printError("Текст не должен содержать '%%'.");
                            return;
                        }
                        configManager.getConfig().setTexts(configManager.getConfig().getTexts().isEmpty() ? text : configManager.getConfig().getTexts() + "%%" + text);
                        loggerService.printSuccess("Вы добавили новый текст.");
                        break;
                    }
                    case (".textremove"): {
                        if (configManager.getConfig().getTexts().isEmpty()) {
                            loggerService.printError("У вас нет настроенных текстов.");
                            return;
                        }
                        if (messageSplit.length == 1) {
                            loggerService.printError("Вы не указали номер текста.");
                            return;
                        }
                        String indexText = messageSplit[1];
                        if (!chatService.checkCorrectInt(indexText)) {
                            loggerService.printError("Некорректный номер текста.");
                            return;
                        }
                        int intIndex = Integer.parseInt(indexText) - 1;
                        if (intIndex >= textsArray.length || intIndex < 0) {
                            loggerService.printError("Элемента с таким номером в списке ваших текстов не существует.");
                            return;
                        }
                        ArrayList<String> textsArrayList = new ArrayList<>(Arrays.asList(textsArray));
                        textsArrayList.remove(intIndex);
                        configManager.getConfig().setTexts(StringUtils.EMPTY);
                        for (String s : textsArrayList) {
                            configManager.getConfig().setTexts(configManager.getConfig().getTexts().isEmpty() ? s : configManager.getConfig().getTexts() + "%%" + s);
                        }
                        loggerService.printSuccess("Вы удалили текст номер " + messageSplit[1] + AQUA + BOLD + ".");
                        break;
                    }
                    case (".setcopy"): {
                        if (messageSplit.length == 1) {
                            configManager.getConfig().setCopyButtonText("§f§l[§a§lcopy§f§l]");
                            loggerService.printSuccess("Текст кнопки был сброшен.");
                            return;
                        }
                        configManager.getConfig().setCopyButtonText(messageSplit[1].replace("&", "§"));
                        loggerService.printSuccess("Вы установили новый текст кнопки копирования.");
                        break;
                    }
                    case (".setmarker"): {
                        if (messageSplit.length == 1) {
                            configManager.getConfig().setPlayerMarker("§d§l[CHECK]");
                            loggerService.printSuccess("Текст метки был сброшен.");
                            return;
                        }
                        configManager.getConfig().setPlayerMarker(messageSplit[1].replace("&", "§"));
                        loggerService.printSuccess("Вы установили новый текст маркера.");
                        break;
                    }
                    case (".setspydelay"): {
                        if (messageSplit.length == 1) {
                            loggerService.printError("Вы не указали число.");
                            return;
                        }

                        String valueText = messageSplit[1];
                        if (!chatService.checkCorrectInt(valueText)) {
                            loggerService.printError("Некорректное число.");
                            return;
                        }
                        configManager.getConfig().setSpyDelay(Integer.parseInt(valueText));
                        loggerService.printSuccess("Вы установили новую задержку в .spy: " + configManager.getConfig().getSpyDelay() + ".");
                        break;
                    }
                }
            } else if (chatService.isArrayContains(settingsWithTwoArguments, command)) {
                messageSplit = message.split(" ", 3);
                switch (command) {
                    case (".textedit"): {
                        if (configManager.getConfig().getTexts().isEmpty()) {
                            loggerService.printError("У вас нет настроенных текстов.");
                            return;
                        }
                        if (messageSplit.length == 1) {
                            loggerService.printError("Вы не указали номер текста и новый текст.");
                            return;
                        }
                        String indexText = messageSplit[1];
                        if (!chatService.checkCorrectInt(indexText)) {
                            loggerService.printError("Некорректный номер текста.");
                            return;
                        }
                        int index = Integer.parseInt(indexText) - 1;
                        if (messageSplit.length == 2) {
                            loggerService.printError("Вы не указали новый текст.");
                            return;
                        }
                        if (index >= textsArray.length || index < 0) {
                            loggerService.printError("Элемента с таким номером в списке ваших текстов не существует.");
                            return;
                        }
                        String text = messageSplit[2].replace("&", "§");
                        if (text.contains("%%")) {
                            loggerService.printError("Текст не должен содержать '%%'.");
                            return;
                        }
                        String[] textsList = configManager.getConfig().getTexts().split("%%");
                        textsList[index] = text;
                        configManager.getConfig().setTexts(StringUtils.EMPTY);
                        for (String t : textsList) {
                            configManager.getConfig().setTexts(configManager.getConfig().getTexts().isEmpty() ? t : configManager.getConfig().getTexts() + "%%" + t);
                        }
                        loggerService.printSuccess("Вы изменили текст номер " + (index + 1) + ".");
                        break;
                    }
                }
            }
            configManager.saveCfg(configManager.getConfig());
        }
    }
}