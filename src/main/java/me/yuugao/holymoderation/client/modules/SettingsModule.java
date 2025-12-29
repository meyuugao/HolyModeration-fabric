package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.CommandSendEvent;
import me.yuugao.holymoderation.client.util.serviceLocator.service.NotificationType;

import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import obfuscator.DontObf;
import obfuscator.ObfRule;

@DontObf(ObfRule.OBF_STRING)
public class SettingsModule extends Module {
    public static final Map<Integer, String> RANKS = new HashMap<>() {
        {
            put(1, AQUA + BOLD + "Стажёр");
            put(2, YELLOW + BOLD + "Мл. Сотрудник");
            put(3, GOLD + BOLD + "Сотрудник");
            put(4, GOLD + BOLD + "Сотрудник+");
            put(5, GOLD + BOLD + "Вед. Сотрудник");
            put(6, WHITE + BOLD + "Спектатор");
            put(7, RED + BOLD + "Ст. Сотрудник");
            put(8, RED + BOLD + "Админ");
            put(9, RED + BOLD + "Куратор");
        }
    };

    private final String[] settingsCommands = {
            "autoban", "autocopy", "autodupeip", "autofly", "autogm3", "autogod", "autoha", "autotp", "autovanish",
            "copy", "me", "setcopy", "setmarker", "setspydelay", "setsoundsvolume", "sounds", "stats",
            "textadd", "textedit", "textremove", "textsclear", "textslist"
    };

    private final String[] settingsWithoutArguments = {
            "autoban", "autocopy", "autodupeip", "autofly", "autogm3", "autogod", "autoha",
            "autotp", "autovanish", "copy", "me", "sounds", "stats", "textsclear", "textslist"
    };

    private final String[] settingsWithOneArgument = {
            "setcopy", "setmarker", "setspydelay", "setsoundsvolume", "textadd", "textremove"
    };

    private final String[] settingsWithTwoArguments = {
            "textedit"
    };


    @Subscribe
    public void onCommandSend(CommandSendEvent event) {
        String eventCommand = event.getCommand();
        String[] commandSplit = eventCommand.split(" ");
        if (!eventCommand.startsWith("hm") || commandSplit.length < 2) return;

        String command = commandSplit[1];

        if (serviceContext.getChatService().isArrayContains(settingsCommands, command)) {
            List<String> textsList = serviceContext.getConfigManager().getConfig().getTextsList();
            if (serviceContext.getChatService().isArrayContains(settingsWithoutArguments, command)) {
                switch (command) {
                    case ("textslist"): {
                        if (textsList.isEmpty()) {
                            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "У вас нет настроенных текстов.", 5f);
                        } else {
                            StringBuilder texts = new StringBuilder(StringUtils.EMPTY);
                            for (int i = 0; i < textsList.size(); i++) {
                                texts.append(AQUA + BOLD).append(i + 1).append(WHITE).append(". ").append(textsList.get(i));
                                if (i < textsList.size() - 1) texts.append("\n");
                            }
                            serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Список ваших текстов", texts.toString(), 5f);
                        }
                        break;
                    }
                    case ("textsclear"): {
                        textsList.clear();
                        serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Вы успешно очистили все тексты.", 5f);
                        break;
                    }
                    case ("autodupeip"): {
                        serviceContext.getConfigManager().getConfig().setDupeIpEnabled(!serviceContext.getConfigManager().getConfig().isDupeIpEnabled());
                        serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Автоматический /dupeip " + (serviceContext.getConfigManager().getConfig().isDupeIpEnabled() ? "включён" : "выключен") + ".", 5f);
                        break;
                    }
                    case ("autocopy"): {
                        serviceContext.getConfigManager().getConfig().setAutoAnyDeskEnabled(!serviceContext.getConfigManager().getConfig().isAutoAnyDeskEnabled());
                        serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Автоматическое копирование айди AnyDesk " + (serviceContext.getConfigManager().getConfig().isAutoAnyDeskEnabled() ? "включено" : "выключено") + ".", 5f);
                        break;
                    }
                    case ("autotp"): {
                        serviceContext.getConfigManager().getConfig().setAutoTpEnabled(!serviceContext.getConfigManager().getConfig().isAutoTpEnabled());
                        serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Атоматический телепорт на /warp logo " + (serviceContext.getConfigManager().getConfig().isAutoTpEnabled() ? "включён" : "выключен") + ".", 5f);
                        break;
                    }
                    case ("autoban"): {
                        serviceContext.getConfigManager().getConfig().setAutoBanEnabled(!serviceContext.getConfigManager().getConfig().isAutoBanEnabled());
                        serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Автоманический бан игрока при ливе с проверки " + (serviceContext.getConfigManager().getConfig().isAutoBanEnabled() ? "включён" : "выключен") + ".", 5f);
                        break;
                    }
                    case ("autovanish"): {
                        serviceContext.getConfigManager().getConfig().setAutoVanishEnabled(!serviceContext.getConfigManager().getConfig().isAutoVanishEnabled());
                        serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Автоматический ваниш " + (serviceContext.getConfigManager().getConfig().isAutoVanishEnabled() ? "включён" : "выключен") + ".", 5f);
                        break;
                    }
                    case ("autofly"): {
                        serviceContext.getConfigManager().getConfig().setAutoFlyEnabled(!serviceContext.getConfigManager().getConfig().isAutoFlyEnabled());
                        serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Автоматический флай " + (serviceContext.getConfigManager().getConfig().isAutoFlyEnabled() ? "включён" : "выключен") + ".", 5f);
                        break;
                    }
                    case ("autogm3"): {
                        serviceContext.getConfigManager().getConfig().setAutoGm3Enabled(!serviceContext.getConfigManager().getConfig().isAutoGm3Enabled());
                        serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Автоматический гм3 " + (serviceContext.getConfigManager().getConfig().isAutoGm3Enabled() ? "включён" : "выключен") + ".", 5f);
                        break;
                    }
                    case ("autoha"): {
                        serviceContext.getConfigManager().getConfig().setAutoHacAlertsEnabled(!serviceContext.getConfigManager().getConfig().isAutoHacAlertsEnabled());
                        serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Автоматический hac alerts " + (serviceContext.getConfigManager().getConfig().isAutoHacAlertsEnabled() ? "включён" : "выключен") + ".", 5f);
                        break;
                    }
                    case ("autogod"): {
                        serviceContext.getConfigManager().getConfig().setAutoGodEnabled(!serviceContext.getConfigManager().getConfig().isAutoGodEnabled());
                        serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Автоматический god " + (serviceContext.getConfigManager().getConfig().isAutoGodEnabled() ? "включён" : "выключен") + ".", 5f);
                        break;
                    }
                    case ("me"): {
                        CompletableFuture.runAsync(() -> {
                            try {
                                Map<String, Object> profile = serviceContext.getStateService().getJournalProfile();

                                String texts = WHITE + "Ваш никнейм: " + AQUA + BOLD + profile.get("nickname").toString() +
                                        "\n" +
                                        WHITE + "Ваша должность: " + RANKS.get((int) Double.parseDouble(profile.get("rank").toString())) +
                                        "\n" +
                                        WHITE + "Ваш вк: " + AQUA + BOLD + profile.get("fullname").toString() +
                                        " (" + WHITE + "vk.com/id" + (long) Double.parseDouble(profile.get("idVk").toString()) + AQUA + BOLD + ")" +
                                        "\n" +
                                        WHITE + "Ваш баланс: " + GREEN + BOLD + (int) Double.parseDouble(profile.get("neponyatki").toString()) +
                                        "\n" +
                                        WHITE + "Количество выговоров: " + RED + BOLD + (int) Double.parseDouble(profile.get("reprimands").toString()) +
                                        "\n" +
                                        WHITE + "Количество предупреждений: " + GOLD + BOLD + (int) Double.parseDouble(profile.get("warns").toString()) +
                                        "\n" +
                                        WHITE + "Режим: " + YELLOW + BOLD + profile.get("anarchyMode");

                                serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "ИНФОРМАЦИЯ О МОДЕРАТОРЕ", texts, 10f);
                            } catch (Exception e) {
                                serviceContext.getNotificationService().addNotification(NotificationType.EXCEPTION, DARK_AQUA + BOLD + "Исключение", "Исключение в SettingsManager/onMessageSend: " + DARK_RED + e, 5f);
                            }
                        });
                        break;
                    }
                    case ("stats"): {
                        try {
                            Map<String, Object> stats = serviceContext.getStateService().getJournalStats();
                            Map<String, Object> revisesAll = (Map<String, Object>) stats.get("revisesAll");
                            Map<String, Object> revisesMonth = (Map<String, Object>) stats.get("revisesMonth");
                            Map<String, Object> revisesWeek = (Map<String, Object>) stats.get("revisesWeek");
                            Map<String, Object> revisesToday = (Map<String, Object>) stats.get("revisesToday");

                            String texts = "";

                            if (revisesAll != null && revisesMonth != null && revisesWeek != null && revisesToday != null) {
                                texts =
                                        LIGHT_PURPLE + BOLD + "СТАТИСТИКА ПРОВЕРОК" +
                                                "\n" +
                                                WHITE + "Проверок за всё время: " + AQUA + BOLD + (int) Double.parseDouble(revisesAll.get("total").toString()) +
                                                " (лайт: " + (int) Double.parseDouble(revisesAll.get("lite").toString()) +
                                                ", лайт 1.20: " + (int) Double.parseDouble(revisesAll.get("lite120").toString()) +
                                                ", классик: " + (int) Double.parseDouble(revisesAll.get("classic").toString()) + ")" +
                                                "\n" +
                                                WHITE + "Проверок за последний месяц: " + AQUA + BOLD + (int) Double.parseDouble(revisesMonth.get("total").toString()) +
                                                " (лайт: " + (int) Double.parseDouble(revisesMonth.get("lite").toString()) +
                                                ", лайт 1.20: " + (int) Double.parseDouble(revisesMonth.get("lite120").toString()) +
                                                ", классик: " + (int) Double.parseDouble(revisesMonth.get("classic").toString()) + ")" +
                                                "\n" +
                                                WHITE + "Проверок за последнюю неделю: " + AQUA + BOLD + (int) Double.parseDouble(revisesWeek.get("total").toString()) +
                                                " (лайт: " + (int) Double.parseDouble(revisesWeek.get("lite").toString()) +
                                                ", лайт 1.20: " + (int) Double.parseDouble(revisesWeek.get("lite120").toString()) +
                                                ", классик: " + (int) Double.parseDouble(revisesWeek.get("classic").toString()) + ")" +
                                                "\n" +
                                                WHITE + "Проверок за сегодня: " + AQUA + BOLD + (int) Double.parseDouble(revisesToday.get("total").toString()) +
                                                " (лайт: " + (int) Double.parseDouble(revisesToday.get("lite").toString()) +
                                                ", лайт 1.20: " + (int) Double.parseDouble(revisesToday.get("lite120").toString()) +
                                                ", классик: " + (int) Double.parseDouble(revisesToday.get("classic").toString()) + ")" +
                                                "\n";
                            }

                            texts +=
                                    LIGHT_PURPLE + BOLD + "СТАТИСТИКА МУТОВ И ГАРАНТОВ" +
                                            "\n" +
                                            WHITE + "Мутов за всё время: " + AQUA + BOLD + (int) Double.parseDouble(stats.get("mutesAll").toString()) +
                                            "\n" +
                                            WHITE + "Мутов за последний месяц: " + AQUA + BOLD + (int) Double.parseDouble(stats.get("mutesMonth").toString()) +
                                            "\n" +
                                            WHITE + "Мутов за сегодня: " + AQUA + BOLD + (int) Double.parseDouble(stats.get("mutesToday").toString()) +
                                            "\n" +
                                            WHITE + "Гарантов за всё время: " + AQUA + BOLD + (int) Double.parseDouble(stats.get("gaurantsAll").toString()) +
                                            "\n" +
                                            WHITE + "Гарантов за последний месяц: " + AQUA + BOLD + (int) Double.parseDouble(stats.get("gaurantsMonth").toString()) +
                                            "\n" +
                                            WHITE + "Гарантов за сегодня: " + AQUA + BOLD + (int) Double.parseDouble(stats.get("gaurantsToday").toString());

                            serviceContext.getNotificationService().addNotification(
                                    NotificationType.SUCCESS,
                                    GREEN + BOLD + "СТАТИСТИКА МОДЕРАТОРА",
                                    texts,
                                    10f
                            );
                        } catch (Exception e) {
                            serviceContext.getNotificationService().addNotification(NotificationType.EXCEPTION, DARK_AQUA + BOLD + "Исключение", "Исключение в SettingsManager/onMessageSend: " + DARK_RED + e, 5f);
                        }
                        break;
                    }
                    case ("spyfrz"): {

                    }
                    case ("copy"): {
                        serviceContext.getConfigManager().getConfig().setCopyButtonEnabled(!serviceContext.getConfigManager().getConfig().isCopyButtonEnabled());
                        serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Кнопка копирования " + (serviceContext.getConfigManager().getConfig().isCopyButtonEnabled() ? "включена" : "выключена") + ".", 5f);
                        break;
                    }
                    case ("sounds"): {
                        serviceContext.getConfigManager().getConfig().setSoundsEnabled(!serviceContext.getConfigManager().getConfig().isSoundsEnabled());
                        serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Звуки мода " + (serviceContext.getConfigManager().getConfig().isSoundsEnabled() ? "включены" : "выключены") + ".", 5f);
                        break;
                    }
                }
            } else if (serviceContext.getChatService().isArrayContains(settingsWithOneArgument, command)) {
                commandSplit = eventCommand.split(" ", 3);
                switch (command) {
                    case ("textadd"): {
                        if (commandSplit.length == 2) {
                            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы не указали текст.", 5f);
                            return;
                        }
                        String text = commandSplit[2].replace("&", "§");
                        textsList.add(text);
                        serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Вы добавили новый текст.", 5f);
                        break;
                    }
                    case ("textremove"): {
                        if (textsList.isEmpty()) {
                            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "У вас нет настроенных текстов.", 5f);
                            return;
                        }
                        if (commandSplit.length == 2) {
                            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы не указали номер текста.", 5f);
                            return;
                        }
                        String indexText = commandSplit[2];
                        if (!serviceContext.getChatService().checkCorrectInt(indexText)) {
                            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Некорректный номер текста.", 5f);
                            return;
                        }
                        int intIndex = Integer.parseInt(indexText) - 1;
                        if (intIndex >= textsList.size() || intIndex < 0) {
                            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Элемента с таким номером в списке ваших текстов не существует.", 5f);
                            return;
                        }
                        textsList.remove(intIndex);
                        serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Вы удалили текст номер " + commandSplit[2] + AQUA + BOLD + ".", 5f);
                        break;
                    }
                    case ("setcopy"): {
                        if (commandSplit.length == 2) {
                            serviceContext.getConfigManager().getConfig().setCopyButtonText("§f§l[§a§lcopy§f§l]");
                            serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Текст кнопки был сброшен.", 5f);
                        } else {
                            serviceContext.getConfigManager().getConfig().setCopyButtonText(commandSplit[2].replace("&", "§"));
                            serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Вы установили новый текст кнопки копирования.", 5f);
                        }
                        break;
                    }
                    case ("setmarker"): {
                        if (commandSplit.length == 2) {
                            serviceContext.getConfigManager().getConfig().setPlayerMarker("§d§l[CHECK]");
                            serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Текст метки был сброшен.", 5f);
                        } else {
                            serviceContext.getConfigManager().getConfig().setPlayerMarker(commandSplit[2].replace("&", "§"));
                            serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Вы установили новый текст маркера.", 5f);
                        }
                        break;
                    }
                    case ("setspydelay"): {
                        if (commandSplit.length == 2) {
                            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы не указали число.", 5f);
                            return;
                        }

                        String valueText = commandSplit[2];
                        if (!serviceContext.getChatService().checkCorrectInt(valueText)) {
                            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Некорректное число.", 5f);
                            return;
                        }
                        serviceContext.getConfigManager().getConfig().setSpyDelay(Integer.parseInt(valueText));
                        serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Вы установили новую задержку в spy: " + serviceContext.getConfigManager().getConfig().getSpyDelay() + ".", 5f);
                        break;
                    }
                    case ("setsoundsvolume"): {
                        if (commandSplit.length == 2) {
                            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы не указали число.", 5f);
                            return;
                        }

                        String valueText = commandSplit[2];
                        if (!serviceContext.getChatService().checkCorrectInt(valueText)) {
                            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Некорректное число.", 5f);
                            return;
                        }
                        serviceContext.getConfigManager().getConfig().setSoundsVolume(Integer.parseInt(valueText));
                        serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Вы установили новую громкость звуков: " + serviceContext.getConfigManager().getConfig().getSoundsVolume() + ".", 5f);
                        break;
                    }
                }
            } else if (serviceContext.getChatService().isArrayContains(settingsWithTwoArguments, command)) {
                commandSplit = eventCommand.split(" ", 4);
                switch (command) {
                    case ("textedit"): {
                        if (textsList.isEmpty()) {
                            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "У вас нет настроенных текстов.", 5f);
                            return;
                        }
                        if (commandSplit.length == 2) {
                            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы не указали номер текста и новый текст.", 5f);
                            return;
                        }
                        String indexText = commandSplit[2];
                        if (!serviceContext.getChatService().checkCorrectInt(indexText)) {
                            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Некорректный номер текста.", 5f);
                            return;
                        }
                        int index = Integer.parseInt(indexText) - 1;
                        if (commandSplit.length == 3) {
                            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы не указали новый текст.", 5f);
                            return;
                        }
                        if (index >= textsList.size() || index < 0) {
                            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Элемента с таким номером в списке ваших текстов не существует.", 5f);
                            return;
                        }
                        String text = commandSplit[3].replace("&", "§");
                        if (text.contains("%%")) {
                            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Текст не должен содержать '%%'.", 5f);
                            return;
                        }
                        textsList.set(index, text);
                        serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Вы изменили текст номер " + (index + 1) + ".", 5f);
                        break;
                    }
                }
            }
            serviceContext.getConfigManager().saveCfg(serviceContext.getConfigManager().getConfig());
        }
    }
}