package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.config.Config;
import me.yuugao.holymoderation.client.config.ConfigManager;
import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.CommandSendEvent;
import me.yuugao.holymoderation.client.eventbus.event.MessageReceiveEvent;
import me.yuugao.holymoderation.client.modules.drawable.DrawableModule;
import me.yuugao.holymoderation.client.modules.drawable.element.CheckoutsDrawableElement;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;
import me.yuugao.holymoderation.client.util.serviceLocator.service.*;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class CheckoutsModule extends DrawableModule<CheckoutsDrawableElement> {
    private final String[] FreezerCommands = {
            "/freezing", "/frz", "freezing", "frz", "sban", "sendtexts", "unfreezing", "unfrz"
    };

    private final String[] ApiCommands = {
            "endcheckout", "startcheckout"
    };

    private boolean startingCheckout = false;
    private boolean banChecking = false;
    private boolean destroyStash;
    private boolean messageIsCheckbanInfo;
    private String banReason;

    public CheckoutsModule(ServiceContext serviceContext, CheckoutsDrawableElement checkoutsDrawableElement) {
        super(serviceContext, checkoutsDrawableElement);
    }

    @Subscribe
    public void onCommandSend(CommandSendEvent event) {
        StateService stateService = serviceContext.getStateService();
        NotificationsService notificationsService = serviceContext.getNotificationsService();
        ChatService chatService = serviceContext.getChatService();
        CheckoutsService checkoutsService = serviceContext.getCheckoutsService();
        PunishmentsService punishmentsService = serviceContext.getPunishmentsService();
        NetService netService = serviceContext.getNetService();

        String checkoutPlayer = stateService.getCheckoutPlayer();

        String eventCommand = event.getCommand();
        String[] commandSplit = eventCommand.split(" ");
        if (eventCommand.startsWith("hm") && commandSplit.length < 2) return;

        String command;
        if (eventCommand.startsWith("hm")) {
            command = commandSplit[1];
        } else {
            command = "/%s".formatted(commandSplit[0]);
        }

        if (ServiceLocator.getChatService().isArrayContains(FreezerCommands, command)
                || ServiceLocator.getChatService().isArrayContains(ApiCommands, command)) {
            if (stateService.isInHub()) {
                notificationsService.addNotification(NotificationType.WARNING,
                        "%s%sПредупреждение".formatted(GOLD, BOLD),
                        "В хабе этого делать нельзя.", 5f);
                return;
            }
        }
        if (ServiceLocator.getChatService().isArrayContains(FreezerCommands, command)) {
            switch (command) {
                case "/freezing", "/frz" -> {
                    event.setCancelled(true);
                    commandSplit = eventCommand.split(" ", 2);
                    if (commandSplit.length == 1) {
                        notificationsService.addNotification(NotificationType.ERROR,
                                "%s%sОшибка".formatted(RED, BOLD),
                                "Вы не указали ник игрока.", 5f);
                        return;
                    }
                    String player = commandSplit[1];
                    if (player.equals(checkoutPlayer)) {
                        notificationsService.addNotification(NotificationType.WARNING,
                                "%s%sПредупреждение".formatted(GOLD, BOLD),
                                "Этот игрок находиться у вас на проверке. Для его разморозки используйте %s%s%s/unfreezing%s или %s%s%s/unfrz%s"
                                        .formatted(GOLD, GOLD, BOLD, WHITE, GOLD, GOLD, BOLD, WHITE),
                                5f);
                        return;
                    }
                    chatService.chatMessage("/freezing %s".formatted(player));
                }
                case "unfreezing", "unfrz" -> {
                    if (checkoutPlayer.isEmpty()) {
                        notificationsService.addNotification(NotificationType.WARNING,
                                "%s%sПредупреждение".formatted(GOLD, BOLD),
                                "Вы никого не проверяете.", 5f);
                        return;
                    }
                    checkoutsService.endCheckOut(false);
                }
                case "sban" -> {
                    commandSplit = eventCommand.split(" ", 4);
                    if (checkoutPlayer.isEmpty()) {
                        notificationsService.addNotification(NotificationType.WARNING,
                                "%s%sПредупреждение".formatted(GOLD, BOLD),
                                "Вы никого не проверяете.", 5f);
                        return;
                    }
                    if (commandSplit.length == 2) {
                        notificationsService.addNotification(NotificationType.ERROR,
                                "%s%sОшибка".formatted(RED, BOLD),
                                "Вы не указали время и причину бана.", 5f);
                        return;
                    }
                    String time = commandSplit[2];
                    String reason = commandSplit.length == 3 ? "2.4" : "2.4 (%s)".formatted(commandSplit[3]);
                    if (!punishmentsService.punish("/banip", checkoutPlayer, time, reason, true)) return;
                    checkoutsService.endCheckOut(false);
                }
                case "freezing", "frz" -> {
                    commandSplit = eventCommand.split(" ", 3);
                    if (commandSplit.length < 3) {
                        notificationsService.addNotification(NotificationType.ERROR,
                                "%s%sОшибка".formatted(RED, BOLD),
                                "Вы не указали ник игрока.", 5f);
                        return;
                    }

                    startingCheckout = true;
                    if (checkoutsService.startCheckOut(commandSplit[2])) {
                        this.drawableElement.coStartForLocal(commandSplit[2]);
                    }
                }
                case "sendtexts" -> {
                    commandSplit = eventCommand.split(" ", 3);
                    if (commandSplit.length == 2) {
                        notificationsService.addNotification(NotificationType.ERROR,
                                "%s%sОшибка".formatted(RED, BOLD),
                                "Вы не указали ник игрока.", 5f);
                        return;
                    }
                    checkoutsService.sendTexts(commandSplit[2]);
                }
            }
        } else if (chatService.isArrayContains(ApiCommands, command)) {
            String[] messageSplit;
            switch (command) {
                case "startcheckout" -> {
                    messageSplit = eventCommand.split(" ", 4);
                    switch (messageSplit.length) {
                        case 2 -> notificationsService.addNotification(NotificationType.ERROR,
                                "%s%sОшибка".formatted(RED, BOLD),
                                "Вы не указали ник игрока и причину проверки.", 5f);
                        case 3 -> notificationsService.addNotification(NotificationType.ERROR,
                                "%s%sОшибка".formatted(RED, BOLD),
                                "Вы не указали причину проверки.", 5f);
                    }
                    String player = messageSplit[2];
                    String reason = messageSplit[3];
                    if (!Arrays.stream(new String[]{"report", "checkout", "autobuy", "autosell", "customka", "personal",
                            "toManyChecks", "candidate"}).toList().contains(reason)) {
                        notificationsService.addNotification(NotificationType.ERROR,
                                "%s%sОшибка".formatted(RED, BOLD),
                                "Некорректная причина проверки.", 5f);
                        return;
                    }
                    String loc = stateService.getUserLocation();
                    String mode = loc.startsWith("lite120") ? "lite120" : loc.startsWith("lite") ? "lite" :
                            loc.startsWith("classic") ? "classic" : "lpvp";
                    CompletableFuture.runAsync(() -> {
                        if (mode.equals("lpvp")) {
                            netService.startCheckout(player, reason, "lite", 1, true);
                        } else {
                            int number = Integer.parseInt(loc.split("%s-".formatted(mode))[1]);
                            netService.startCheckout(player, reason, mode, number, false);
                        }
                    });
                }
                case "endcheckout" -> {
                    messageSplit = eventCommand.split(" ", 6);
                    if (messageSplit.length == 2) {
                        notificationsService.addNotification(NotificationType.ERROR,
                                "%s%sОшибка".formatted(RED, BOLD),
                                "Вы не указали результат проверки.", 5f);
                        return;
                    }
                    String result = messageSplit[2];
                    if (!Arrays.stream(new String[]{"clean", "ban", "autobuy", "autosell"}).toList().contains(result)) {
                        notificationsService.addNotification(NotificationType.ERROR,
                                "%s%sОшибка".formatted(RED, BOLD),
                                "Некорректный результат проверки.", 5f);
                        return;
                    }
                    CompletableFuture.runAsync(() -> {
                        switch (result) {
                            case "clean" -> netService.endCheckout(result, result, false);
                            case "ban" -> {
                                if (messageSplit.length == 3) {
                                    notificationsService.addNotification(NotificationType.ERROR,
                                            "%s%sОшибка".formatted(RED, BOLD),
                                            "Вы не указали ник игрока и необходимость снести стеш.", 5f);
                                    return;
                                } else if (messageSplit.length == 4) {
                                    notificationsService.addNotification(NotificationType.ERROR,
                                            "%s%sОшибка".formatted(RED, BOLD),
                                            "Вы не указали необходимость снести стеш.", 5f);
                                    return;
                                }

                                if (!Arrays.stream(new String[]{"true", "false"}).toList().contains(messageSplit[4])) {
                                    notificationsService.addNotification(NotificationType.ERROR,
                                            "%s%sОшибка".formatted(RED, BOLD),
                                            "Некорректная необходимость снести стеш.", 5f);
                                    return;
                                }
                                destroyStash = messageSplit[4].equals("true");

                                if (messageSplit.length == 5) {
                                    banChecking = true;
                                    chatService.chatMessage("/checkban %s".formatted(messageSplit[3]));
                                } else if (messageSplit.length == 6) {
                                    netService.endCheckout(result, messageSplit[5], destroyStash);
                                }
                            }
                            case "autobuy", "autosell" -> netService.endCheckout(result, result, true);
                        }
                    });
                }
            }
        }
    }

    @Subscribe(priority = 99)
    public void onMessageReceive(MessageReceiveEvent event) {
        ChatService chatService = serviceContext.getChatService();
        StateService stateService = serviceContext.getStateService();
        ConfigManager configManager = serviceContext.getConfigManager();
        PunishmentsService punishmentsService = serviceContext.getPunishmentsService();
        CheckoutsService checkoutsService = serviceContext.getCheckoutsService();
        NotificationsService notificationsService = serviceContext.getNotificationsService();
        NetService netService = serviceContext.getNetService();

        String checkoutPlayer = stateService.getCheckoutPlayer();
        Config config = configManager.getConfig();
        String receivedText = chatService.formatReceivedText(event.getMessage().getString());
        if (receivedText == null) return;

        if (startingCheckout) {
            if (receivedText.equals("Игрок заморожен!")) {
                startingCheckout = false;
            } else if (receivedText.equals("Игрок не найден!")) {
                startingCheckout = false;
                checkoutsService.endCheckOut(true);
            }
        }

        if (!checkoutPlayer.isEmpty() && config.isAutoBanEnabled()) {
            if (receivedText.startsWith("▶ Замороженный игрок %s".formatted(checkoutPlayer))) {
                punishmentsService.punish("/banip", checkoutPlayer,
                        "30d", "2.4 (Лив с проверки)", true);
                checkoutsService.endCheckOut(false);
            }
        }

        if (config.isAutoAnyDeskEnabled() && !checkoutPlayer.isEmpty() && receivedText.contains(checkoutPlayer)) {
            String chatText;
            String msgText;
            if (receivedText.startsWith("[%s ->".formatted(checkoutPlayer))
                    && chatService.checkCorrectLong(msgText = receivedText.split("я]", 0)[1]
                    .replace(" ", StringUtils.EMPTY)) && msgText.length() >= 9 && msgText.length() <= 11) {
                chatService.copyToClipboard(msgText);
                notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                        "Скопирован анидеск из лс: %s".formatted(msgText), 5f);
            } else if (chatService.checkCorrectLong(chatText = receivedText.split(":")[1]
                    .replace(" ", StringUtils.EMPTY)) && chatText.length() >= 9 && chatText.length() <= 11) {
                chatService.copyToClipboard(chatText);
                notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                        "Скопирован анидеск из чата: %s".formatted(chatText), 5f);
            }
        }

        if (banChecking) {
            if (receivedText.equals("Цель не забанена!") || receivedText.equals("История не найдена.")) {
                event.setCancelled(true);
                notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                        "Проверка не была закончена, т.к. не удалось определить причину бана игрока. Пожалуйста, допишите причину вручную.", 5f);
                banChecking = false;
            }

            if (receivedText.startsWith("Игрок ["))
                messageIsCheckbanInfo = true;

            if (receivedText.startsWith("Причина:"))
                banReason = receivedText.split("Причина: ")[1].split(" \\| ")[0];

            if (messageIsCheckbanInfo)
                event.setCancelled(true);

            if (receivedText.startsWith("IP бан:")) {
                messageIsCheckbanInfo = false;
                banChecking = false;
                CompletableFuture.runAsync(() -> netService.endCheckout("ban", banReason, destroyStash));
            }
        }
    }

    @Override
    public int getRenderPriority() {
        return 1002;
    }
}