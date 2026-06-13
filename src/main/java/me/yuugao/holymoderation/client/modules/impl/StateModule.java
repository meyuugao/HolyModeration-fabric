package me.yuugao.holymoderation.client.modules.impl;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.service.*;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.config.impl.ApiConfig;
import me.yuugao.holymoderation.client.util.service.config.impl.SettingsConfig;
import me.yuugao.holymoderation.client.util.service.eventbus.EventBusService;
import me.yuugao.holymoderation.client.util.service.eventbus.Subscribe;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.chat.CommandSendEvent;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.chat.MessageReceiveEvent;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.connection.ServerConnectEvent;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.connection.ServerDisconnectEvent;
import me.yuugao.holymoderation.client.util.service.state.ModStateService;
import me.yuugao.holymoderation.client.util.service.state.PlayerStateService;
import me.yuugao.holymoderation.client.util.service.state.UserStateService;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class StateModule {
    private final MinecraftService minecraftService;
    private final ModStateService modStateService;
    private final PlayerStateService playerStateService;
    private final UserStateService userStateService;
    private final EventBusService eventBusService;
    private final ConfigManagerService configManagerService;
    private final ChatService chatService;
    private final NotificationsService notificationsService;
    private final SoundService soundService;
    private final SchedulerService schedulerService;
    private final UserValidationService userValidationService;

    @Subscribe(priority = 101)
    public void onServerConnect(ServerConnectEvent event) {
        ClientPlayerEntity player = minecraftService.getPlayer();

        if (event.isSwitch()) return; //tip: выполняется только при заходе на сервер, не при свитче

        if (player != null) {
            userStateService.setUserNickname(player.getName().getString());
        }

        userValidationService.onJoinServer();

        userStateService.setConnected(true);
        checkServerAddress(event);

        if (!userStateService.isOnHW()) {
            modStateService.block();
            return;
        }

        if (modStateService.isBlocked() && modStateService.isEnabled() && !modStateService.isForceBlocked()) {
            modStateService.unblock();
            eventBusService.getEventBus().invokeEvent(event);
        }
    }

    @Subscribe(priority = 100)
    public void onServerConnectSecond(ServerConnectEvent event) {
        ClientPlayerInteractionManager interactionManager = minecraftService.getClient().interactionManager;
        ClientWorld clientWorld = minecraftService.getWorld();
        SettingsConfig settingsConfig = configManagerService.getSettingsConfig();

        if (modStateService.isBlocked()) return;

        userStateService.setGameInitCompleted(false);
        userStateService.setUserLocation(StringUtils.EMPTY);

        if (interactionManager != null && interactionManager.getCurrentGameMode().equals(GameMode.ADVENTURE)) {
            userStateService.setInHub(true);
        } else {
            userStateService.setInHub(false);
            tryFindUser();
        }

        if (event.isSwitch() && !userStateService.isInHub() && interactionManager != null) {
            if (clientWorld != null && clientWorld.getRegistryKey().getValue().toString().equals("minecraft:spawn_world")) { //tip: при первом заходе на анку не работает, потому что не был проинициализирован мир ни разу
                userStateService.setVanishEnabled(true);
            }

            userStateService.setGm3Enabled(interactionManager.getCurrentGameMode() == GameMode.SPECTATOR);

            if (settingsConfig.isAutoVanishEnabled() && !userStateService.isVanishEnabled()
                    || !settingsConfig.isAutoVanishEnabled() && userStateService.isVanishEnabled()) {
                chatService.chatMessage("/v");
                userStateService.setVanishEnabled(userStateService.isVanishEnabled());
            }

            if (settingsConfig.isAutoGm3Enabled() && !userStateService.isGm3Enabled()
                    || !settingsConfig.isAutoGm3Enabled() && userStateService.isGm3Enabled()) {
                chatService.chatMessage("/gm 3");
                userStateService.setGm3Enabled(userStateService.isGm3Enabled());
            }

            if (settingsConfig.isAutoFlyEnabled() && !userStateService.isFlyEnabled()
                    || !settingsConfig.isAutoFlyEnabled() && userStateService.isFlyEnabled()) {
                if (!userStateService.isGm3Enabled()) {
                    chatService.chatMessage("/fly");
                    userStateService.setFlyEnabled(userStateService.isFlyEnabled());
                }
            }

            if (settingsConfig.isAutoGodEnabled() && !userStateService.isGodEnabled()
                    || !settingsConfig.isAutoGodEnabled() && userStateService.isGodEnabled()) {
                chatService.chatMessage("/god");
                userStateService.setGodEnabled(userStateService.isGodEnabled());
            }

            if (settingsConfig.isAutoHacAlertsEnabled() && !userStateService.isHacAlertsEnabled()
                    || !settingsConfig.isAutoHacAlertsEnabled() && userStateService.isHacAlertsEnabled()) {
                chatService.chatMessage("/hac alerts");
                userStateService.setHacAlertsEnabled(userStateService.isHacAlertsEnabled());
            }
        }

        userStateService.setGameInitCompleted(true);
    }

    @Subscribe(priority = 100)
    public void onServerDisconnect(ServerDisconnectEvent event) {
        if (userStateService.isConnected()) {
            playerStateService.reset();
            userStateService.reset();
            notificationsService.clearNotifications();
        }
    }

    @Subscribe(priority = 100)
    public void onCommandSend(CommandSendEvent event) {
        ClientWorld clientWorld = minecraftService.getWorld();
        ClientPlayNetworkHandler clientPlayNetworkHandler = minecraftService.getClient().getNetworkHandler();
        ApiConfig apiConfig = configManagerService.getApiConfig();

        if (!userStateService.isOnHW()) return;

        if (!userStateService.isGameInitCompleted()) {
            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                    "Не спеши, инициализация игры ещё не завершилась!", 5f);
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
                            userStateService.setVanishEnabled(true);
                            break;
                        } else if (messageSplit[1].equals("disable")) {
                            userStateService.setVanishEnabled(false);
                            break;
                        }
                    }

                    userStateService.setVanishEnabled(!userStateService.isVanishEnabled());
                }
            }

            case "gamemode", "gm" -> {
                if (messageSplit[1].equals("3") || messageSplit[1].equals("spectator")) {
                    userStateService.setGm3Enabled(true);
                } else if (messageSplit[1].equals("0") || messageSplit[1].equals("1") || messageSplit[1].equals("2")
                        || messageSplit[1].equals("survival") || messageSplit[1].equals("creative")
                        || messageSplit[1].equals("adventure")) {
                    userStateService.setGm3Enabled(false);
                }
            }

            case "fly" -> {
                if (messageSplit.length > 1) {
                    if (messageSplit[1].equals("enable")) {
                        userStateService.setFlyEnabled(true);
                        break;
                    } else if (messageSplit[1].equals("disable")) {
                        userStateService.setFlyEnabled(false);
                        break;
                    }
                }

                userStateService.setFlyEnabled(!userStateService.isFlyEnabled());
            }

            case "god" -> {
                if (messageSplit.length > 1) {
                    if (messageSplit[1].equals("enable")) {
                        userStateService.setGodEnabled(true);
                        break;
                    } else if (messageSplit[1].equals("disable")) {
                        userStateService.setGodEnabled(false);
                        break;
                    }
                }

                userStateService.setGodEnabled(!userStateService.isGodEnabled());
            }

            case "hac" -> {
                if (messageSplit.length > 1 && messageSplit[1].equals("alerts")) {
                    userStateService.setHacAlertsEnabled(!userStateService.isHacAlertsEnabled());
                }
            }

            case "enableDebug" -> {
                modStateService.enableDebug();
                notificationsService.addNotification(NotificationType.WARNING,
                        "%s%sПОЖАЛУЙСТА, ОБРАТИТЕ ВНИМАНИЕ!".formatted(RED, BOLD),
                        """
                                %sБыл включен %sдебаг.%s Это означает, что в логах может появиться важная информация, и в том числе:
                                %sИнформация о вашей системе.
                                %sИнформация о ваших действиях в текущей сессии.
                                %sВаш конфиг, в том числе API-ключ.
                                %sЕсли кто-то сказал вам включить этот режим, будьте осторожны и %sНИ В КОЕМ СЛУЧАЕ НЕ ДАВАЙТЕ ДОСТУП К СВОЕМУ ПК ИЛИ ЛОГАМ ТЕКУЩЕЙ СЕССИИ!"""
                                .formatted(GOLD, RED, GOLD, RED, RED, RED, GOLD, RED), 30f);
            }

            case "disableDebug" -> {
                modStateService.disableDebug();
                notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                        "Дебаг выключен!", 5f);
            }

            case "enable" -> {
                if (modStateService.enable()) {
                    notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                            "Мод включен!", 5f);
                } else {
                    notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                            "Мод уже включён!", 5f);
                }
            }

            case "disable" -> {
                event.setCancelled(true);
                if (modStateService.disable()) {
                    notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                            "Мод выключен!", 5f);
                } else {
                    notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                            "Мод уже выключен!", 5f);
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
                            "В API-ключе обнаружены пробелы, пожалуйста, указывайте его без пробелов.", 5f);
                    return;
                }
                apiConfig.setApiToken(apiToken);
                configManagerService.saveConfig(apiConfig);
                soundService.playSound("success.wav");

                if (clientPlayNetworkHandler != null) {
                    clientPlayNetworkHandler.getConnection().disconnect(Text.of(
                            "%s%sВы успешно установили API-ключ. Пожалуйста, перезайдите на сервер.".formatted(AQUA, BOLD)));
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
        String receivedText = chatService.formatReceivedText(event.getMessage().getString());
        if (receivedText == null) return;

        if (receivedText.equals("▶ Ожидайте завершения проверки... Пожалуйста, не двигайтесь.")
                || receivedText.equals("▶ Введите цифры с картинки в чат! Для открытия чата, нажмите <T>")) {
            userStateService.setInHub(true);
            userStateService.setGameInitCompleted(true);
            userStateService.setUserLocation(StringUtils.EMPTY);
        }

        if (userStateService.getUserLocation().isEmpty()) {
            if (receivedText.startsWith("Игрок %s".formatted(userStateService.getUserNickname()))) {
                event.setCancelled(true);
                userStateService.setUserLocation(chatService.formatLocation(receivedText.split("сервере ")[1]));
            }
        }
    }

    private void tryFindUser() {
        schedulerService.schedule("", () -> {
            if (userStateService.getUserLocation().isEmpty() && !userStateService.isInHub()) {
                chatService.chatMessage("/find %s".formatted(userStateService.getUserNickname()));
                tryFindUser();
            }
        }, 1000, TimeUnit.MILLISECONDS);
    }

    private void checkServerAddress(ServerConnectEvent event) {
        userStateService.setOnHW(event.getServerInfo().address.matches("(?i).*hol(l)?yworld.*"));
    }
}