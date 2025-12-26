package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.eventbus.event.MessageReceiveEvent;
import me.yuugao.holymoderation.client.eventbus.event.MessageSendEvent;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import com.google.common.eventbus.Subscribe;

public class CheckoutsModule extends Module {
    private final String[] FreezerCommands = {"/sban", "/freezing", "/frz", "/unfreezing", "/unfrz", ".freezing", ".frz", ".sendtexts"};
    private final String[] ApiCommands = {"/startcheckout", "/endcheckout"};

    private boolean banChecking = false;
    private boolean destroyStash;
    private boolean messageIsCheckbanInfo;
    private String banReason;

    @Subscribe
    public void onMessageSend(MessageSendEvent event) {
        String message = event.getContent();
        String command = message.split(" ")[0];
        if (serviceContext.getChatService().isArrayContains(FreezerCommands, command) || serviceContext.getChatService().isArrayContains(ApiCommands, command)) {
            event.setCancelled(true);
            if (serviceContext.getStateService().isInHub()) {
                serviceContext.getLoggerService().printError("В хабе этого делать нельзя.");
                return;
            }
        }
        if (serviceContext.getChatService().isArrayContains(FreezerCommands, command)) {
            String[] messageSplit;
            switch (command) {
                case ("/freezing"):
                case ("/frz"): {
                    messageSplit = message.split(" ", 2);
                    if (messageSplit.length < 2) {
                        serviceContext.getLoggerService().printError("Вы не указали ник игрока.");
                        return;
                    }
                    if (!serviceContext.getStateService().getPlayer().isEmpty()) {
                        serviceContext.getLoggerService().printError("Вы уже проверяете какого-то игрока. Сначала закончите текущую проверку --> " + GOLD + GOLD + BOLD + "/unfreezing" + WHITE + RED + BOLD + " или " + GOLD + GOLD + BOLD + "/unfrz" + WHITE);
                        return;
                    }
                    serviceContext.getCheckoutsService().startCheckOut(messageSplit[1], serviceContext);
                    break;
                }

                case ("/unfreezing"):
                case ("/unfrz"): {
                    if (serviceContext.getStateService().getPlayer().isEmpty()) {
                        serviceContext.getLoggerService().printError("Вы никого не проверяете.");
                        return;
                    }
                    serviceContext.getCheckoutsService().endCheckOut(serviceContext);
                    break;
                }

                case ("/sban"): {
                    messageSplit = message.split(" ", 3);
                    if (serviceContext.getStateService().getPlayer().isEmpty()) {
                        serviceContext.getLoggerService().printError("Вы никого не проверяете.");
                        return;
                    }
                    switch (messageSplit.length) {
                        case (1):
                            serviceContext.getLoggerService().printError("Вы не указали время и причину бана.");
                            return;
                        case (2):
                            serviceContext.getLoggerService().printError("Вы не указали причину бана.");
                            return;
                    }
                    String time = messageSplit[1];
                    String reason = messageSplit[2];
                    if (!serviceContext.getPunishmentsService().punish("/banip", serviceContext.getStateService().getPlayer(), time, "2.4 (" + reason + ")", true, serviceContext)) {
                        return;
                    }
                    serviceContext.getCheckoutsService().endCheckOut(serviceContext);
                    break;
                }

                case (".freezing"):
                case (".frz"): {
                    messageSplit = message.split(" ", 2);
                    if (messageSplit.length == 1) {
                        serviceContext.getLoggerService().printError("Вы не указали ник игрока.");
                        return;
                    }
                    String player = messageSplit[1];
                    if (player.equals(serviceContext.getStateService().getPlayer())) {
                        serviceContext.getLoggerService().printError("Этот игрок находиться у вас на проверке. Для его разморозки используйте " + GOLD + GOLD + BOLD + "/unfreezing" + WHITE + RED + BOLD + " или " + GOLD + GOLD + BOLD + "/unfrz" + WHITE);
                        return;
                    }
                    serviceContext.getChatService().chatMessage("/freezing " + player);
                    break;
                }

                case (".sendtexts"): {
                    messageSplit = message.split(" ", 2);
                    if (messageSplit.length == 1) {
                        serviceContext.getLoggerService().printError("Вы не указали ник игрока.");
                    }
                    serviceContext.getCheckoutsService().sendTexts(messageSplit[1], serviceContext);
                    break;
                }
            }
        } else if (serviceContext.getChatService().isArrayContains(ApiCommands, command)) {
            String[] messageSplit;
            switch (command) {
                case ("/startcheckout"): {
                    messageSplit = message.split(" ", 3);
                    switch (messageSplit.length) {
                        case (1):
                            serviceContext.getLoggerService().printError("Вы не указали ник игрока и причину проверки.");
                            return;
                        case (2):
                            serviceContext.getLoggerService().printError("Вы не указали причину проверки.");
                            return;
                    }
                    String player = messageSplit[1];
                    String reason = messageSplit[2];
                    if (!Arrays.stream(new String[]{"report", "checkout", "autobuy", "autosell", "customka", "personal", "toManyChecks", "candidate"}).toList().contains(reason)) {
                        serviceContext.getLoggerService().printError("Некорректная причина проверки.");
                        return;
                    }
                    String loc = serviceContext.getStateService().getModerLocation();
                    String mode = loc.startsWith("lite") ? "lite" : loc.startsWith("lite120") ? "lite120" : loc.startsWith("classic") ? "classic" : "lpvp";
                    CompletableFuture.runAsync(() -> {
                        if (mode.equals("lpvp")) {
                            serviceContext.getNetService().startCheckout(player, reason, "lite", 1, true);
                        } else {
                            int number = Integer.parseInt(loc.split(mode + "-")[1]);
                            serviceContext.getNetService().startCheckout(player, reason, mode, number, false);
                        }
                    });
                    break;
                }
                case ("/endcheckout"): {
                    messageSplit = message.split(" ", 5);
                    if (messageSplit.length == 1) {
                        serviceContext.getLoggerService().printError("Вы не указали результат проверки.");
                        return;
                    }
                    String result = messageSplit[1];
                    if (!Arrays.stream(new String[]{"clean", "ban", "autobuy", "autosell"}).toList().contains(result)) {
                        serviceContext.getLoggerService().printError("Некорректный результат проверки.");
                        return;
                    }
                    CompletableFuture.runAsync(() -> {
                        switch (result) {
                            case ("clean"): {
                                serviceContext.getNetService().endCheckout(result, result, false);
                                break;
                            }
                            case ("ban"): {
                                if (messageSplit.length == 2) {
                                    serviceContext.getLoggerService().printError("Вы не указали ник игрока и необходимость снести стеш.");
                                    return;
                                } else if (messageSplit.length == 3) {
                                    serviceContext.getLoggerService().printError("Вы не указали необходимость снести стеш.");
                                    return;
                                }

                                if (!Arrays.stream(new String[]{"true", "false"}).toList().contains(messageSplit[3])) {
                                    serviceContext.getLoggerService().printError("Некорректная необходимость снести стеш.");
                                    return;
                                }
                                destroyStash = messageSplit[3].equals("true");

                                if (messageSplit.length == 4) {
                                    banChecking = true;
                                    serviceContext.getChatService().chatMessage("/checkban " + messageSplit[2]);
                                } else if (messageSplit.length == 5) {
                                    serviceContext.getNetService().endCheckout(result, messageSplit[4], destroyStash);
                                }
                                break;
                            }
                            case ("autobuy"):
                            case ("autosell"): {
                                serviceContext.getNetService().endCheckout(result, result, true);
                                break;
                            }
                        }
                    });
                    break;
                }
            }
        }
    }

    @Subscribe
    public void onMessageReceive(MessageReceiveEvent event) {
        String receivedText = serviceContext.getChatService().formatReceivedText(event.getMessage().getString());
        if (receivedText == null) {
            return;
        }

        if (!serviceContext.getStateService().getPlayer().isEmpty() && serviceContext.getConfigManager().getConfig().isAutoBanEnabled()) {
            if (receivedText.startsWith("▶ Замороженный игрок " + serviceContext.getStateService().getPlayer())) {
                serviceContext.getPunishmentsService().punish("/banip", serviceContext.getStateService().getPlayer(), "30d", "2.4 (Лив с проверки)", true, serviceContext);
                serviceContext.getCheckoutsService().endCheckOut(serviceContext);
            }
        }

        if (serviceContext.getConfigManager().getConfig().isAutoAnyDeskEnabled() && !serviceContext.getStateService().getPlayer().isEmpty() && receivedText.contains(serviceContext.getStateService().getPlayer())) {
            String chatText;
            String msgText;
            if (receivedText.startsWith("[" + serviceContext.getStateService().getPlayer() + " ->") && serviceContext.getChatService().checkCorrectLong(msgText = receivedText.split("я]", 0)[1].replace(" ", "")) && msgText.length() >= 9 && msgText.length() <= 11) {
                serviceContext.getChatService().copyToClipboard(msgText);
            }
            if (serviceContext.getChatService().checkCorrectLong(chatText = receivedText.split(":")[1].replace(" ", "")) && chatText.length() >= 9 && chatText.length() <= 11) {
                serviceContext.getChatService().copyToClipboard(chatText);
            }
        }

        if (banChecking) {
            if (receivedText.equals("Цель не забанена!") || receivedText.equals("История не найдена.")) {
                event.setCancelled(true);
                serviceContext.getLoggerService().printError("Проверка не была закончена, т.к. не удалось определить причину бана игрока. Пожалуйста, допишите причину вручную.");
                banChecking = false;
            }

            if (receivedText.startsWith("Игрок [")) {
                messageIsCheckbanInfo = true;
            }

            if (receivedText.startsWith("Причина:")) {
                banReason = receivedText.split("Причина: ")[1].split(" \\| ")[0];
            }

            if (messageIsCheckbanInfo) {
                event.setCancelled(true);
            }

            if (receivedText.startsWith("IP бан:")) {
                messageIsCheckbanInfo = false;
                banChecking = false;
                CompletableFuture.runAsync(() -> serviceContext.getNetService().endCheckout("ban", banReason, destroyStash));
            }
        }
    }
}