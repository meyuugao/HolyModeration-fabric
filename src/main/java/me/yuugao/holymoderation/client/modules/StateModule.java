package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.*;
import me.yuugao.holymoderation.client.util.serviceLocator.service.NotificationType;

import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import org.apache.commons.lang3.StringUtils;

import java.util.AbstractMap;

public class StateModule extends Module {
    @Subscribe(priority = 100)
    public void onServerConnect(ServerConnectEvent event) {
        if (!event.isSwitch()) {
            if (serviceContext.getMinecraftService().getPlayer() != null) {
                serviceContext.getStateService().setModerNickname(serviceContext.getMinecraftService().getPlayer().getName().getString());
            }

            serviceContext.getStateService().setConnected(true);
            checkServerAddress(event);

            if (!serviceContext.getStateService().isOnHW()) {
                serviceContext.getStateService().block();
                return;
            } else if (serviceContext.getStateService().isBlocked() && serviceContext.getStateService().isEnabled()) {
                serviceContext.getStateService().unblock();
                serviceContext.getEventBus().invokeEvent(event);
            }
        }

        serviceContext.getStateService().setGameInitCompleted(false);

        if (serviceContext.getMinecraftService().getClient().interactionManager != null && serviceContext.getMinecraftService().getClient().interactionManager.getCurrentGameMode().equals(GameMode.ADVENTURE)) {
            serviceContext.getStateService().setInHub(true);
            serviceContext.getStateService().setModerLocation(StringUtils.EMPTY);
        } else {
            serviceContext.getStateService().setInHub(false);
            serviceContext.getChatService().chatMessage("/find " + serviceContext.getStateService().getModerNickname());
        }

        serviceContext.getStateService().setGameInitCompleted(true);
    }

    @Subscribe(priority = 100)
    public void onServerDisconnect(ServerDisconnectEvent event) {
        if (serviceContext.getStateService().isConnected()) {
            serviceContext.getStateService().reset();
            serviceContext.getNotificationService().clearNotifications();
        }
    }

    @Subscribe(priority = 100)
    public void onCommandSend(CommandSendEvent event) {
        if (!serviceContext.getStateService().isOnHW()) return;

        if (!serviceContext.getStateService().isGameInitCompleted()) {
            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Не спеши, инициализация игры ещё не завершилась!", 5f);
            return;
        }

        if (serviceContext.getStateService().isCheckingTwinks()) {
            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Дождитесь окончания проверки твинков.", 5f);
            return;
        }

        String eventCommand = event.getCommand();
        String[] messageSplit = eventCommand.split(" ", 3);
        if (!eventCommand.startsWith("hm") || messageSplit.length < 2) return;

        switch (messageSplit[1]) {
            case ("disable"): {
                serviceContext.getStateService().setEnabled(false);
                serviceContext.getStateService().block();
                serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Мод выключен!", 5f);
                break;
            }

            case ("enable"): {
                serviceContext.getStateService().setEnabled(true);
                serviceContext.getStateService().unblock();
                serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Мод включен!", 5f);
                break;
            }

            case ("setapitoken"): {
                if (messageSplit.length == 2) {
                    serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы не ввели токен.", 5f);
                    return;
                }
                String apiToken = messageSplit[2];
                if (apiToken.contains(" ")) {
                    serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "В API токене обнаружены пробелы, пожалуйста, указывайте его без пробелов.", 5f);
                    return;
                }
                serviceContext.getConfigManager().getConfig().setApiToken(apiToken);
                serviceContext.getConfigManager().saveCfg(serviceContext.getConfigManager().getConfig());
                serviceContext.getSoundService().playSound("success.wav");

                if (serviceContext.getMinecraftService().getClient().getNetworkHandler() != null) {
                    serviceContext.getMinecraftService().getClient().getNetworkHandler().getConnection().disconnect(Text.of(AQUA + BOLD + "Вы успешно установили API токен. Пожалуйста, перезайдите на сервер."));
                }
                break;
            }
        }
    }

    @Subscribe(priority = -100)
    public void onCommandSendSecond(CommandSendEvent event) {
        if (event.getCommand().startsWith("hm")) event.setCancelled(true);
    }

    @Subscribe(priority = 100)
    public void onMessageReceive(MessageReceiveEvent event) {
        String receivedText = serviceContext.getChatService().formatReceivedText(event.getMessage().getString());
        if (receivedText == null) {
            return;
        }

        if (receivedText.equals("▶ Ожидайте завершения проверки... Пожалуйста, не двигайтесь.") || receivedText.equals("▶ Введите цифры с картинки в чат! Для открытия чата, нажмите <T>")) {
            serviceContext.getStateService().setInHub(true);
            serviceContext.getStateService().setGameInitCompleted(true);
            serviceContext.getStateService().setModerLocation(StringUtils.EMPTY);
        }

        if (serviceContext.getStateService().getModerLocation().isEmpty()) {
            if (receivedText.startsWith("Игрок " + serviceContext.getStateService().getModerNickname())) {
                event.setCancelled(true);
                serviceContext.getStateService().setModerLocation(serviceContext.getChatService().formatLocation(receivedText.split("сервере ")[1]));
            }
        }
    }

    @Subscribe(priority = 100)
    public void onHudRender(HudRenderEvent event) {
        serviceContext.getNotificationService().renderNotifications(event.getDrawContext());
    }

    private void checkServerAddress(ServerConnectEvent event) {
        serviceContext.getStateService().setOnHW(event.getServerInfo().address.matches("(?i).*hol(l)?yworld.*"));
    }
}