package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.CommandSendEvent;
import me.yuugao.holymoderation.client.eventbus.event.HudRenderEvent;
import me.yuugao.holymoderation.client.eventbus.event.MessageReceiveEvent;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;
import me.yuugao.holymoderation.client.util.serviceLocator.service.NotificationType;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

public class CheckoutsModule extends Module {
    private final String[] FreezerCommands = {
            "/freezing", "/frz", "freezing", "frz", "sban", "sendtexts", "unfreezing", "unfrz"
    };

    private final String[] ApiCommands = {
            "endcheckout", "startcheckout"
    };

    private boolean banChecking = false;
    private boolean destroyStash;
    private boolean messageIsCheckbanInfo;
    private String banReason;

    private float coAnim = 0f;
    private float coAnimTarget = 0f;
    private float coCurrentWidth = 1f;
    private float coCurrentHeight = 1f;
    private long checkoutStartMillis = 0L;
    private String coLastPlayer = "";
    private boolean coClearDisplayWhenHidden = false;

    @Subscribe
    public void onCommandSend(CommandSendEvent event) {
        String eventCommand = event.getCommand();
        String[] commandSplit = eventCommand.split(" ");
        if (eventCommand.startsWith("hm") && commandSplit.length < 2) return;

        String command;
        if (eventCommand.startsWith("hm")) {
            command = commandSplit[1];
        } else {
            command = "/" + commandSplit[0];
        }

        if (ServiceLocator.getChatService().isArrayContains(FreezerCommands, command) || ServiceLocator.getChatService().isArrayContains(ApiCommands, command)) {
            if (serviceContext.getStateService().isInHub()) {
                serviceContext.getNotificationService().addNotification(NotificationType.WARNING, GOLD + BOLD + "Предупреждение", "В хабе этого делать нельзя.", 5f);
                return;
            }
        }
        if (ServiceLocator.getChatService().isArrayContains(FreezerCommands, command)) {
            switch (command) {
                case ("/freezing"):
                case ("/frz"): {
                    event.setCancelled(true);
                    commandSplit = eventCommand.split(" ", 2);
                    if (commandSplit.length < 2) {
                        serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы не указали ник игрока.", 5f);
                        return;
                    }

                    if (serviceContext.getCheckoutsService().startCheckOut(commandSplit[1], serviceContext)) {
                        coStartForLocal(commandSplit[1]);
                    }

                    break;
                }

                case ("unfreezing"):
                case ("unfrz"): {
                    if (serviceContext.getStateService().getPlayer().isEmpty()) {
                        serviceContext.getNotificationService().addNotification(NotificationType.WARNING, GOLD + BOLD + "Предупреждение", "Вы никого не проверяете.", 5f);
                        return;
                    }
                    serviceContext.getCheckoutsService().endCheckOut(serviceContext);
                    break;
                }

                case ("sban"): {
                    commandSplit = eventCommand.split(" ", 4);
                    if (serviceContext.getStateService().getPlayer().isEmpty()) {
                        serviceContext.getNotificationService().addNotification(NotificationType.WARNING, GOLD + BOLD + "Предупреждение", "Вы никого не проверяете.", 5f);
                        return;
                    }
                    switch (commandSplit.length) {
                        case (2):
                            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы не указали время и причину бана.", 5f);
                            return;
                        case (3):
                            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы не указали причину бана.", 5f);
                            return;
                    }
                    String time = commandSplit[2];
                    String reason = commandSplit[3];
                    if (!serviceContext.getPunishmentsService().punish("/banip", serviceContext.getStateService().getPlayer(), time, "2.4 (" + reason + ")", true, serviceContext)) {
                        return;
                    }
                    serviceContext.getCheckoutsService().endCheckOut(serviceContext);
                    break;
                }

                case ("freezing"):
                case ("frz"): {
                    commandSplit = eventCommand.split(" ", 3);
                    if (commandSplit.length == 2) {
                        serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы не указали ник игрока.", 5f);
                        return;
                    }
                    String player = commandSplit[2];
                    if (player.equals(serviceContext.getStateService().getPlayer())) {
                        serviceContext.getNotificationService().addNotification(NotificationType.WARNING, GOLD + BOLD + "Предупреждение", "Этот игрок находиться у вас на проверке. Для его разморозки используйте " + GOLD + GOLD + BOLD + "/unfreezing" + WHITE + " или " + GOLD + GOLD + BOLD + "/unfrz" + WHITE, 5f);
                        return;
                    }
                    serviceContext.getChatService().chatMessage("/freezing " + player);
                    break;
                }

                case ("sendtexts"): {
                    commandSplit = eventCommand.split(" ", 3);
                    if (commandSplit.length == 2) {
                        serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы не указали ник игрока.", 5f);
                        return;
                    }
                    serviceContext.getCheckoutsService().sendTexts(commandSplit[2], serviceContext);
                    break;
                }
            }
        } else if (ServiceLocator.getChatService().isArrayContains(ApiCommands, command)) {
            String[] messageSplit;
            switch (command) {
                case ("startcheckout"): {
                    messageSplit = eventCommand.split(" ", 4);
                    switch (messageSplit.length) {
                        case (2):
                            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы не указали ник игрока и причину проверки.", 5f);
                            return;
                        case (3):
                            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы не указали причину проверки.", 5f);
                            return;
                    }
                    String player = messageSplit[2];
                    String reason = messageSplit[3];
                    if (!Arrays.stream(new String[]{"report", "checkout", "autobuy", "autosell", "customka", "personal", "toManyChecks", "candidate"}).toList().contains(reason)) {
                        serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Некорректная причина проверки.", 5f);
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
                    coStartForLocal(player);
                    break;
                }
                case ("endcheckout"): {
                    messageSplit = eventCommand.split(" ", 6);
                    if (messageSplit.length == 2) {
                        serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы не указали результат проверки.", 5f);
                        return;
                    }
                    String result = messageSplit[2];
                    if (!Arrays.stream(new String[]{"clean", "ban", "autobuy", "autosell"}).toList().contains(result)) {
                        serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Некорректный результат проверки.", 5f);
                        return;
                    }
                    CompletableFuture.runAsync(() -> {
                        switch (result) {
                            case ("clean"): {
                                serviceContext.getNetService().endCheckout(result, result, false);
                                break;
                            }
                            case ("ban"): {
                                if (messageSplit.length == 3) {
                                    serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы не указали ник игрока и необходимость снести стеш.", 5f);
                                    return;
                                } else if (messageSplit.length == 4) {
                                    serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы не указали необходимость снести стеш.", 5f);
                                    return;
                                }

                                if (!Arrays.stream(new String[]{"true", "false"}).toList().contains(messageSplit[4])) {
                                    serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Некорректная необходимость снести стеш.", 5f);
                                    return;
                                }
                                destroyStash = messageSplit[4].equals("true");

                                if (messageSplit.length == 5) {
                                    banChecking = true;
                                    serviceContext.getChatService().chatMessage("/checkban " + messageSplit[3]);
                                } else if (messageSplit.length == 6) {
                                    serviceContext.getNetService().endCheckout(result, messageSplit[5], destroyStash);
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
                    coStopLocal();
                    break;
                }
            }
        }
    }

    @Subscribe
    public void onMessageReceive(MessageReceiveEvent event) {
        String receivedText = serviceContext.getChatService().formatReceivedText(event.getMessage().getString());
        if (receivedText == null) return;

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
                ServiceLocator.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Скопирован анидеск из лс: " + msgText, 5f);
            } else if (serviceContext.getChatService().checkCorrectLong(chatText = receivedText.split(":")[1].replace(" ", "")) && chatText.length() >= 9 && chatText.length() <= 11) {
                serviceContext.getChatService().copyToClipboard(chatText);
                ServiceLocator.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Скопирован анидеск из чата: " + chatText, 5f);
            }
        }

        if (banChecking) {
            if (receivedText.equals("Цель не забанена!") || receivedText.equals("История не найдена.")) {
                event.setCancelled(true);
                serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Проверка не была закончена, т.к. не удалось определить причину бана игрока. Пожалуйста, допишите причину вручную.", 5f);
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

    @Subscribe
    public void onHudRender(HudRenderEvent event) {
        coAnim += (coAnimTarget - coAnim) * 0.15f;

        String player = serviceContext.getStateService().getPlayer();
        if (!coLastPlayer.equals(player)) {
            if (coLastPlayer.isEmpty() && !player.isEmpty()) {
                checkoutStartMillis = System.currentTimeMillis();
                coAnimTarget = 1f;
            } else if (!coLastPlayer.isEmpty() && player.isEmpty()) {
                coAnimTarget = 0f;
                coClearDisplayWhenHidden = true;
            }
            coLastPlayer = player;
        }

        if (coAnim < 0.01f && player.isEmpty()) return;

        DrawContext ctx = event.getDrawContext();
        TextRenderer tr = serviceContext.getMinecraftService().getClient().textRenderer;
        MatrixStack ms = ctx.getMatrices();

        long elapsedSec = checkoutStartMillis == 0L ? 0L : (System.currentTimeMillis() - checkoutStartMillis) / 1000L;
        long minutes = elapsedSec / 60L;
        long seconds = elapsedSec % 60L;
        String timeText = String.format("%d:%02d", minutes, seconds);
        String display = "Текущая проверка: " + (player.isEmpty() ? coLastPlayer : player) + " | " + timeText;

        int w = tr.getWidth(display);
        float targetWidth = w + 16f;
        float targetHeight = tr.fontHeight + 12f;

        coCurrentWidth += (targetWidth - coCurrentWidth) * 0.2f;
        coCurrentHeight += (targetHeight - coCurrentHeight) * 0.2f;

        float width = Math.max(1f, coCurrentWidth * coAnim);
        float height = Math.max(1f, coCurrentHeight * coAnim);

        float cx = ctx.getScaledWindowWidth() / 2f;
        float x = cx - width / 2f;
        float y = ctx.getScaledWindowHeight() - 90f;

        Color bg = new Color(10, 20, 40, 220);
        Color outline = new Color(60, 120, 220);

        serviceContext.getRender2DService().renderSoftRoundedRectOutline(ms, x, y, width, height, 10f, bg, outline, 1.5f, 3);

        ms.push();
        ms.translate(cx, y + height / 2f, 0);
        ms.scale(coAnim, coAnim, 1f);

        float textBlockHeight = tr.fontHeight;
        float textY = -textBlockHeight / 2f + 0.5f;

        serviceContext.getRender2DService().renderText(tr, display, -tr.getWidth(display) / 2f, textY, 0xffffffff, false, ctx);

        ms.pop();

        if (coAnim < 0.02f && coAnimTarget == 0f && coClearDisplayWhenHidden) {
            checkoutStartMillis = 0L;
            coClearDisplayWhenHidden = false;
            coLastPlayer = "";
        }
    }

    private void coStartForLocal(String player) {
        checkoutStartMillis = System.currentTimeMillis();
        coLastPlayer = player;
        coAnimTarget = 1f;
    }

    private void coStopLocal() {
        coAnimTarget = 0f;
        coClearDisplayWhenHidden = true;
    }
}