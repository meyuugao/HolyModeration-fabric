package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.CommandSendEvent;
import me.yuugao.holymoderation.client.eventbus.event.MessageReceiveEvent;
import me.yuugao.holymoderation.client.eventbus.event.ServerConnectEvent;
import me.yuugao.holymoderation.client.eventbus.event.ServerDisconnectEvent;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.NotificationType;

import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import net.minecraft.world.biome.Biome;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

public class StateModule extends Module {
    public StateModule(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Subscribe(priority = 101)
    public void onServerConnect(ServerConnectEvent event) {
        if (event.isSwitch()) return; //tip: выполняется только при заходе на сервер, не при свитче

        if (serviceContext.getMinecraftService().getPlayer() != null) {
            serviceContext.getStateService().setUserNickname(serviceContext.getMinecraftService().getPlayer().getName().getString());
        }

        serviceContext.getStateService().setConnected(true);
        checkServerAddress(event);

        if (!serviceContext.getStateService().isOnHW()) {
            serviceContext.getStateService().block(); //tip: ретурн если дальше будет логика
        } else if (serviceContext.getStateService().isBlocked() && serviceContext.getStateService().isEnabled()) {
            serviceContext.getStateService().unblock();
            serviceContext.getEventBus().invokeEvent(event);
        }
    }

    @Subscribe(priority = 100)
    public void onServerConnectSecond(ServerConnectEvent event) {
        if (serviceContext.getStateService().isBlocked()) return;

        serviceContext.getStateService().setGameInitCompleted(false);

        if (serviceContext.getMinecraftService().getClient().interactionManager != null && serviceContext.getMinecraftService().getClient().interactionManager.getCurrentGameMode().equals(GameMode.ADVENTURE)) {
            serviceContext.getStateService().setInHub(true);
            serviceContext.getStateService().setUserLocation(StringUtils.EMPTY);
        } else {
            serviceContext.getStateService().setInHub(false);
            serviceContext.getChatService().chatMessage("/find " + serviceContext.getStateService().getUserNickname());
        }

        if (event.isSwitch() && !serviceContext.getStateService().isInHub()) {
            if (serviceContext.getMinecraftService().getWorld() != null && serviceContext.getMinecraftService().getWorld().getRegistryKey().getValue().toString().equals("minecraft:spawn_world")) {
                serviceContext.getStateService().setVanishEnabled(true);
            }

            serviceContext.getStateService().setGm3Enabled(serviceContext.getMinecraftService().getClient().interactionManager.getCurrentGameMode() == GameMode.SPECTATOR);

            if (serviceContext.getConfigManager().getConfig().isAutoVanishEnabled() && !serviceContext.getStateService().isVanishEnabled()
                    || !serviceContext.getConfigManager().getConfig().isAutoVanishEnabled() && serviceContext.getStateService().isVanishEnabled()) {
                serviceContext.getChatService().chatMessage("/v");
                serviceContext.getStateService().setVanishEnabled(serviceContext.getStateService().isVanishEnabled());
            }

            if (serviceContext.getConfigManager().getConfig().isAutoGm3Enabled() && !serviceContext.getStateService().isGm3Enabled()
                    || !serviceContext.getConfigManager().getConfig().isAutoGm3Enabled() && serviceContext.getStateService().isGm3Enabled()) {
                serviceContext.getChatService().chatMessage("/gm 3");
                serviceContext.getStateService().setGm3Enabled(serviceContext.getStateService().isGm3Enabled());
            }

            if (serviceContext.getConfigManager().getConfig().isAutoFlyEnabled() && !serviceContext.getStateService().isFlyEnabled()
                    || !serviceContext.getConfigManager().getConfig().isAutoFlyEnabled() && serviceContext.getStateService().isFlyEnabled()) {
                if (!serviceContext.getStateService().isGm3Enabled()) {
                    serviceContext.getChatService().chatMessage("/fly");
                    serviceContext.getStateService().setFlyEnabled(serviceContext.getStateService().isFlyEnabled());
                }
            }

            if (serviceContext.getConfigManager().getConfig().isAutoGodEnabled() && !serviceContext.getStateService().isGodEnabled()
                    || !serviceContext.getConfigManager().getConfig().isAutoGodEnabled() && serviceContext.getStateService().isGodEnabled()) {
                serviceContext.getChatService().chatMessage("/god");
                serviceContext.getStateService().setGodEnabled(serviceContext.getStateService().isGodEnabled());
            }

            if (serviceContext.getConfigManager().getConfig().isAutoHacAlertsEnabled() && !serviceContext.getStateService().isHacAlertsEnabled()
                    || !serviceContext.getConfigManager().getConfig().isAutoHacAlertsEnabled() && serviceContext.getStateService().isHacAlertsEnabled()) {
                serviceContext.getChatService().chatMessage("/hac alerts");
                serviceContext.getStateService().setHacAlertsEnabled(serviceContext.getStateService().isHacAlertsEnabled());
            }
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
            event.setCancelled(true);
            return;
        }

        String eventCommand = event.getCommand();
        String[] messageSplit = eventCommand.split(" ");
        String command = eventCommand.startsWith("hm") ? messageSplit[1] : messageSplit[0];

        switch (command) {
            case ("v"): {
                if (serviceContext.getMinecraftService().getWorld() != null && serviceContext.getMinecraftService().getWorld().getRegistryKey().getValue().toString().equals("minecraft:spawn_world")) {
                    if (messageSplit.length > 1) {
                        if (messageSplit[1].equals("enable")) {
                            serviceContext.getStateService().setVanishEnabled(true);
                            break;
                        } else if (messageSplit[1].equals("disable")) {
                            serviceContext.getStateService().setVanishEnabled(false);
                            break;
                        }
                    }

                    serviceContext.getStateService().setVanishEnabled(!serviceContext.getStateService().isVanishEnabled());
                }
                break;
            }

            case ("gamemode"):
            case ("gm"): {
                if (messageSplit[1].equals("3") || messageSplit[1].equals("spectator")) {
                    serviceContext.getStateService().setGm3Enabled(true);
                } else if (messageSplit[1].equals("0") || messageSplit[1].equals("1") || messageSplit[1].equals("2")
                        || messageSplit[1].equals("survival") || messageSplit[1].equals("creative") || messageSplit[1].equals("adventure")) {
                    serviceContext.getStateService().setGm3Enabled(false);
                }
                break;
            }

            case ("fly"): {
                if (messageSplit.length > 1) {
                    if (messageSplit[1].equals("enable")) {
                        serviceContext.getStateService().setFlyEnabled(true);
                        break;
                    } else if (messageSplit[1].equals("disable")) {
                        serviceContext.getStateService().setFlyEnabled(false);
                        break;
                    }
                }

                serviceContext.getStateService().setFlyEnabled(!serviceContext.getStateService().isFlyEnabled());
                break;
            }

            case ("god"): {
                if (messageSplit.length > 1) {
                    if (messageSplit[1].equals("enable")) {
                        serviceContext.getStateService().setGodEnabled(true);
                        break;
                    } else if (messageSplit[1].equals("disable")) {
                        serviceContext.getStateService().setGodEnabled(false);
                        break;
                    }
                }

                serviceContext.getStateService().setGodEnabled(!serviceContext.getStateService().isGodEnabled());
                break;
            }

            case ("hac"): {
                if (messageSplit.length > 1 && messageSplit[1].equals("alerts")) {
                    serviceContext.getStateService().setHacAlertsEnabled(!serviceContext.getStateService().isHacAlertsEnabled());
                }

                break;
            }

            case ("disable"): {
                event.setCancelled(true);

                if (!serviceContext.getStateService().isBlocked()) {
                    serviceContext.getStateService().disable();
                    serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Мод выключен!", 5f);
                }
                break;
            }

            case ("enable"): {
                if (!serviceContext.getStateService().isBlocked()) {
                    serviceContext.getStateService().enable();
                    serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Мод включен!", 5f);
                }
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
        if (receivedText == null) return;

        if (receivedText.equals("▶ Ожидайте завершения проверки... Пожалуйста, не двигайтесь.") || receivedText.equals("▶ Введите цифры с картинки в чат! Для открытия чата, нажмите <T>")) {
            serviceContext.getStateService().setInHub(true);
            serviceContext.getStateService().setGameInitCompleted(true);
            serviceContext.getStateService().setUserLocation(StringUtils.EMPTY);
        }

        if (serviceContext.getStateService().getUserLocation().isEmpty()) {
            if (receivedText.startsWith("Игрок " + serviceContext.getStateService().getUserNickname())) {
                event.setCancelled(true);
                serviceContext.getStateService().setUserLocation(serviceContext.getChatService().formatLocation(receivedText.split("сервере ")[1]));
            }
        }
    }

    private void checkServerAddress(ServerConnectEvent event) {
        serviceContext.getStateService().setOnHW(event.getServerInfo().address.matches("(?i).*hol(l)?yworld.*"));
    }
}