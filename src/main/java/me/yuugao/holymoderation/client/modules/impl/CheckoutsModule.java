package me.yuugao.holymoderation.client.modules.impl;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.singleton.CheckoutsDrawableElement;
import me.yuugao.holymoderation.client.modules.DrawableModule;
import me.yuugao.holymoderation.client.util.service.*;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.config.impl.SettingsConfig;
import me.yuugao.holymoderation.client.util.service.eventbus.Subscribe;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.chat.CommandSendEvent;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.chat.MessageReceiveEvent;
import me.yuugao.holymoderation.client.util.service.state.PlayerStateService;
import me.yuugao.holymoderation.client.util.service.state.UserStateService;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

@Singleton
public class CheckoutsModule extends DrawableModule<CheckoutsDrawableElement> {
    private final String[] FreezerCommands = {"/freezing", "/frz", "freezing", "frz", "unfreezing", "unfrz", "sban", "sendtexts"};
    private final String[] ApiCommands = {"endcheckout", "startcheckout"};
    private final ConfigManagerService configManagerService;
    private final PlayerStateService playerStateService;
    private final UserStateService userStateService;
    private final NotificationsService notificationsService;
    private final ChatService chatService;
    private final CheckoutsService checkoutsService;
    private final PunishmentsService punishmentsService;
    private final NetService netService;
    private final AsyncExecutor asyncExecutor;
    private boolean startingCheckout = false;
    private boolean banChecking = false;
    private boolean destroyStash;
    private boolean messageIsCheckbanInfo;
    private String banReason;

    @Inject
    public CheckoutsModule(ConfigManagerService configManagerService,
                           CheckoutsDrawableElement drawableElement,
                           PlayerStateService playerStateService,
                           UserStateService userStateService,
                           NotificationsService notificationsService,
                           ChatService chatService,
                           CheckoutsService checkoutsService,
                           PunishmentsService punishmentsService,
                           NetService netService,
                           AsyncExecutor asyncExecutor) {
        super(drawableElement);
        this.configManagerService = configManagerService;
        this.playerStateService = playerStateService;
        this.userStateService = userStateService;
        this.notificationsService = notificationsService;
        this.chatService = chatService;
        this.checkoutsService = checkoutsService;
        this.punishmentsService = punishmentsService;
        this.netService = netService;
        this.asyncExecutor = asyncExecutor;
    }

    @Subscribe
    public void onCommandSend(CommandSendEvent event) {
        String checkoutPlayer = playerStateService.getCheckoutPlayer();
        String eventCommand = event.getCommand();
        String[] commandSplit = eventCommand.split(" ");
        if (eventCommand.startsWith("hm") && commandSplit.length < 2) return;
        String command = eventCommand.startsWith("hm") ? commandSplit[1] : "/%s".formatted(commandSplit[0]);

        if (chatService.isArrayContains(FreezerCommands, command) || chatService.isArrayContains(ApiCommands, command)) {
            if (userStateService.isInHub()) {
                notificationsService.addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(GOLD, BOLD), "В хабе этого делать нельзя.", 5f);
                return;
            }
        }

        if (chatService.isArrayContains(FreezerCommands, command)) {
            switch (command) {
                case "/freezing", "/frz" -> {
                    event.setCancelled(true);
                    commandSplit = eventCommand.split(" ", 2);
                    if (commandSplit.length == 1) {
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD), "Вы не указали ник игрока.", 5f);
                        return;
                    }
                    String player = commandSplit[1];
                    if (player.equals(checkoutPlayer)) {
                        notificationsService.addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(GOLD, BOLD), "Этот игрок находиться у вас на проверке. Для его разморозки используйте %s%s%s/unfreezing%s или %s%s%s/unfrz%s".formatted(GOLD, GOLD, BOLD, WHITE, GOLD, GOLD, BOLD, WHITE), 5f);
                        return;
                    }
                    chatService.chatMessage("/freezing %s".formatted(player));
                }
                case "freezing", "frz" -> {
                    String nickname = eventCommand.split(command + " ")[1];
                    if (commandSplit.length < 3) {
                        notificationsService.addNotification(NotificationType.ERROR,
                                "%s%sОшибка".formatted(RED, BOLD),
                                "Вы не указали ник игрока.", 5f);
                        return;
                    }

                    startingCheckout = true;
                    if (checkoutsService.startCheckOut(nickname)) {
                        this.drawableElement.coStartForLocal(nickname);
                    }
                }
                case "unfreezing", "unfrz" -> {
                    if (checkoutPlayer.isEmpty()) {
                        notificationsService.addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(GOLD, BOLD), "Вы никого не проверяете.", 5f);
                        return;
                    }
                    checkoutsService.endCheckOut(false);
                }
                case "sban" -> {
                    commandSplit = eventCommand.split(" ", 4);
                    if (checkoutPlayer.isEmpty()) {
                        notificationsService.addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(GOLD, BOLD), "Вы никого не проверяете.", 5f);
                        return;
                    }
                    if (commandSplit.length == 2) {
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD), "Вы не указали время и причину бана.", 5f);
                        return;
                    }
                    String time = commandSplit[2];
                    String reason = commandSplit.length == 3 ? "2.4" : "2.4 (%s)".formatted(commandSplit[3]);
                    if (!punishmentsService.punish("/banip", checkoutPlayer, time, reason, true)) return;
                    checkoutsService.endCheckOut(false);
                }
                case "sendtexts" -> {
                    commandSplit = eventCommand.split(" ", 3);
                    if (commandSplit.length == 2) {
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD), "Вы не указали ник игрока.", 5f);
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
                    if (messageSplit.length == 2)
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD), "Вы не указали ник игрока и причину проверки.", 5f);
                    else if (messageSplit.length == 3)
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD), "Вы не указали причину проверки.", 5f);
                    String player = messageSplit[2];
                    String reason = messageSplit[3];
                    if (!Arrays.stream(new String[]{"report", "checkout", "autobuy", "autosell", "customka", "personal", "toManyChecks", "candidate"}).toList().contains(reason)) {
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD), "Некорректная причина проверки.", 5f);
                        return;
                    }
                    String loc = userStateService.getUserLocation();
                    String mode = loc.startsWith("lite120") ? "lite120" : loc.startsWith("lite") ? "lite" : loc.startsWith("classic") ? "classic" : "lpvp";
                    asyncExecutor.runAsync("CheckoutsModule/onCommandSend", () -> {
                        if (mode.equals("lpvp")) netService.startCheckout(player, reason, "lite", 1, true);
                        else
                            netService.startCheckout(player, reason, mode, Integer.parseInt(loc.split("%s-".formatted(mode))[1]), false);
                    });
                }
                case "endcheckout" -> {
                    messageSplit = eventCommand.split(" ", 6);
                    if (messageSplit.length == 2) {
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD), "Вы не указали результат проверки.", 5f);
                        return;
                    }
                    String result = messageSplit[2];
                    if (!Arrays.stream(new String[]{"clean", "ban", "autobuy", "autosell"}).toList().contains(result)) {
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD), "Некорректный результат проверки.", 5f);
                        return;
                    }
                    asyncExecutor.runAsync("CheckoutsModule/onCommandSend", () -> {
                        switch (result) {
                            case "clean" -> netService.endCheckout(result, result, false);
                            case "ban" -> {
                                if (messageSplit.length == 3) {
                                    notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD), "Вы не указали ник игрока и необходимость снести стеш.", 5f);
                                } else if (messageSplit.length == 4) {
                                    notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD), "Вы не указали необходимость снести стеш.", 5f);
                                } else {
                                    if (!Arrays.stream(new String[]{"true", "false"}).toList().contains(messageSplit[4])) {
                                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD), "Некорректная необходимость снести стеш.", 5f);
                                    } else {
                                        destroyStash = messageSplit[4].equals("true");
                                        if (messageSplit.length == 5) {
                                            banChecking = true;
                                            chatService.chatMessage("/checkban %s".formatted(messageSplit[3]));
                                        } else if (messageSplit.length == 6) {
                                            netService.endCheckout(result, messageSplit[5], destroyStash);
                                        } else {
                                            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD), "Некорректный формат команды.", 5f);
                                        }
                                    }
                                }
                            }
                            case "autobuy", "autosell" -> {
                                if (messageSplit.length == 3) {
                                    notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD), "Вы не указали необходимость снести стеш.", 5f);
                                } else if (messageSplit.length == 4) {
                                    if (!Arrays.stream(new String[]{"true", "false"}).toList().contains(messageSplit[3])) {
                                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD), "Некорректная необходимость снести стеш.", 5f);
                                    } else {
                                        destroyStash = messageSplit[3].equals("true");
                                        netService.endCheckout(result, result, destroyStash);
                                    }
                                } else {
                                    notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD), "Некорректный формат команды.", 5f);
                                }
                            }
                        }
                    });
                }
            }
        }
    }

    @Subscribe(priority = 99)
    public void onMessageReceive(MessageReceiveEvent event) {
        String checkoutPlayer = playerStateService.getCheckoutPlayer();
        SettingsConfig settingsConfig = configManagerService.getSettingsConfig();
        String receivedText = chatService.formatReceivedText(event.getMessage().getString());
        if (receivedText == null) return;

        if (startingCheckout) {
            if (receivedText.equals("Игрок заморожен!")) startingCheckout = false;
            else if (receivedText.equals("Игрок не найден!")) {
                startingCheckout = false;
                checkoutsService.endCheckOut(true);
            }
        }

        if (!checkoutPlayer.isEmpty() && settingsConfig.isAutoBanEnabled() && receivedText.startsWith("▶ Замороженный игрок %s".formatted(checkoutPlayer))) {
            punishmentsService.punish("/banip", checkoutPlayer, "30d", "2.4 (Лив с проверки)", true);
            checkoutsService.endCheckOut(false);
        }

        if (settingsConfig.isAutoAnyDeskEnabled() && !checkoutPlayer.isEmpty() && receivedText.contains(checkoutPlayer)) {
            String chatText;
            String msgText;
            if (receivedText.startsWith("[%s ->".formatted(checkoutPlayer)) && chatService.checkCorrectLong(msgText = receivedText.split("я]", 0)[1].replace(" ", StringUtils.EMPTY)) && msgText.length() >= 9 && msgText.length() <= 11) {
                chatService.copyToClipboard(msgText);
                notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD), "Скопирован анидеск из лс: %s".formatted(msgText), 5f);
            } else if (chatService.checkCorrectLong(chatText = receivedText.split(":")[1].replace(" ", StringUtils.EMPTY)) && chatText.length() >= 9 && chatText.length() <= 11) {
                chatService.copyToClipboard(chatText);
                notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD), "Скопирован анидеск из чата: %s".formatted(chatText), 5f);
            }
        }

        if (banChecking) {
            if (receivedText.equals("Цель не забанена!") || receivedText.equals("История не найдена.")) {
                event.setCancelled(true);
                notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD), "Проверка не была закончена, т.к. не удалось определить причину бана игрока. Пожалуйста, допишите причину вручную.", 5f);
                banChecking = false;
            }
            if (receivedText.startsWith("Игрок [")) messageIsCheckbanInfo = true;
            if (receivedText.startsWith("Причина:")) banReason = receivedText.split("Причина: ")[1].split(" \\| ")[0];
            if (messageIsCheckbanInfo) event.setCancelled(true);
            if (receivedText.startsWith("IP бан:")) {
                messageIsCheckbanInfo = false;
                banChecking = false;
                asyncExecutor.runAsync("CheckoutsModule/onMessageReceive", () -> netService.endCheckout("ban", banReason, destroyStash));
            }
        }
    }

    @Override
    public int getRenderPriority() {
        return 1002;
    }
}