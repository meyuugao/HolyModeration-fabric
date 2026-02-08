package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.config.Config;
import me.yuugao.holymoderation.client.config.ConfigManager;
import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.CommandSendEvent;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.ChatService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.NotificationType;
import me.yuugao.holymoderation.client.util.serviceLocator.service.NotificationsService;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import obfuscator.DontObf;
import obfuscator.ObfRule;

@DontObf(ObfRule.OBF_STRING)
public class SettingsModule extends Module {
    private final String[] settingsCommands = {
            "autoban", "autocopy", "autodupeip", "autofly", "autogm3", "autogod", "autoha", "autotp", "autovanish",
            "copy", "setcopy", "setmarker", "setspydelay", "setsoundsvolume", "sounds", "textadd", "textedit",
            "textremove", "textsclear", "textslist"
    };

    private final String[] settingsWithoutArguments = {
            "autoban", "autocopy", "autodupeip", "autofly", "autogm3", "autogod", "autoha", "autotp", "autovanish",
            "copy", "sounds", "textsclear", "textslist"
    };

    private final String[] settingsWithOneArgument = {
            "setcopy", "setmarker", "setspydelay", "setsoundsvolume", "textadd", "textremove"
    };

    private final String[] settingsWithTwoArguments = {
            "textedit"
    };

    public SettingsModule(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Subscribe
    public void onCommandSend(CommandSendEvent event) {
        ChatService chatService = serviceContext.getChatService();
        ConfigManager configManager = serviceContext.getConfigManager();
        NotificationsService notificationsService = serviceContext.getNotificationsService();

        Config config = configManager.getConfig();

        String eventCommand = event.getCommand();
        String[] commandSplit = eventCommand.split(" ");
        if (!eventCommand.startsWith("hm") || commandSplit.length < 2) return;

        String command = commandSplit[1];

        if (chatService.isArrayContains(settingsCommands, command)) {
            List<String> textsList = config.getTextsList();
            if (chatService.isArrayContains(settingsWithoutArguments, command)) {
                switch (command) {
                    case "textslist" -> {
                        if (textsList.isEmpty()) {
                            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                                    "У вас нет настроенных текстов.", 5f);
                        } else {
                            StringBuilder texts = new StringBuilder(StringUtils.EMPTY);
                            for (int i = 0; i < textsList.size(); i++) {
                                texts.append("%s%s".formatted(AQUA, BOLD)).append(i + 1).append(WHITE).append(". ").append(textsList.get(i));
                                if (i < textsList.size() - 1) texts.append("\n");
                            }
                            notificationsService.addNotification(NotificationType.SUCCESS,
                                    "%s%sСписок ваших текстов".formatted(GREEN, BOLD), texts.toString(), 10f);
                        }
                    }
                    case "textsclear" -> {
                        textsList.clear();
                        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                                "Вы успешно очистили все тексты.", 5f);
                    }
                    case "autodupeip" -> {
                        config.setDupeIpEnabled(!config.isDupeIpEnabled());
                        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                                "Автоматический /dupeip %s.".formatted(config.isDupeIpEnabled() ? "включён" : "выключен"), 5f);
                    }
                    case "autocopy" -> {
                        config.setAutoAnyDeskEnabled(!config.isAutoAnyDeskEnabled());
                        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                                "Автоматическое копирование айди AnyDesk %s.".formatted(config.isAutoAnyDeskEnabled() ? "включено" : "выключено"), 5f);
                    }
                    case "autotp" -> {
                        config.setAutoTpEnabled(!config.isAutoTpEnabled());
                        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                                "Автоматический телепорт на /warp logo %s.".formatted(config.isAutoTpEnabled() ? "включён" : "выключен"), 5f);
                    }
                    case "autoban" -> {
                        config.setAutoBanEnabled(!config.isAutoBanEnabled());
                        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                                "Автоматический бан игрока при ливе с проверки %s.".formatted(config.isAutoBanEnabled() ? "включён" : "выключен"), 5f);
                    }
                    case "autovanish" -> {
                        config.setAutoVanishEnabled(!config.isAutoVanishEnabled());
                        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                                "Автоматический ваниш %s.".formatted(config.isAutoVanishEnabled() ? "включён" : "выключен"), 5f);
                    }
                    case "autofly" -> {
                        config.setAutoFlyEnabled(!config.isAutoFlyEnabled());
                        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                                "Автоматический флай %s.".formatted(config.isAutoFlyEnabled() ? "включён" : "выключен"), 5f);
                    }
                    case "autogm3" -> {
                        config.setAutoGm3Enabled(!config.isAutoGm3Enabled());
                        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                                "Автоматический гм3 %s.".formatted(config.isAutoGm3Enabled() ? "включён" : "выключен"), 5f);
                    }
                    case "autoha" -> {
                        config.setAutoHacAlertsEnabled(!config.isAutoHacAlertsEnabled());
                        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                                "Автоматический hac alerts %s.".formatted(config.isAutoHacAlertsEnabled() ? "включён" : "выключен"), 5f);
                    }
                    case "autogod" -> {
                        config.setAutoGodEnabled(!config.isAutoGodEnabled());
                        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                                "Автоматический god %s.".formatted(config.isAutoGodEnabled() ? "включён" : "выключен"), 5f);
                    }
                    case "copy" -> {
                        config.setCopyButtonEnabled(!config.isCopyButtonEnabled());
                        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                                "Кнопка копирования %s.".formatted(config.isCopyButtonEnabled() ? "включена" : "выключена"), 5f);
                    }
                    case "sounds" -> {
                        config.setSoundsEnabled(!config.isSoundsEnabled());
                        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                                "Звуки мода %s.".formatted(config.isSoundsEnabled() ? "включены" : "выключены"), 5f);
                    }
                }
            } else if (chatService.isArrayContains(settingsWithOneArgument, command)) {
                commandSplit = eventCommand.split(" ", 3);
                switch (command) {
                    case "textadd" -> {
                        if (commandSplit.length == 2) {
                            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                                    "Вы не указали текст.", 5f);
                            return;
                        }
                        String text = commandSplit[2].replace("&", "§");
                        if (text.length() > 200) {
                            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                                    "Текст слишком длинный! Длина текста: %s (максимум 200)".formatted(text.length()), 5f);
                            return;
                        }
                        textsList.add(text);
                        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                                "Вы добавили новый текст.", 5f);
                    }
                    case "textremove" -> {
                        if (textsList.isEmpty()) {
                            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                                    "У вас нет настроенных текстов.", 5f);
                            return;
                        }
                        if (commandSplit.length == 2) {
                            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                                    "Вы не указали номер текста.", 5f);
                            return;
                        }
                        String indexText = commandSplit[2];
                        if (!chatService.checkCorrectInt(indexText)) {
                            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                                    "Некорректный номер текста.", 5f);
                            return;
                        }
                        int intIndex = Integer.parseInt(indexText) - 1;
                        if (intIndex >= textsList.size() || intIndex < 0) {
                            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                                    "Элемента с таким номером в списке ваших текстов не существует.", 5f);
                            return;
                        }
                        textsList.remove(intIndex);
                        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                                "Вы удалили текст номер %s%s%s.".formatted(commandSplit[2], AQUA, BOLD), 5f);
                    }
                    case "setcopy" -> {
                        if (commandSplit.length == 2) {
                            config.setCopyButtonText("§f§l[§a§lcopy§f§l]");
                            notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                                    "Текст кнопки был сброшен.", 5f);
                        } else {
                            config.setCopyButtonText(commandSplit[2].replace("&", "§"));
                            notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                                    "Вы установили новый текст кнопки копирования.", 5f);
                        }
                    }
                    case "setmarker" -> {
                        if (commandSplit.length == 2) {
                            config.setPlayerMarker("§d§l[CHECK]");
                            notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                                    "Текст метки был сброшен.", 5f);
                        } else {
                            config.setPlayerMarker(commandSplit[2].replace("&", "§"));
                            notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                                    "Вы установили новый текст маркера.", 5f);
                        }
                    }
                    case "setspydelay" -> {
                        String valueText;
                        try {
                            valueText = validateArguments(commandSplit);
                        } catch (IllegalArgumentException e) {
                            return;
                        }

                        config.setSpyDelay(Integer.parseInt(valueText));
                        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                                "Вы установили новую задержку в spy: %s.".formatted(config.getSpyDelay()), 5f);
                    }
                    case "setsoundsvolume" -> {
                        String valueText;
                        try {
                            valueText = validateArguments(commandSplit);
                        } catch (IllegalArgumentException e) {
                            return;
                        }

                        config.setSoundsVolume(Integer.parseInt(valueText));
                        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                                "Вы установили новую громкость звуков: %s.".formatted(config.getSoundsVolume()), 5f);
                    }
                }
            } else if (chatService.isArrayContains(settingsWithTwoArguments, command)) {
                commandSplit = eventCommand.split(" ", 4);
                if (command.equals("textedit")) {
                    if (textsList.isEmpty()) {
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                                "У вас нет настроенных текстов.", 5f);
                        return;
                    }
                    if (commandSplit.length == 2) {
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                                "Вы не указали номер текста и новый текст.", 5f);
                        return;
                    }
                    String indexText = commandSplit[2];
                    if (!chatService.checkCorrectInt(indexText)) {
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                                "Некорректный номер текста.", 5f);
                        return;
                    }
                    int index = Integer.parseInt(indexText) - 1;
                    if (commandSplit.length == 3) {
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                                "Вы не указали новый текст.", 5f);
                        return;
                    }
                    if (index >= textsList.size() || index < 0) {
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                                "Элемента с таким номером в списке ваших текстов не существует.", 5f);
                        return;
                    }
                    String text = commandSplit[3].replace("&", "§");
                    if (text.contains("%%")) {
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                                "Текст не должен содержать '%%'.", 5f);
                        return;
                    }
                    textsList.set(index, text);
                    notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                            "Вы изменили текст номер %s.".formatted((index + 1)), 5f);
                }
            }
            configManager.saveCfg(config);
        }
    }

    private String validateArguments(String[] commandSplit) {
        NotificationsService notificationsService = serviceContext.getNotificationsService();
        ChatService chatService = serviceContext.getChatService();

        if (commandSplit.length == 2) {
            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                    "Вы не указали число.", 5f);
            throw new IllegalArgumentException();
        }

        String valueText = commandSplit[2];
        if (!chatService.checkCorrectInt(valueText)) {
            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                    "Некорректное число.", 5f);
            throw new IllegalArgumentException();
        }

        return valueText;
    }
}