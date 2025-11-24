package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.HolyModerationClient;
import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.MessageSendEvent;
import me.yuugao.holymoderation.client.eventbus.event.ServerConnectEvent;
import me.yuugao.holymoderation.client.eventbus.event.ServerDisconnectEvent;

public class StateModule extends Module {
    private boolean blocked = false;
    private boolean enabled = true;
    private boolean isOnHW = false;
    private boolean needUpdate = false;

    @Subscribe(priority = 100)
    public void onServerConnect(ServerConnectEvent event) {
        if (!event.isSwitch()) {
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
    }

    @Subscribe(priority = 100)
    public void onServerDisconnect(ServerDisconnectEvent event) {
        if (stateService.isConnected()) {
            stateService.reset();
        }
    }

    @Subscribe(priority = 100)
    public void onMessageSend(MessageSendEvent event) {
        if (event.getContent().equals(".enable")) {
            event.setCancelled(true);
            enabled = true;
            unblock();
            holyLogger.printSuccess("Мод включен!");
        } else if (event.getContent().equals(".disable")) {
            event.setCancelled(true);
            enabled = false;
            block();
            holyLogger.printSuccess(RED + BOLD + "Мод выключен!");
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