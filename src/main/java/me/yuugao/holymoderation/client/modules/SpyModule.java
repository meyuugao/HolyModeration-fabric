package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.*;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;
import me.yuugao.holymoderation.client.util.serviceLocator.service.StateService;

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
                if (!stateService.getSpyPlayer().isEmpty()) {
                    endSpy();
                } else {
                    loggerService.printError("Вы никого не отслеживаете.");
                }
                return;
            }

            if (stateService.isInHub()) {
                loggerService.printError("В хабе этого делать нельзя.");
                return;
            }

            if (!stateService.getSpyPlayer().isEmpty()) {
                loggerService.printError("Вы уже следите за кем-то --> " + GOLD + BOLD + ".spy" + WHITE + RED + BOLD + ".");
                return;
            }

            startSpy(messageSplit[1]);
        }
    }

    @Subscribe
    public void onMessageReceive(MessageReceiveEvent event) {
        String receivedText = chatService.formatReceivedText(event.getMessage().getString());
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

            if (receivedText.startsWith("Игрок") && !receivedText.startsWith("Игрок " + stateService.getModerNickname())) {
                event.setCancelled(true);
                if (receivedText.equals("Игрок оффлайн")) {
                    stateService.setSpyPlayerStatus("offline");
                } else if (receivedText.split("сервере ")[1].startsWith("lobby")) {
                    stateService.setSpyPlayerStatus("lobby");
                } else {
                    stateService.setSpyPlayerStatus(chatService.formatLocation(receivedText.split("сервере ")[1]));
                }
                stateService.setSpyPlayerActivity(StringUtils.EMPTY);
                checkingSpy = false;
                shouldUpdate = true;
            }

            if (receivedText.startsWith("Текущая")) {
                String spyPlayerLocation = receivedText.split(": ")[1].substring(1, receivedText.split(": ")[1].length() - 1);
                if (spyPlayerLocation.equals("Оффлайн")) {
                    stateService.setSpyPlayerStatus("offline");
                    stateService.setSpyPlayerActivity(StringUtils.EMPTY);
                    instantUpdate = true;
                } else {
                    if (lastKnownLocation.isEmpty()) {
                        lastKnownLocation = spyPlayerLocation;
                    } else if (!spyPlayerLocation.equals(lastKnownLocation)) {
                        stateService.setSpyPlayerStatus(StringUtils.EMPTY);
                        lastKnownLocation = spyPlayerLocation;
                    }
                }
            }

            if (receivedText.startsWith("Последняя") && !stateService.getSpyPlayerStatus().isEmpty()) {
                stateService.setSpyPlayerActivity(receivedText.split(": ")[1]);
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
                instantUpdate = instantUpdate || (stateService.getModerLocation().equals(stateService.getSpyPlayerStatus()) && stateService.getSpyPlayerActivity().isEmpty());
                schedulerService.getInstance().schedule(this::update, instantUpdate ? 500 : configManager.getConfig().getSpyDelay(), instantUpdate ? TimeUnit.MILLISECONDS : TimeUnit.SECONDS);
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
        if (!stateService.getSpyPlayer().isEmpty()) {
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
        render2DService.renderRoundedRect(matrixStack, (float) (Math.floor((float) drawContext.getScaledWindowWidth() / 2 - windowWidth / 2) - 0.5f), y1 - ((windowHeight - blockHeight) / 2), windowWidth, windowHeight, 5f, new Color(0x4666FFFF));
        render2DService.renderText(textRenderer, texts[0], textX, y1, 0xffffffff, false, drawContext);
        render2DService.renderText(textRenderer, texts[1], textX, y2, 0xffffffff, false, drawContext);
    }

    private static String @NotNull [] getStringsToRender() {
        StateService stateService = ServiceLocator.getStateService();
        String[] texts = new String[]{"", ""};
        if (!stateService.getSpyPlayer().isEmpty()) {
            if (stateService.getSpyPlayerStatus().isEmpty()) {
                texts = new String[]{stateService.getSpyPlayer(), ""};
            } else {
                texts = switch (stateService.getSpyPlayerStatus()) {
                    case ("stop") ->
                            new String[]{"Слежка за игроком " + stateService.getSpyPlayer() + " была приостановлена", ""};
                    case ("offline") -> new String[]{"Игрок " + stateService.getSpyPlayer() + " оффлайн", ""};
                    case ("lobby") -> new String[]{"Игрок " + stateService.getSpyPlayer() + " находится в лобби", ""};
                    default ->
                            new String[]{"Игрок " + stateService.getSpyPlayer() + " находится на " + stateService.getSpyPlayerStatus(),
                                    stateService.getSpyPlayerActivity() == null ? "" : "Активность: " + stateService.getSpyPlayerActivity()};
                };
            }
        }
        return texts;
    }

    private void tryInit() {
        schedulerService.getInstance().schedule(() -> {
            if (stateService.isGameInitCompleted()) {
                if (enabled) {
                    if (stateService.isInHub()) {
                        stateService.setSpyPlayerActivity(lastKnownLocation = StringUtils.EMPTY);
                        stateService.setSpyPlayerStatus("stop");
                        loggerService.printSuccess("Слежка приостановлена.");
                    } else {
                        if (!stateService.getModerLocation().isEmpty()) {
                            update();
                            loggerService.printSuccess("Слежка возобновлена.");
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
        stateService.setSpyPlayer(player);
        schedulerService.getInstance().schedule(() -> {
            enabled = true;
            loggerService.printSuccess("Слежка начата");
            update();
        }, 250, TimeUnit.MILLISECONDS);
    }

    private void update() {
        if (enabled) {
            if (!stateService.getPlayer().isEmpty()) {
                endSpy();
                return;
            }

            checkingSpy = true;
            if (!stateService.isInHub() && stateService.isGameInitCompleted()) {
                if (!stateService.getModerLocation().equals(stateService.getSpyPlayerStatus())) {
                    stateService.setSpyPlayerActivity(StringUtils.EMPTY);
                    chatService.chatMessage("/find " + stateService.getSpyPlayer());
                } else {
                    chatService.chatMessage("/playtime " + stateService.getSpyPlayer());
                }
            }
        }
    }

    private void endSpy() {
        if (!stateService.getSpyPlayer().isEmpty()) {
            enabled = false;
            lastKnownLocation = StringUtils.EMPTY;
            stateService.setSpyPlayer(StringUtils.EMPTY);
            stateService.setSpyPlayerActivity(StringUtils.EMPTY);
            stateService.setSpyPlayerStatus(StringUtils.EMPTY);
            loggerService.printSuccess("Слежка остановлена.");
        }
    }
}