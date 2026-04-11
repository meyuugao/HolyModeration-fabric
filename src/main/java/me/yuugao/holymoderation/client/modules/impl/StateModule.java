package me.yuugao.holymoderation.client.modules.impl;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.config.impl.ApiConfig;
import me.yuugao.holymoderation.client.config.impl.SettingsConfig;
import me.yuugao.holymoderation.client.config.manager.ConfigManager;
import me.yuugao.holymoderation.client.eventbus.EventBus;
import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.impl.chat.CommandSendEvent;
import me.yuugao.holymoderation.client.eventbus.event.impl.chat.MessageReceiveEvent;
import me.yuugao.holymoderation.client.eventbus.event.impl.connection.ServerConnectEvent;
import me.yuugao.holymoderation.client.eventbus.event.impl.connection.ServerDisconnectEvent;
import me.yuugao.holymoderation.client.modules.Module;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.*;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import org.apache.commons.lang3.StringUtils;

public class StateModule extends Module {
    public StateModule(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Subscribe(priority = 101)
    public void onServerConnect(ServerConnectEvent event) {
        MinecraftService minecraftService = serviceContext.getMinecraftService();
        StateService stateService = serviceContext.getStateService();
        EventBus eventBus = serviceContext.getEventBus();

        ClientPlayerEntity player = minecraftService.getPlayer();

        if (event.isSwitch()) return; //tip: выполняется только при заходе на сервер, не при свитче

        if (player != null) {
            stateService.setUserNickname(player.getName().getString());
        }

        stateService.setConnected(true);
        checkServerAddress(event);

        if (!stateService.isOnHW()) {
            stateService.block(); //tip: ретурн если дальше будет логика
        } else if (stateService.isBlocked() && stateService.isEnabled()) {
            stateService.unblock();
            eventBus.invokeEvent(event);
        }
    }

    @Subscribe(priority = 100)
    public void onServerConnectSecond(ServerConnectEvent event) {
        StateService stateService = serviceContext.getStateService();
        MinecraftService minecraftService = serviceContext.getMinecraftService();
        ChatService chatService = serviceContext.getChatService();
        ConfigManager configManager = serviceContext.getConfigManager();

        ClientPlayerInteractionManager interactionManager = minecraftService.getClient().interactionManager;
        ClientWorld clientWorld = minecraftService.getWorld();
        SettingsConfig settingsConfig = configManager.getSettingsConfig();

        if (stateService.isBlocked()) return;

        stateService.setGameInitCompleted(false);

        if (interactionManager != null && interactionManager.getCurrentGameMode().equals(GameMode.ADVENTURE)) {
            stateService.setInHub(true);
            stateService.setUserLocation(StringUtils.EMPTY);
        } else {
            stateService.setInHub(false);
            chatService.chatMessage("/find %s".formatted(stateService.getUserNickname()));
        }

        if (event.isSwitch() && !stateService.isInHub() && interactionManager != null) {
            if (clientWorld != null && clientWorld.getRegistryKey().getValue().toString().equals("minecraft:spawn_world")) { //tip: при первом заходе на анку не работает, потому что не был проинициализирован мир ни разу
                stateService.setVanishEnabled(true);
            }

            stateService.setGm3Enabled(interactionManager.getCurrentGameMode() == GameMode.SPECTATOR);

            if (settingsConfig.isAutoVanishEnabled() && !stateService.isVanishEnabled()
                    || !settingsConfig.isAutoVanishEnabled() && stateService.isVanishEnabled()) {
                chatService.chatMessage("/v");
                stateService.setVanishEnabled(stateService.isVanishEnabled());
            }

            if (settingsConfig.isAutoGm3Enabled() && !stateService.isGm3Enabled()
                    || !settingsConfig.isAutoGm3Enabled() && stateService.isGm3Enabled()) {
                chatService.chatMessage("/gm 3");
                stateService.setGm3Enabled(stateService.isGm3Enabled());
            }

            if (settingsConfig.isAutoFlyEnabled() && !stateService.isFlyEnabled()
                    || !settingsConfig.isAutoFlyEnabled() && stateService.isFlyEnabled()) {
                if (!stateService.isGm3Enabled()) {
                    chatService.chatMessage("/fly");
                    stateService.setFlyEnabled(stateService.isFlyEnabled());
                }
            }

            if (settingsConfig.isAutoGodEnabled() && !stateService.isGodEnabled()
                    || !settingsConfig.isAutoGodEnabled() && stateService.isGodEnabled()) {
                chatService.chatMessage("/god");
                stateService.setGodEnabled(stateService.isGodEnabled());
            }

            if (settingsConfig.isAutoHacAlertsEnabled() && !stateService.isHacAlertsEnabled()
                    || !settingsConfig.isAutoHacAlertsEnabled() && stateService.isHacAlertsEnabled()) {
                chatService.chatMessage("/hac alerts");
                stateService.setHacAlertsEnabled(stateService.isHacAlertsEnabled());
            }
        }

        stateService.setGameInitCompleted(true);
    }

    @Subscribe(priority = 100)
    public void onServerDisconnect(ServerDisconnectEvent event) {
        StateService stateService = serviceContext.getStateService();
        NotificationsService notificationsService = serviceContext.getNotificationsService();

        if (stateService.isConnected()) {
            stateService.reset();
            notificationsService.clearNotifications();
        }
    }

    @Subscribe(priority = 100)
    public void onCommandSend(CommandSendEvent event) {
        StateService stateService = serviceContext.getStateService();
        NotificationsService notificationsService = serviceContext.getNotificationsService();
        MinecraftService minecraftService = serviceContext.getMinecraftService();
        ConfigManager configManager = serviceContext.getConfigManager();
        SoundService soundService = serviceContext.getSoundService();

        ClientWorld clientWorld = minecraftService.getWorld();
        ClientPlayNetworkHandler clientPlayNetworkHandler = minecraftService.getClient().getNetworkHandler();
        ApiConfig apiConfig = configManager.getApiConfig();

        if (!stateService.isOnHW()) return;

        if (!stateService.isGameInitCompleted()) {
            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                    "Не спеши, инициализация игры ещё не завершилась!", 5f);
            return;
        }

        if (stateService.isCheckingTwinks()) {
            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                    "Дождитесь окончания проверки твинков.", 5f);
            event.setCancelled(true);
            return;
        }

        String eventCommand = event.getCommand();
        String[] messageSplit = eventCommand.split(" ");
        String command = eventCommand.startsWith("hm") ? messageSplit[1] : messageSplit[0];

        switch (command) {
            case ("v") -> {
                if (clientWorld != null && clientWorld.getRegistryKey().getValue().toString().equals("minecraft:spawn_world")) {
                    if (messageSplit.length > 1) {
                        if (messageSplit[1].equals("enable")) {
                            stateService.setVanishEnabled(true);
                            break;
                        } else if (messageSplit[1].equals("disable")) {
                            stateService.setVanishEnabled(false);
                            break;
                        }
                    }

                    stateService.setVanishEnabled(!stateService.isVanishEnabled());
                }
            }

            case "gamemode", "gm" -> {
                if (messageSplit[1].equals("3") || messageSplit[1].equals("spectator")) {
                    stateService.setGm3Enabled(true);
                } else if (messageSplit[1].equals("0") || messageSplit[1].equals("1") || messageSplit[1].equals("2")
                        || messageSplit[1].equals("survival") || messageSplit[1].equals("creative")
                        || messageSplit[1].equals("adventure")) {
                    stateService.setGm3Enabled(false);
                }
            }

            case "fly" -> {
                if (messageSplit.length > 1) {
                    if (messageSplit[1].equals("enable")) {
                        stateService.setFlyEnabled(true);
                        break;
                    } else if (messageSplit[1].equals("disable")) {
                        stateService.setFlyEnabled(false);
                        break;
                    }
                }

                stateService.setFlyEnabled(!stateService.isFlyEnabled());
            }

            case "god" -> {
                if (messageSplit.length > 1) {
                    if (messageSplit[1].equals("enable")) {
                        stateService.setGodEnabled(true);
                        break;
                    } else if (messageSplit[1].equals("disable")) {
                        stateService.setGodEnabled(false);
                        break;
                    }
                }

                stateService.setGodEnabled(!stateService.isGodEnabled());
            }

            case "hac" -> {
                if (messageSplit.length > 1 && messageSplit[1].equals("alerts")) {
                    stateService.setHacAlertsEnabled(!stateService.isHacAlertsEnabled());
                }
            }

            case "enableDebug" -> {
                stateService.enableDebug();
                notificationsService.addNotification(NotificationType.WARNING,
                        "%s%sПОЖАЛУЙСТА, ОБРАТИТЕ ВНИМАНИЕ!".formatted(RED, BOLD),
                        """
                                %sБыл включен %sдебаг.%s Это означает, что в логах может появиться важная информация, и в том числе:
                                %sИнформация о вашей системе.
                                %sИнформация о ваших действиях в текущей сессии.
                                %sВаш конфиг, в том числе apitoken.
                                %sЕсли кто-то сказал вам включить этот режим, будьте осторожны и %sНИ В КОЕМ СЛУЧАЕ НЕ ДАВАЙТЕ ДОСТУП К СВОЕМУ ПК ИЛИ ЛОГАМ ТЕКУЩЕЙ СЕССИИ!"""
                                .formatted(GOLD, RED, GOLD, RED, RED, RED, GOLD, RED), 30f);
            }

            case "disableDebug" -> {
                stateService.disableDebug();
                notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                        "Дебаг выключен!", 5f);
                break;
            }

            case "enable" -> {
                if (!stateService.isBlocked()) {
                    stateService.enable();
                    notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                            "Мод включен!", 5f);
                }
            }

            case "disable" -> {
                event.setCancelled(true);

                if (!stateService.isBlocked()) {
                    stateService.disable();
                    notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                            "Мод выключен!", 5f);
                }
            }

            case "setapitoken" -> {
                if (messageSplit.length == 2) {
                    notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                            "Вы не ввели токен.", 5f);
                    return;
                }
                String apiToken = messageSplit[2];
                if (apiToken.contains(" ")) {
                    notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                            "В API токене обнаружены пробелы, пожалуйста, указывайте его без пробелов.", 5f);
                    return;
                }
                apiConfig.setApiToken(apiToken);
                configManager.saveConfig(apiConfig);
                soundService.playSound("success.wav");

                if (clientPlayNetworkHandler != null) {
                    clientPlayNetworkHandler.getConnection().disconnect(Text.of(
                            "%s%sВы успешно установили API токен. Пожалуйста, перезайдите на сервер.".formatted(AQUA, BOLD)));
                }
            }
        }
    }

    @Subscribe(priority = -100)
    public void onCommandSendSecond(CommandSendEvent event) {
        if (event.getCommand().startsWith("hm")) event.setCancelled(true);
    }

    @Subscribe(priority = 100)
    public void onMessageReceive(MessageReceiveEvent event) {
        ChatService chatService = serviceContext.getChatService();
        StateService stateService = serviceContext.getStateService();

        String receivedText = chatService.formatReceivedText(event.getMessage().getString());
        if (receivedText == null) return;

        if (receivedText.equals("▶ Ожидайте завершения проверки... Пожалуйста, не двигайтесь.")
                || receivedText.equals("▶ Введите цифры с картинки в чат! Для открытия чата, нажмите <T>")) {
            stateService.setInHub(true);
            stateService.setGameInitCompleted(true);
            stateService.setUserLocation(StringUtils.EMPTY);
        }

        if (stateService.getUserLocation().isEmpty()) {
            if (receivedText.startsWith("Игрок %s".formatted(stateService.getUserNickname()))) {
                event.setCancelled(true);
                stateService.setUserLocation(chatService.formatLocation(receivedText.split("сервере ")[1]));
            }
        }
    }

    private void checkServerAddress(ServerConnectEvent event) {
        StateService stateService = serviceContext.getStateService();

        stateService.setOnHW(event.getServerInfo().address.matches("(?i).*hol(l)?yworld.*"));
    }
}