package me.yuugao.holymoderation.client.modules.impl;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.singleton.SpyDrawableElement;
import me.yuugao.holymoderation.client.modules.DrawableModule;
import me.yuugao.holymoderation.client.util.service.*;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.config.impl.SettingsConfig;
import me.yuugao.holymoderation.client.util.service.eventbus.Subscribe;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.chat.CommandSendEvent;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.chat.MessageReceiveEvent;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.connection.ServerConnectEvent;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.connection.ServerDisconnectEvent;
import me.yuugao.holymoderation.client.util.service.state.PlayerStateService;
import me.yuugao.holymoderation.client.util.service.state.UserStateService;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

@Singleton
public class SpyModule extends DrawableModule<SpyDrawableElement> {
    private final PlayerStateService playerStateService;
    private final UserStateService userStateService;
    private final SpyService spyService;
    private final NotificationsService notificationsService;
    private final CheckoutsService checkoutsService;
    private final ConfigManagerService configManagerService;
    private final SchedulerService schedulerService;
    private final ChatService chatService;

    private boolean processingPlaytimeInfo = false;
    private boolean shouldUpdate = false;
    private boolean instantUpdate = false;
    private String lastKnownLocation = StringUtils.EMPTY;

    @Inject
    public SpyModule(SpyDrawableElement spyDrawableElement, PlayerStateService playerStateService,
                     UserStateService userStateService, SpyService spyService,
                     NotificationsService notificationsService, CheckoutsService checkoutsService,
                     ConfigManagerService configManagerService, SchedulerService schedulerService, ChatService chatService) {
        super(spyDrawableElement);
        this.playerStateService = playerStateService;
        this.userStateService = userStateService;
        this.spyService = spyService;
        this.notificationsService = notificationsService;
        this.checkoutsService = checkoutsService;
        this.configManagerService = configManagerService;
        this.schedulerService = schedulerService;
        this.chatService = chatService;
    }

    @Subscribe
    public void onCommandSend(CommandSendEvent event) {
        String eventCommand = event.getCommand();
        String[] commandSplit = eventCommand.split(" ", 3);
        if (!eventCommand.startsWith("hm") || commandSplit.length < 2) return;

        if (commandSplit[1].equals("spy")) {
            if (commandSplit.length == 2) {
                if (!playerStateService.getSpyPlayer().isEmpty()) {
                    spyService.endSpy();
                } else {
                    notificationsService.addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(GOLD, BOLD),
                            "Вы никого не отслеживаете.", 5f);
                }
                return;
            }

            if (userStateService.isInHub()) {
                notificationsService.addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(GOLD, BOLD),
                        "В хабе этого делать нельзя.", 5f);
                return;
            }

            if (!playerStateService.getCheckoutPlayer().isEmpty() && playerStateService.getCheckoutPlayer().equals(commandSplit[2])) {
                notificationsService.addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(GOLD, BOLD),
                        "Вы не можете начать следить за игроком на вашей проверке.", 5f);
                return;
            }

            if (!playerStateService.getSpyPlayer().isEmpty()) {
                notificationsService.addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(GOLD, BOLD),
                        "Вы уже следите за кем-то --> %s%s/hm spy%s%s%s.".formatted(GOLD, BOLD, WHITE, RED, BOLD), 5f);
                return;
            }

            spyService.startSpy(commandSplit[2]);
        } else if (commandSplit[1].equals("spyfrz")) {
            if (userStateService.isInHub()) {
                notificationsService.addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(GOLD, BOLD),
                        "В хабе этого делать нельзя.", 5f);
                return;
            }

            if (playerStateService.getSpyPlayer().isEmpty()) {
                notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                        "Вы ни за кем не следите.", 5f);
                return;
            }

            if (checkoutsService.startCheckOut(playerStateService.getSpyPlayer())) {
                spyService.endSpy();
            }
        }
    }

    @Subscribe(priority = 98)
    public void onMessageReceive(MessageReceiveEvent event) {
        SettingsConfig settingsConfig = configManagerService.getSettingsConfig();

        String receivedText = chatService.formatReceivedText(event.getMessage().getString());
        if (receivedText == null) return;

        boolean isChecking = spyService.isCheckingSpy();

        if (isChecking) {
            if (receivedText.startsWith("----------")) {
                if (processingPlaytimeInfo) {
                    spyService.onPlaytimeComplete();
                    shouldUpdate = true;
                    processingPlaytimeInfo = false;
                } else {
                    processingPlaytimeInfo = true;
                }
            }

            if (receivedText.startsWith("Игрок") && !receivedText.startsWith("Игрок %s".formatted(userStateService.getUserNickname()))) {
                event.setCancelled(true);
                String status;
                if (receivedText.equals("Игрок оффлайн")) {
                    status = "offline";
                } else if (receivedText.split("сервере ")[1].startsWith("lobby")) {
                    status = "lobby";
                } else {
                    status = chatService.formatLocation(receivedText.split("сервере ")[1]);
                }
                spyService.onFindResponse(status);
                shouldUpdate = true;
            }

            if (receivedText.startsWith("Текущая")) {
                String loc = receivedText.split(": ")[1];
                loc = loc.substring(1, loc.length() - 1);
                if (loc.equals("Оффлайн")) {
                    processingPlaytimeInfo = true;
                    spyService.onFindResponse("offline");
                    instantUpdate = true;
                } else {
                    if (lastKnownLocation.isEmpty()) lastKnownLocation = loc;
                    else if (!loc.equals(lastKnownLocation)) {
                        playerStateService.setSpyPlayerStatus(StringUtils.EMPTY);
                        lastKnownLocation = loc;
                    }
                }
            }

            if (receivedText.startsWith("Последняя") && !playerStateService.getSpyPlayerStatus().isEmpty()) {
                playerStateService.setSpyPlayerActivity(receivedText.split(": ")[1]);
            }

            if (receivedText.startsWith("Активность") || receivedText.startsWith("Общее время") ||
                    receivedText.startsWith("Текущая") || receivedText.startsWith("Время") ||
                    receivedText.startsWith("Последняя") || receivedText.startsWith("Последний") ||
                    receivedText.startsWith("----------") || receivedText.isEmpty()) {
                event.setCancelled(true);
            }

            if (shouldUpdate) {
                if (userStateService.getUserLocation().equals(playerStateService.getSpyPlayerStatus())
                        && playerStateService.getSpyPlayerActivity().isEmpty()) {
                    instantUpdate = true;
                    if (settingsConfig.isAutoSpyTpEnabled()) {
                        chatService.chatMessage("/tpo " + playerStateService.getSpyPlayer());
                    }
                }

                schedulerService.schedule("SpyModule/onMessageReceive", spyService::update,
                        instantUpdate ? 500 : settingsConfig.getSpyDelay(),
                        instantUpdate ? TimeUnit.MILLISECONDS : TimeUnit.SECONDS);
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
        if (spyService.isEnabled()) {
            spyService.endSpy();
        }
    }

    private void tryInit() {
        schedulerService.schedule("SpyModule/tryInit", () -> {
            if (userStateService.isGameInitCompleted()) {
                if (spyService.isEnabled()) {
                    if (userStateService.isInHub()) {
                        lastKnownLocation = StringUtils.EMPTY;
                        spyService.onPause();
                        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех"
                                .formatted(GREEN, BOLD), "Слежка приостановлена.", 5f);
                    } else {
                        if (!userStateService.getUserLocation().isEmpty()) {
                            spyService.update();
                            notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех"
                                    .formatted(GREEN, BOLD), "Слежка возобновлена.", 5f);
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

    @Override
    public int getRenderPriority() {
        return 1001;
    }
}