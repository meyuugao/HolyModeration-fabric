package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.*;

import net.minecraft.client.MinecraftClient;
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

    @Subscribe
    public void onMessageSend(MessageSendEvent event) {
        String[] messageSplit = event.getContent().split(" ", 2);

        if (messageSplit[0].equals(".spy")) {
            event.setCancelled(true);

            if (messageSplit.length == 1) {
                if (!serviceContext.getStateService().getSpyPlayer().isEmpty()) {
                    endSpy();
                } else {
                    serviceContext.getLoggerService().printError("Вы никого не отслеживаете.");
                }
                return;
            }

            if (serviceContext.getStateService().isInHub()) {
                serviceContext.getLoggerService().printError("В хабе этого делать нельзя.");
                return;
            }

            if (!serviceContext.getStateService().getSpyPlayer().isEmpty()) {
                serviceContext.getLoggerService().printError("Вы уже следите за кем-то --> " + GOLD + BOLD + ".spy" + WHITE + RED + BOLD + ".");
                return;
            }

            startSpy(messageSplit[1]);
        }
    }

    @Subscribe
    public void onMessageReceive(MessageReceiveEvent event) {
        String receivedText = serviceContext.getChatService().formatReceivedText(event.getMessage().getString());
        if (receivedText == null) {
            return;
        }

        if (checkingSpy) {
            if (receivedText.startsWith("----------")) {
                if (processingPlaytimeInfo) {
                    checkingSpy = false;
                    shouldUpdate = true;
                    processingPlaytimeInfo = false;
                } else {
                    processingPlaytimeInfo = true;
                }
            }

            if (receivedText.startsWith("Игрок") && !receivedText.startsWith("Игрок " + serviceContext.getStateService().getModerNickname())) {
                event.setCancelled(true);
                if (receivedText.equals("Игрок оффлайн")) {
                    serviceContext.getStateService().setSpyPlayerStatus("offline");
                } else if (receivedText.split("сервере ")[1].startsWith("lobby")) {
                    serviceContext.getStateService().setSpyPlayerStatus("lobby");
                } else {
                    serviceContext.getStateService().setSpyPlayerStatus(serviceContext.getChatService().formatLocation(receivedText.split("сервере ")[1]));
                }
                serviceContext.getStateService().setSpyPlayerActivity(StringUtils.EMPTY);
                checkingSpy = false;
                shouldUpdate = true;
            }

            if (receivedText.startsWith("Текущая")) {
                String spyPlayerLocation = receivedText.split(": ")[1].substring(1, receivedText.split(": ")[1].length() - 1);
                if (spyPlayerLocation.equals("Оффлайн")) {
                    serviceContext.getStateService().setSpyPlayerStatus("offline");
                    serviceContext.getStateService().setSpyPlayerActivity(StringUtils.EMPTY);
                    instantUpdate = true;
                } else {
                    if (lastKnownLocation.isEmpty()) {
                        lastKnownLocation = spyPlayerLocation;
                    } else if (!spyPlayerLocation.equals(lastKnownLocation)) {
                        serviceContext.getStateService().setSpyPlayerStatus(StringUtils.EMPTY);
                        lastKnownLocation = spyPlayerLocation;
                    }
                }
            }

            if (receivedText.startsWith("Последняя") && !serviceContext.getStateService().getSpyPlayerStatus().isEmpty()) {
                serviceContext.getStateService().setSpyPlayerActivity(receivedText.split(": ")[1]);
            }

            if (receivedText.startsWith("Активность") ||
                    receivedText.startsWith("Общее время") ||
                    receivedText.startsWith("Текущая") ||
                    receivedText.startsWith("Время") ||
                    receivedText.startsWith("Последняя") ||
                    receivedText.startsWith("Последний") ||
                    receivedText.startsWith("----------") ||
                    receivedText.isEmpty()) {
                event.setCancelled(true);
            }
            if (shouldUpdate) {
                instantUpdate = instantUpdate || (serviceContext.getStateService().getModerLocation().equals(serviceContext.getStateService().getSpyPlayerStatus()) && serviceContext.getStateService().getSpyPlayerActivity().isEmpty());
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

    @Subscribe
    public void onHudRender(HudRenderEvent event) {
        String[] texts = getStringsToRender();

        if (texts[0].isEmpty() && texts[1].isEmpty()) return;

        DrawContext drawContext = event.getDrawContext();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        MatrixStack matrixStack = drawContext.getMatrices();

        int textX = drawContext.getScaledWindowWidth() / 2 - textRenderer.getWidth(texts[0]) / 2;
        int y1 = 20;
        int y2 = 30;
        float maxTextWidth = (float) Math.max(textRenderer.getWidth(texts[0]), textRenderer.getWidth(texts[1]));
        float windowWidth = maxTextWidth + 8;
        float blockHeight = (float) (textRenderer.fontHeight * 2 + (y2 - y1 - textRenderer.fontHeight));
        float windowHeight = (float) (blockHeight * 1.3);
        serviceContext.getRender2DService().renderRoundedRect(matrixStack, (float) (Math.floor((float) drawContext.getScaledWindowWidth() / 2 - windowWidth / 2) - 0.5f), y1 - ((windowHeight - blockHeight) / 2), windowWidth, windowHeight, 5f, new Color(0x4666FFFF));
        serviceContext.getRender2DService().renderText(textRenderer, texts[0], textX, y1, 0xffffffff, false, drawContext);
        serviceContext.getRender2DService().renderText(textRenderer, texts[1], textX, y2, 0xffffffff, false, drawContext);
    }

    private String @NotNull [] getStringsToRender() {
        String[] texts = new String[]{"", ""};
        if (!serviceContext.getStateService().getSpyPlayer().isEmpty()) {
            if (serviceContext.getStateService().getSpyPlayerStatus().isEmpty()) {
                texts = new String[]{serviceContext.getStateService().getSpyPlayer(), ""};
            } else {
                texts = switch (serviceContext.getStateService().getSpyPlayerStatus()) {
                    case ("stop") ->
                            new String[]{"Слежка за игроком " + serviceContext.getStateService().getSpyPlayer() + " была приостановлена", ""};
                    case ("offline") ->
                            new String[]{"Игрок " + serviceContext.getStateService().getSpyPlayer() + " оффлайн", ""};
                    case ("lobby") ->
                            new String[]{"Игрок " + serviceContext.getStateService().getSpyPlayer() + " находится в лобби", ""};
                    default ->
                            new String[]{"Игрок " + serviceContext.getStateService().getSpyPlayer() + " находится на " + serviceContext.getStateService().getSpyPlayerStatus(),
                                    serviceContext.getStateService().getSpyPlayerActivity() == null ? "" : "Активность: " + serviceContext.getStateService().getSpyPlayerActivity()};
                };
            }
        }
        return texts;
    }

    private void tryInit() {
        serviceContext.getSchedulerService().getInstance().schedule(() -> {
            if (serviceContext.getStateService().isGameInitCompleted()) {
                if (enabled) {
                    if (serviceContext.getStateService().isInHub()) {
                        serviceContext.getStateService().setSpyPlayerActivity(lastKnownLocation = StringUtils.EMPTY);
                        serviceContext.getStateService().setSpyPlayerStatus("stop");
                        serviceContext.getLoggerService().printSuccess("Слежка приостановлена.");
                    } else {
                        if (!serviceContext.getStateService().getModerLocation().isEmpty()) {
                            update();
                            serviceContext.getLoggerService().printSuccess("Слежка возобновлена.");
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
        endSpy();
        serviceContext.getStateService().setSpyPlayer(player);
        serviceContext.getSchedulerService().getInstance().schedule(() -> {
            enabled = true;
            serviceContext.getLoggerService().printSuccess("Слежка начата");
            update();
        }, 250, TimeUnit.MILLISECONDS);
    }

    private void update() {
        if (enabled) {
            if (!serviceContext.getStateService().getPlayer().isEmpty()) {
                endSpy();
                return;
            }

            checkingSpy = true;
            if (!serviceContext.getStateService().isInHub() && serviceContext.getStateService().isGameInitCompleted()) {
                if (!serviceContext.getStateService().getModerLocation().equals(serviceContext.getStateService().getSpyPlayerStatus())) {
                    serviceContext.getStateService().setSpyPlayerActivity(StringUtils.EMPTY);
                    serviceContext.getChatService().chatMessage("/find " + serviceContext.getStateService().getSpyPlayer());
                } else {
                    serviceContext.getChatService().chatMessage("/playtime " + serviceContext.getStateService().getSpyPlayer());
                }
            }
        }
    }

    private void endSpy() {
        if (!serviceContext.getStateService().getSpyPlayer().isEmpty()) {
            enabled = false;
            lastKnownLocation = StringUtils.EMPTY;
            serviceContext.getStateService().setSpyPlayer(StringUtils.EMPTY);
            serviceContext.getStateService().setSpyPlayerActivity(StringUtils.EMPTY);
            serviceContext.getStateService().setSpyPlayerStatus(StringUtils.EMPTY);
            serviceContext.getLoggerService().printSuccess("Слежка остановлена.");
        }
    }
}