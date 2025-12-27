package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.MessageSendEvent;

import net.minecraft.text.Text;

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

        if (serviceContext.getChatService().isArrayContains(settingsCommands, command)) {
            event.setCancelled(true);
            String[] messageSplit;
            String[] textsArray = serviceContext.getConfigManager().getConfig().getTexts().isEmpty() ? new String[]{} : serviceContext.getConfigManager().getConfig().getTexts().split("%%");
            if (serviceContext.getChatService().isArrayContains(settingsWithoutArguments, command)) {
                switch (command) {
                    case (".textslist"): {
                        serviceContext.getLoggerService().printSuccess("Список ваших текстов:");
                        if (serviceContext.getConfigManager().getConfig().getTexts().isEmpty()) {
                            serviceContext.getLoggerService().printError("У вас нет настроенных текстов.");
                        } else {
                            for (int i = 0; i < textsArray.length; i++) {
                                serviceContext.getChatService().clientMessage(AQUA + BOLD + (i + 1) + WHITE + BOLD + ". " + textsArray[i]);
                            }
                        }
                        break;
                    }
                    case (".textsclear"): {
                        serviceContext.getConfigManager().getConfig().setTexts(StringUtils.EMPTY);
                        serviceContext.getLoggerService().printSuccess("Вы успешно очистили все тексты.");
                        break;
                    }
                    case (".dupeip"): {
                        serviceContext.getConfigManager().getConfig().setDupeIpEnabled(!serviceContext.getConfigManager().getConfig().isDupeIpEnabled());
                        serviceContext.getLoggerService().printSuccess("Автоматический /dupeip " + (serviceContext.getConfigManager().getConfig().isDupeIpEnabled() ? "включён" : "выключен") + ".");
                        break;
                    }
                    case (".autocopy"): {
                        serviceContext.getConfigManager().getConfig().setAutoAnyDeskEnabled(!serviceContext.getConfigManager().getConfig().isAutoAnyDeskEnabled());
                        serviceContext.getLoggerService().printSuccess("Автоматическое копирование айди AnyDesk " + (serviceContext.getConfigManager().getConfig().isAutoAnyDeskEnabled() ? "включено" : "выключено") + ".");
                        break;
                    }
                    case (".autotp"): {
                        serviceContext.getConfigManager().getConfig().setAutoTpEnabled(!serviceContext.getConfigManager().getConfig().isAutoTpEnabled());
                        serviceContext.getLoggerService().printSuccess("Атоматический телепорт на /warp logo " + (serviceContext.getConfigManager().getConfig().isAutoTpEnabled() ? "включён" : "выключен") + ".");
                        break;
                    }
                    case (".autoban"): {
                        serviceContext.getConfigManager().getConfig().setAutoBanEnabled(!serviceContext.getConfigManager().getConfig().isAutoBanEnabled());
                        serviceContext.getLoggerService().printSuccess("Автоманический бан игрока при ливе с проверки " + (serviceContext.getConfigManager().getConfig().isAutoBanEnabled() ? "включён" : "выключен") + ".");
                        break;
                    }
                    case (".vanish"): {
                        serviceContext.getConfigManager().getConfig().setAutoVanishEnabled(!serviceContext.getConfigManager().getConfig().isAutoVanishEnabled());
                        serviceContext.getLoggerService().printSuccess("Автоматический ваниш " + (serviceContext.getConfigManager().getConfig().isAutoVanishEnabled() ? "включён" : "выключен") + ".");
                        break;
                    }
                    case (".fly"): {
                        serviceContext.getConfigManager().getConfig().setAutoFlyEnabled(!serviceContext.getConfigManager().getConfig().isAutoFlyEnabled());
                        serviceContext.getLoggerService().printSuccess("Автоматический флай " + (serviceContext.getConfigManager().getConfig().isAutoFlyEnabled() ? "включён" : "выключен") + ".");
                        break;
                    }
                    case (".gm3"): {
                        serviceContext.getConfigManager().getConfig().setAutoGm3Enabled(!serviceContext.getConfigManager().getConfig().isAutoGm3Enabled());
                        serviceContext.getLoggerService().printSuccess("Автоматический гм3 " + (serviceContext.getConfigManager().getConfig().isAutoGm3Enabled() ? "включён" : "выключен") + ".");
                        break;
                    }
                    case (".hacalerts"): {
                        serviceContext.getConfigManager().getConfig().setAutoHacAlertsEnabled(!serviceContext.getConfigManager().getConfig().isAutoHacAlertsEnabled());
                        serviceContext.getLoggerService().printSuccess("Автоматический hac alerts " + (serviceContext.getConfigManager().getConfig().isAutoHacAlertsEnabled() ? "включён" : "выключен") + ".");
                        break;
                    }
                    case (".god"): {
                        serviceContext.getConfigManager().getConfig().setAutoGodEnabled(!serviceContext.getConfigManager().getConfig().isAutoGodEnabled());
                        serviceContext.getLoggerService().printSuccess("Автоматический god " + (serviceContext.getConfigManager().getConfig().isAutoGodEnabled() ? "включён" : "выключен") + ".");
                        break;
                    }
                    case (".me"): {
                        CompletableFuture.runAsync(() -> {
                            try {
                                Map<String, Object> profile = serviceContext.getStateService().getJournalProfile();
                                serviceContext.getLoggerService().printSuccess("ИНФОРМАЦИЯ О МОДЕРАТОРЕ");
                                serviceContext.getChatService().clientMessage(WHITE + BOLD + "Ваш никнейм: " + AQUA + BOLD + profile.get("nickname").toString());
                                serviceContext.getChatService().clientMessage(WHITE + BOLD + "Ваша должность: " + RANKS.get((int) Double.parseDouble(profile.get("rank").toString())));
                                serviceContext.getChatService().clientMessage(serviceContext.getChatService().generateComponent(
                                        Text.of(WHITE + BOLD + "Ваш вк: " + AQUA + BOLD + profile.get("fullname").toString() + " ("),
                                        serviceContext.getChatService().openURLTextComponent(WHITE + BOLD + "vk.com/id" + (long) Double.parseDouble(profile.get("idVk").toString()), "Нажмите, чтобы открыть ссылку на свой вк.", "https://vk.com/id" + (long) Double.parseDouble(profile.get("idVk").toString())),
                                        Text.of(AQUA + BOLD + ")")));
                                serviceContext.getChatService().clientMessage(WHITE + BOLD + "Ваш баланс: " + GREEN + BOLD + (int) Double.parseDouble(profile.get("neponyatki").toString()));
                                serviceContext.getChatService().clientMessage(WHITE + BOLD + "Количество выговоров: " + RED + BOLD + (int) Double.parseDouble(profile.get("reprimands").toString()));
                                serviceContext.getChatService().clientMessage(WHITE + BOLD + "Количество предупреждений: " + GOLD + BOLD + (int) Double.parseDouble(profile.get("warns").toString()));
                                serviceContext.getChatService().clientMessage(WHITE + BOLD + "Режим: " + YELLOW + BOLD + profile.get("anarchyMode"));
                            } catch (Exception e) {
                                serviceContext.getLoggerService().printException("Исключение в SettingsManager/onMessageSend: " + e);
                            }
                        });
                        break;
                    }
                    case (".stats"): {
                        try {
                            Map<String, Object> stats = serviceContext.getStateService().getJournalStats();
                            serviceContext.getLoggerService().printSuccess("СТАТИСТИКА МОДЕРАТОРА");
                            Map<String, Object> revisesAll = (Map<String, Object>) stats.get("revisesAll");
                            Map<String, Object> revisesMonth = (Map<String, Object>) stats.get("revisesMonth");
                            Map<String, Object> revisesWeek = (Map<String, Object>) stats.get("revisesWeek");
                            Map<String, Object> revisesToday = (Map<String, Object>) stats.get("revisesToday");
                            if (revisesAll != null && revisesMonth != null && revisesWeek != null && revisesToday != null) {
                                serviceContext.getChatService().clientMessage(LIGHT_PURPLE + BOLD + "СТАТИСТИКА ПРОВЕРОК");
                                serviceContext.getChatService().clientMessage(WHITE + BOLD + "Проверок за всё время: " +
                                        AQUA + BOLD + (int) Double.parseDouble(revisesAll.get("total").toString()) +
                                        " (лайт: " + (int) Double.parseDouble(revisesAll.get("lite").toString()) +
                                        ", лайт 1.20: " + (int) Double.parseDouble(revisesAll.get("lite120").toString()) +
                                        ", классик: " + (int) Double.parseDouble(revisesAll.get("classic").toString()) + ")");
                                serviceContext.getChatService().clientMessage(WHITE + BOLD + "Проверок за последний месяц: " + AQUA + BOLD + (int) Double.parseDouble(revisesMonth.get("total").toString())
                                        + " (лайт: " + (int) Double.parseDouble(revisesMonth.get("lite").toString()) +
                                        ", лайт 1.20: " + (int) Double.parseDouble(revisesMonth.get("lite120").toString()) +
                                        ", классик: " + (int) Double.parseDouble(revisesMonth.get("classic").toString()) + ")");
                                serviceContext.getChatService().clientMessage(WHITE + BOLD + "Проверок за последнюю неделю: " + AQUA + BOLD + (int) Double.parseDouble(revisesWeek.get("total").toString())
                                        + " (лайт: " + (int) Double.parseDouble(revisesWeek.get("lite").toString()) +
                                        ", лайт 1.20: " + (int) Double.parseDouble(revisesWeek.get("lite120").toString()) +
                                        ", классик: " + (int) Double.parseDouble(revisesWeek.get("classic").toString()) + ")");
                                serviceContext.getChatService().clientMessage(WHITE + BOLD + "Проверок за сегодня: " + AQUA + BOLD + (int) Double.parseDouble(revisesToday.get("total").toString())
                                        + " (лайт: " + (int) Double.parseDouble(revisesToday.get("lite").toString()) +
                                        ", лайт 1.20: " + (int) Double.parseDouble(revisesToday.get("lite120").toString()) +
                                        ", классик: " + (int) Double.parseDouble(revisesToday.get("classic").toString()) + ")");
                            }
                            serviceContext.getChatService().clientMessage(LIGHT_PURPLE + BOLD + "СТАТИСТИКА МУТОВ И ГАРАНТОВ");
                            serviceContext.getChatService().clientMessage(WHITE + BOLD + "Мутов за всё время: " + AQUA + BOLD + (int) Double.parseDouble(stats.get("mutesAll").toString()));
                            serviceContext.getChatService().clientMessage(WHITE + BOLD + "Мутов за последний месяц: " + AQUA + BOLD + (int) Double.parseDouble(stats.get("mutesMonth").toString()));
                            serviceContext.getChatService().clientMessage(WHITE + BOLD + "Мутов за сегодня: " + AQUA + BOLD + (int) Double.parseDouble(stats.get("mutesToday").toString()));
                            serviceContext.getChatService().clientMessage(WHITE + BOLD + "Гарантов за всё время: " + AQUA + BOLD + (int) Double.parseDouble(stats.get("gaurantsAll").toString()));
                            serviceContext.getChatService().clientMessage(WHITE + BOLD + "Гарантов за последний месяц: " + AQUA + BOLD + (int) Double.parseDouble(stats.get("gaurantsMonth").toString()));
                            serviceContext.getChatService().clientMessage(WHITE + BOLD + "Гарантов за сегодня: " + AQUA + BOLD + (int) Double.parseDouble(stats.get("gaurantsToday").toString()));
                        } catch (Exception e) {
                            serviceContext.getLoggerService().printException("Исключение в SettingsManager/onMessageSend: " + e);
                        }
                        break;
                    }
                    case (".copy"): {
                        serviceContext.getConfigManager().getConfig().setCopyButtonEnabled(!serviceContext.getConfigManager().getConfig().isCopyButtonEnabled());
                        serviceContext.getLoggerService().printSuccess("Кнопка копирования " + (serviceContext.getConfigManager().getConfig().isCopyButtonEnabled() ? "включена" : "выключена") + ".");
                        break;
                    }
                    case (".sounds"): {
                        serviceContext.getConfigManager().getConfig().setSoundsEnabled(!serviceContext.getConfigManager().getConfig().isSoundsEnabled());
                        serviceContext.getLoggerService().printSuccess("Звуки мода " + (serviceContext.getConfigManager().getConfig().isSoundsEnabled() ? "включены" : "выключены") + ".");
                        break;
                    }
                }
            } else if (serviceContext.getChatService().isArrayContains(settingsWithOneArgument, command)) {
                messageSplit = message.split(" ", 2);
                switch (command) {
                    case (".textadd"): {
                        if (messageSplit.length == 1) {
                            serviceContext.getLoggerService().printError("Вы не указали текст.");
                            return;
                        }
                        String text = messageSplit[1].replace("&", "§");
                        if (text.contains("%%")) {
                            serviceContext.getLoggerService().printError("Текст не должен содержать '%%'.");
                            return;
                        }
                        serviceContext.getConfigManager().getConfig().setTexts(serviceContext.getConfigManager().getConfig().getTexts().isEmpty() ? text : serviceContext.getConfigManager().getConfig().getTexts() + "%%" + text);
                        serviceContext.getLoggerService().printSuccess("Вы добавили новый текст.");
                        break;
                    }
                    case (".textremove"): {
                        if (serviceContext.getConfigManager().getConfig().getTexts().isEmpty()) {
                            serviceContext.getLoggerService().printError("У вас нет настроенных текстов.");
                            return;
                        }
                        if (messageSplit.length == 1) {
                            serviceContext.getLoggerService().printError("Вы не указали номер текста.");
                            return;
                        }
                        String indexText = messageSplit[1];
                        if (!serviceContext.getChatService().checkCorrectInt(indexText)) {
                            serviceContext.getLoggerService().printError("Некорректный номер текста.");
                            return;
                        }
                        int intIndex = Integer.parseInt(indexText) - 1;
                        if (intIndex >= textsArray.length || intIndex < 0) {
                            serviceContext.getLoggerService().printError("Элемента с таким номером в списке ваших текстов не существует.");
                            return;
                        }
                        ArrayList<String> textsArrayList = new ArrayList<>(Arrays.asList(textsArray));
                        textsArrayList.remove(intIndex);
                        serviceContext.getConfigManager().getConfig().setTexts(StringUtils.EMPTY);
                        for (String s : textsArrayList) {
                            serviceContext.getConfigManager().getConfig().setTexts(serviceContext.getConfigManager().getConfig().getTexts().isEmpty() ? s : serviceContext.getConfigManager().getConfig().getTexts() + "%%" + s);
                        }
                        serviceContext.getLoggerService().printSuccess("Вы удалили текст номер " + messageSplit[1] + AQUA + BOLD + ".");
                        break;
                    }
                    case (".setcopy"): {
                        if (messageSplit.length == 1) {
                            serviceContext.getConfigManager().getConfig().setCopyButtonText("§f§l[§a§lcopy§f§l]");
                            serviceContext.getLoggerService().printSuccess("Текст кнопки был сброшен.");
                            return;
                        }
                        serviceContext.getConfigManager().getConfig().setCopyButtonText(messageSplit[1].replace("&", "§"));
                        serviceContext.getLoggerService().printSuccess("Вы установили новый текст кнопки копирования.");
                        break;
                    }
                    case (".setmarker"): {
                        if (messageSplit.length == 1) {
                            serviceContext.getConfigManager().getConfig().setPlayerMarker("§d§l[CHECK]");
                            serviceContext.getLoggerService().printSuccess("Текст метки был сброшен.");
                            return;
                        }
                        serviceContext.getConfigManager().getConfig().setPlayerMarker(messageSplit[1].replace("&", "§"));
                        serviceContext.getLoggerService().printSuccess("Вы установили новый текст маркера.");
                        break;
                    }
                    case (".setspydelay"): {
                        if (messageSplit.length == 1) {
                            serviceContext.getLoggerService().printError("Вы не указали число.");
                            return;
                        }

                        String valueText = messageSplit[1];
                        if (!serviceContext.getChatService().checkCorrectInt(valueText)) {
                            serviceContext.getLoggerService().printError("Некорректное число.");
                            return;
                        }
                        serviceContext.getConfigManager().getConfig().setSpyDelay(Integer.parseInt(valueText));
                        serviceContext.getLoggerService().printSuccess("Вы установили новую задержку в .spy: " + serviceContext.getConfigManager().getConfig().getSpyDelay() + ".");
                        break;
                    }
                }
            } else if (serviceContext.getChatService().isArrayContains(settingsWithTwoArguments, command)) {
                messageSplit = message.split(" ", 3);
                switch (command) {
                    case (".textedit"): {
                        if (serviceContext.getConfigManager().getConfig().getTexts().isEmpty()) {
                            serviceContext.getLoggerService().printError("У вас нет настроенных текстов.");
                            return;
                        }
                        if (messageSplit.length == 1) {
                            serviceContext.getLoggerService().printError("Вы не указали номер текста и новый текст.");
                            return;
                        }
                        String indexText = messageSplit[1];
                        if (!serviceContext.getChatService().checkCorrectInt(indexText)) {
                            serviceContext.getLoggerService().printError("Некорректный номер текста.");
                            return;
                        }
                        int index = Integer.parseInt(indexText) - 1;
                        if (messageSplit.length == 2) {
                            serviceContext.getLoggerService().printError("Вы не указали новый текст.");
                            return;
                        }
                        if (index >= textsArray.length || index < 0) {
                            serviceContext.getLoggerService().printError("Элемента с таким номером в списке ваших текстов не существует.");
                            return;
                        }
                        String text = messageSplit[2].replace("&", "§");
                        if (text.contains("%%")) {
                            serviceContext.getLoggerService().printError("Текст не должен содержать '%%'.");
                            return;
                        }
                        String[] textsList = serviceContext.getConfigManager().getConfig().getTexts().split("%%");
                        textsList[index] = text;
                        serviceContext.getConfigManager().getConfig().setTexts(StringUtils.EMPTY);
                        for (String t : textsList) {
                            serviceContext.getConfigManager().getConfig().setTexts(serviceContext.getConfigManager().getConfig().getTexts().isEmpty() ? t : serviceContext.getConfigManager().getConfig().getTexts() + "%%" + t);
                        }
                        serviceContext.getLoggerService().printSuccess("Вы изменили текст номер " + (index + 1) + ".");
                        break;
                    }
                }
            }
            serviceContext.getConfigManager().saveCfg(serviceContext.getConfigManager().getConfig());
        }
    }
}