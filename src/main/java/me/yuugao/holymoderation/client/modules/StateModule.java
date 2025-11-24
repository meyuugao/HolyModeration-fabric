package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.HolyModerationClient;
import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.MessageReceiveEvent;
import me.yuugao.holymoderation.client.eventbus.event.MessageSendEvent;
import me.yuugao.holymoderation.client.eventbus.event.ServerConnectEvent;
import me.yuugao.holymoderation.client.eventbus.event.ServerDisconnectEvent;

import net.minecraft.world.GameMode;

import org.apache.commons.lang3.StringUtils;

public class StateModule extends Module {
    private boolean blocked = false;
    private boolean enabled = true;
    private boolean isOnHW = false;
    private boolean needUpdate = false;

    @Subscribe(priority = 100)
    public void onServerConnect(ServerConnectEvent event) {
        if (!event.isSwitch()) {
            if (minecraftService.getPlayer() != null) {
                stateService.setModerNickname(minecraftService.getPlayer().getName().getString());
            }

            stateService.setConnected(true);
            checkServerAddress(event);
            checkUpdates();

            boolean shouldBlock = !isOnHW || needUpdate || !enabled;
            if (!blocked && shouldBlock) {
                block();
            } else if (blocked && !shouldBlock) {
                unblock();
                eventBus.invokeEvent(event);
            }
        }

        stateService.setGameInitCompleted(false);

        if (minecraftService.getClient().interactionManager != null && minecraftService.getClient().interactionManager.getCurrentGameMode().equals(GameMode.ADVENTURE)) {
            stateService.setInHub(true);
            stateService.setModerLocation(StringUtils.EMPTY);
        } else {
            stateService.setInHub(false);
            chatService.chatMessage("/find " + stateService.getModerNickname());
        }

        stateService.setGameInitCompleted(true);
    }

    @Subscribe(priority = 100)
    public void onServerDisconnect(ServerDisconnectEvent event) {
        if (stateService.isConnected()) {
            stateService.reset();
        }
    }

    @Subscribe(priority = 100)
    public void onMessageSend(MessageSendEvent event) {
        if (!stateService.isGameInitCompleted()) {
            event.setCancelled(true);
            holyLogger.printError("Не спеши, инициализация игры ещё не завершилась!");
            return;
        }

        if (event.getContent().startsWith(".enable")) {
            event.setCancelled(true);
            enabled = true;
            unblock();
            holyLogger.printSuccess("Мод включен!");
        } else if (event.getContent().startsWith(".disable")) {
            event.setCancelled(true);
            enabled = false;
            block();
            holyLogger.printSuccess(RED + BOLD + "Мод выключен!");
        }
    }

    @Subscribe(priority = 100)
    public void onMessageReceive(MessageReceiveEvent event) {
        String receivedText = chatService.formatReceivedText(event.getMessage().getString());
        if (receivedText == null) {
            return;
        }

        if (receivedText.equals("▶ Ожидайте завершения проверки... Пожалуйста, не двигайтесь.") || receivedText.equals("▶ Введите цифры с картинки в чат! Для открытия чата, нажмите <T>")) {
            stateService.setInHub(true);
            stateService.setGameInitCompleted(true);
            stateService.setModerLocation(StringUtils.EMPTY);
        }

        if (stateService.getModerLocation().isEmpty()) {
            if (receivedText.startsWith("Игрок " + stateService.getModerNickname())) {
                event.setCancelled(true);
                stateService.setModerLocation(chatService.formatLocation(receivedText.split("сервере ")[1]));
            }
        }
    }

    private void checkServerAddress(ServerConnectEvent event) {
        isOnHW = event.getServerInfo().address.matches("(?i).*hol(l)?yworld.*");
    }

    private void checkUpdates() {
        String lastVersion = netService.getLastUpdates().getKey();
        String description = netService.getLastUpdates().getValue();

        if (!configManager.getConfig().currentVersion.equals(lastVersion)) {
            chatService.clientMessage(RED + BOLD + "Ваша версия HolyModeration устарела. Новейшая версия: " + DARK_GREEN + BOLD + lastVersion + RED + BOLD + ", ваша: " + DARK_GREEN + BOLD + configManager.getConfig().currentVersion);
            chatService.clientMessage(AQUA + BOLD + "Описание обновления: " + LIGHT_PURPLE + BOLD + description.replace("\\n", "\n"));
            chatService.clientMessage("ссылканаобновление");
            soundService.playSound("update.wav", 70);
            needUpdate = true;
        }
    }

    private void block() {
        blocked = true;
        eventBus.clear();
        eventBus.register(this);
    }

    private void unblock() {
        blocked = false;
        HolyModerationClient.registerEventListeners(eventBus);
    }
}