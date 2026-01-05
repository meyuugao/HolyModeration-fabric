package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.*;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.NotificationType;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.util.concurrent.TimeUnit;

public class SpyModule extends Module {
    private boolean enabled = false;
    private boolean checkingSpy = false;
    private boolean processingPlaytimeInfo = false;
    private boolean shouldUpdate = false;
    private boolean instantUpdate = false;
    private String lastKnownLocation = StringUtils.EMPTY;

    private float anim = 0f;
    private float animTarget = 0f;
    private float currentWidth = 1f;
    private float currentHeight = 1f;
    private String display0 = StringUtils.EMPTY;
    private String display1 = StringUtils.EMPTY;
    private boolean clearDisplayWhenHidden = false;

    public SpyModule(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Subscribe
    public void onCommandSend(CommandSendEvent event) {
        String eventCommand = event.getCommand();
        String[] commandSplit = eventCommand.split(" ", 3);
        if (!eventCommand.startsWith("hm") || commandSplit.length < 2) return;

        if (commandSplit[1].equals("spy")) {
            if (commandSplit.length == 2) {
                if (!serviceContext.getStateService().getSpyPlayer().isEmpty()) {
                    endSpy();
                } else {
                    serviceContext.getNotificationService().addNotification(NotificationType.WARNING, GOLD + BOLD + "Предупреждение", "Вы никого не отслеживаете.", 5f);
                }
                return;
            }

            if (serviceContext.getStateService().isInHub()) {
                serviceContext.getNotificationService().addNotification(NotificationType.WARNING, GOLD + BOLD + "Предупреждение", "В хабе этого делать нельзя.", 5f);
                return;
            }

            if (!serviceContext.getStateService().getPlayer().isEmpty() && serviceContext.getStateService().getPlayer().equals(commandSplit[2])) {
                serviceContext.getNotificationService().addNotification(NotificationType.WARNING, GOLD + BOLD + "Предупреждение", "Вы не можете начать следить за игроком на вашей проверке.", 5f);
                return;
            }

            if (!serviceContext.getStateService().getSpyPlayer().isEmpty()) {
                serviceContext.getNotificationService().addNotification(NotificationType.WARNING, GOLD + BOLD + "Предупреждение", "Вы уже следите за кем-то --> " + GOLD + BOLD + "/hm spy" + WHITE + RED + BOLD + ".", 5f);
                return;
            }

            startSpy(commandSplit[2]);
        } else if (commandSplit[1].equals("spyfrz")) {
            if (serviceContext.getStateService().isInHub()) {
                serviceContext.getNotificationService().addNotification(NotificationType.WARNING, GOLD + BOLD + "Предупреждение", "В хабе этого делать нельзя.", 5f);
                return;
            }

            if (serviceContext.getStateService().getSpyPlayer().isEmpty()) {
                serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы ни за кем не следите.", 5f);
                return;
            }

            if (serviceContext.getCheckoutsService().startCheckOut(serviceContext.getStateService().getSpyPlayer(), serviceContext)) {
                endSpy();
            }
        }
    }

    @Subscribe(priority = 98)
    public void onMessageReceive(MessageReceiveEvent event) {
        String receivedText = serviceContext.getChatService().formatReceivedText(event.getMessage().getString());
        if (receivedText == null) return;

        if (checkingSpy) {
            if (receivedText.startsWith("----------")) {
                if (processingPlaytimeInfo) {
                    checkingSpy = false;
                    shouldUpdate = true;
                    processingPlaytimeInfo = false;
                } else processingPlaytimeInfo = true;
            }

            if (receivedText.startsWith("Игрок") && !receivedText.startsWith("Игрок " + serviceContext.getStateService().getUserNickname())) {
                event.setCancelled(true);
                if (receivedText.equals("Игрок оффлайн"))
                    serviceContext.getStateService().setSpyPlayerStatus("offline");
                else if (receivedText.split("сервере ")[1].startsWith("lobby"))
                    serviceContext.getStateService().setSpyPlayerStatus("lobby");
                else
                    serviceContext.getStateService().setSpyPlayerStatus(serviceContext.getChatService().formatLocation(receivedText.split("сервере ")[1]));
                serviceContext.getStateService().setSpyPlayerActivity(StringUtils.EMPTY);
                checkingSpy = false;
                shouldUpdate = true;
            }

            if (receivedText.startsWith("Текущая")) {
                String loc = receivedText.split(": ")[1];
                loc = loc.substring(1, loc.length() - 1);
                if (loc.equals("Оффлайн")) {
                    serviceContext.getStateService().setSpyPlayerStatus("offline");
                    serviceContext.getStateService().setSpyPlayerActivity(StringUtils.EMPTY);
                    instantUpdate = true;
                } else {
                    if (lastKnownLocation.isEmpty()) lastKnownLocation = loc;
                    else if (!loc.equals(lastKnownLocation)) {
                        serviceContext.getStateService().setSpyPlayerStatus(StringUtils.EMPTY);
                        lastKnownLocation = loc;
                    }
                }
            }

            if (receivedText.startsWith("Последняя") && !serviceContext.getStateService().getSpyPlayerStatus().isEmpty()) {
                serviceContext.getStateService().setSpyPlayerActivity(receivedText.split(": ")[1]);
            }

            if (receivedText.startsWith("Активность") || receivedText.startsWith("Общее время") ||
                    receivedText.startsWith("Текущая") || receivedText.startsWith("Время") ||
                    receivedText.startsWith("Последняя") || receivedText.startsWith("Последний") ||
                    receivedText.startsWith("----------") || receivedText.isEmpty()) {
                event.setCancelled(true);
            }

            if (shouldUpdate) {
                instantUpdate |= serviceContext.getStateService().getUserLocation().equals(serviceContext.getStateService().getSpyPlayerStatus())
                        && serviceContext.getStateService().getSpyPlayerActivity().isEmpty();
                serviceContext.getSchedulerService().getInstance().schedule(this::update, instantUpdate ? 500 : serviceContext.getConfigManager().getConfig().getSpyDelay(), instantUpdate ? TimeUnit.MILLISECONDS : TimeUnit.SECONDS);
                shouldUpdate = instantUpdate = false;
            }
        }
    }

    @Subscribe
    public void onServerConnect(ServerConnectEvent event) {
        if (event.isSwitch()) tryInit();
    }

    @Subscribe
    public void onServerDisconnect(ServerDisconnectEvent event) {
        if (!serviceContext.getStateService().getSpyPlayer().isEmpty()) {
            endSpy();
        }
    }

    @Subscribe(priority = 100)
    public void onHudRender(HudRenderEvent event) {
        anim += (animTarget - anim) * 0.15f;
        if (anim < 0.01f && display0.isEmpty() && display1.isEmpty()) return;

        String[] current = getStringsToRender();
        if (!(current[0].isEmpty() && current[1].isEmpty())) {
            display0 = current[0];
            display1 = current[1];
        }

        DrawContext ctx = event.getDrawContext();
        TextRenderer tr = serviceContext.getMinecraftService().getClient().textRenderer;
        MatrixStack ms = ctx.getMatrices();

        int w1 = tr.getWidth(display0);
        int w2 = tr.getWidth(display1);
        float targetWidth = Math.max(w1, w2) + 16;

        int lines = display1.isEmpty() ? 1 : 2;
        int textBlockHeight = lines * tr.fontHeight + (lines == 2 ? 4 : 0);
        float targetHeight = textBlockHeight + 12;

        currentWidth += (targetWidth - currentWidth) * 0.2f;
        currentHeight += (targetHeight - currentHeight) * 0.2f;

        float width = Math.max(1f, currentWidth * anim);
        float height = Math.max(1f, currentHeight * anim);

        float cx = ctx.getScaledWindowWidth() / 2f;
        float x = cx - width / 2f;
        float y = 30f;

        Color bg = new Color(10, 20, 40, 220);
        Color outline = new Color(60, 120, 220);

        serviceContext.getRender2DService().renderSoftRoundedRectOutline(
                ms, x, y, width, height, 10f, bg, outline, 1.5f, 3
        );

        ms.push();
        ms.translate(cx, y + height / 2f, 0);
        ms.scale(anim, anim, 1f);

        float textY = (float) -textBlockHeight / 2 + 0.5f;

        serviceContext.getRender2DService().renderText(
                tr,
                display0,
                -tr.getWidth(display0) / 2f,
                textY,
                0xffffffff,
                false,
                ctx
        );

        if (!display1.isEmpty()) {
            serviceContext.getRender2DService().renderText(
                    tr,
                    display1,
                    -tr.getWidth(display1) / 2f,
                    textY + tr.fontHeight + 4,
                    0xffffffff,
                    false,
                    ctx
            );
        }

        ms.pop();

        if (anim < 0.02f && animTarget == 0f && clearDisplayWhenHidden) {
            display0 = display1 = StringUtils.EMPTY;
            clearDisplayWhenHidden = false;
        }
    }

    private String @NotNull [] getStringsToRender() {
        String spyPlayer = serviceContext.getStateService().getSpyPlayer();
        if (spyPlayer.isEmpty()) return new String[]{"", ""};

        if (serviceContext.getStateService().getSpyPlayerStatus().isEmpty())
            return new String[]{spyPlayer, ""};

        return switch (serviceContext.getStateService().getSpyPlayerStatus()) {
            case "stop" -> new String[]{"Слежка приостановлена", ""};
            case "offline" -> new String[]{"Игрок " + spyPlayer + " оффлайн", ""};
            case "lobby" -> new String[]{"Игрок " + spyPlayer + " в лобби", ""};
            default -> new String[]{
                    "Игрок " + spyPlayer + " находится на " + serviceContext.getStateService().getSpyPlayerStatus(),
                    serviceContext.getStateService().getSpyPlayerActivity() == null ? "" : "Активность: " + serviceContext.getStateService().getSpyPlayerActivity()
            };
        };
    }

    private void tryInit() {
        serviceContext.getSchedulerService().getInstance().schedule(() -> {
            if (serviceContext.getStateService().isGameInitCompleted()) {
                if (enabled) {
                    if (serviceContext.getStateService().isInHub()) {
                        serviceContext.getStateService().setSpyPlayerActivity(lastKnownLocation = StringUtils.EMPTY);
                        serviceContext.getStateService().setSpyPlayerStatus("stop");
                        serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Слежка приостановлена.", 5f);
                    } else {
                        if (!serviceContext.getStateService().getUserLocation().isEmpty()) {
                            update();
                            serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Слежка возобновлена.", 5f);
                        } else {
                            tryInit();
                        }
                    }
                }
            } else {
                tryInit();
            }
        }, 50, TimeUnit.MILLISECONDS);
    }

    private void startSpy(String player) {
        resetSpy();
        serviceContext.getStateService().setSpyPlayer(player);
        enabled = true;
        animTarget = 1f;
        display0 = getStringsToRender()[0];
        display1 = getStringsToRender()[1];
        currentWidth = Math.max(1f, currentWidth);
        currentHeight = Math.max(1f, currentHeight);
        serviceContext.getSchedulerService().getInstance().schedule(this::update, 250, TimeUnit.MILLISECONDS);
        serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Слежка начата", 5f);
    }

    private void endSpy() {
        resetSpy();
        serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Слежка остановлена.", 5f);
    }

    private void resetSpy() {
        serviceContext.getStateService().setSpyPlayer(StringUtils.EMPTY);
        enabled = false;
        animTarget = 0f;
        display0 = getStringsToRender()[0];
        display1 = getStringsToRender()[1];
        lastKnownLocation = StringUtils.EMPTY;
        serviceContext.getStateService().setSpyPlayerActivity(StringUtils.EMPTY);
        serviceContext.getStateService().setSpyPlayerStatus(StringUtils.EMPTY);
        clearDisplayWhenHidden = true;
    }

    private void update() {
        if (!enabled) return;

        checkingSpy = true;
        if (!serviceContext.getStateService().isInHub() && serviceContext.getStateService().isGameInitCompleted()) {
            if (!serviceContext.getStateService().getUserLocation().equals(serviceContext.getStateService().getSpyPlayerStatus()))
                serviceContext.getChatService().chatMessage("/find " + serviceContext.getStateService().getSpyPlayer());
            else
                serviceContext.getChatService().chatMessage("/playtime " + serviceContext.getStateService().getSpyPlayer());
        }
    }
}