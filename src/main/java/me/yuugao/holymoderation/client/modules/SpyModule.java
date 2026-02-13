package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.config.SettingsConfig;
import me.yuugao.holymoderation.client.config.manager.ConfigManager;
import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.CommandSendEvent;
import me.yuugao.holymoderation.client.eventbus.event.MessageReceiveEvent;
import me.yuugao.holymoderation.client.eventbus.event.ServerConnectEvent;
import me.yuugao.holymoderation.client.eventbus.event.ServerDisconnectEvent;
import me.yuugao.holymoderation.client.modules.drawable.DrawableModule;
import me.yuugao.holymoderation.client.modules.drawable.element.SpyDrawableElement;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.*;

import net.minecraft.text.Text;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

public class SpyModule extends DrawableModule<SpyDrawableElement> {
    private boolean processingPlaytimeInfo = false;
    private boolean shouldUpdate = false;
    private boolean instantUpdate = false;
    private String lastKnownLocation = StringUtils.EMPTY;

    public SpyModule(ServiceContext serviceContext, SpyDrawableElement spyDrawableElement) {
        super(serviceContext, spyDrawableElement);
    }

    @Subscribe
    public void onCommandSend(CommandSendEvent event) {
        StateService stateService = serviceContext.getStateService();
        SpyService spyService = serviceContext.getSpyService();
        NotificationsService notificationsService = serviceContext.getNotificationsService();
        CheckoutsService checkoutsService = serviceContext.getCheckoutsService();

        String eventCommand = event.getCommand();
        String[] commandSplit = eventCommand.split(" ", 3);
        if (!eventCommand.startsWith("hm") || commandSplit.length < 2) return;

        if (commandSplit[1].equals("spy")) {
            if (commandSplit.length == 2) {
                if (!stateService.getSpyPlayer().isEmpty()) {
                    spyService.endSpy();
                } else {
                    notificationsService.addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(GOLD, BOLD),
                            "Вы никого не отслеживаете.", 5f);
                }
                return;
            }

            if (stateService.isInHub()) {
                notificationsService.addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(GOLD, BOLD),
                        "В хабе этого делать нельзя.", 5f);
                return;
            }

            if (!stateService.getCheckoutPlayer().isEmpty() && stateService.getCheckoutPlayer().equals(commandSplit[2])) {
                notificationsService.addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(GOLD, BOLD),
                        "Вы не можете начать следить за игроком на вашей проверке.", 5f);
                return;
            }

            if (!stateService.getSpyPlayer().isEmpty()) {
                notificationsService.addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(GOLD, BOLD),
                        "Вы уже следите за кем-то --> %s%s/hm spy%s%s%s.".formatted(GOLD, BOLD, WHITE, RED, BOLD), 5f);
                return;
            }

            spyService.startSpy(commandSplit[2]);
        } else if (commandSplit[1].equals("spyfrz")) {
            if (stateService.isInHub()) {
                notificationsService.addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(GOLD, BOLD),
                        "В хабе этого делать нельзя.", 5f);
                return;
            }

            if (stateService.getSpyPlayer().isEmpty()) {
                notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                        "Вы ни за кем не следите.", 5f);
                return;
            }

            if (checkoutsService.startCheckOut(stateService.getSpyPlayer())) {
                spyService.endSpy();
            }
        }
    }

    @Subscribe(priority = 98)
    public void onMessageReceive(MessageReceiveEvent event) {
        ChatService chatService = serviceContext.getChatService();
        StateService stateService = serviceContext.getStateService();
        SchedulerService schedulerService = serviceContext.getSchedulerService();
        SpyService spyService = serviceContext.getSpyService();
        ConfigManager configManager = serviceContext.getConfigManager();

        SettingsConfig settingsConfig = configManager.getSettingsConfig();

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

            if (receivedText.startsWith("Игрок") && !receivedText.startsWith("Игрок %s".formatted(stateService.getUserNickname()))) {
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
                        stateService.setSpyPlayerStatus(StringUtils.EMPTY);
                        lastKnownLocation = loc;
                    }
                }
            }

            if (receivedText.startsWith("Последняя") && !stateService.getSpyPlayerStatus().isEmpty()) {
                stateService.setSpyPlayerActivity(receivedText.split(": ")[1]);
            }

            if (receivedText.startsWith("Активность") || receivedText.startsWith("Общее время") ||
                    receivedText.startsWith("Текущая") || receivedText.startsWith("Время") ||
                    receivedText.startsWith("Последняя") || receivedText.startsWith("Последний") ||
                    receivedText.startsWith("----------") || receivedText.isEmpty()) {
                event.setCancelled(true);
            }

            if (shouldUpdate) {
                if (stateService.getUserLocation().equals(stateService.getSpyPlayerStatus())
                        && stateService.getSpyPlayerActivity().isEmpty()) {
                    instantUpdate = true;
                    if (!settingsConfig.isAutoSpyTpEnabled()) {
                        chatService.chatMessage("/tpo " + stateService.getSpyPlayer());
                    }
                }

                schedulerService.getScheduler().schedule(spyService::update,
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
        SpyService spyService = serviceContext.getSpyService();
        if (spyService.isEnabled()) {
            spyService.endSpy();
        }
    }

    private void tryInit() {
        SchedulerService schedulerService = serviceContext.getSchedulerService();
        StateService stateService = serviceContext.getStateService();
        SpyService spyService = serviceContext.getSpyService();
        NotificationsService notificationsService = serviceContext.getNotificationsService();

        schedulerService.getScheduler().schedule(() -> {
            if (stateService.isGameInitCompleted()) {
                if (spyService.isEnabled()) {
                    if (stateService.isInHub()) {
                        lastKnownLocation = StringUtils.EMPTY;
                        spyService.onPause();
                        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех"
                                .formatted(GREEN, BOLD), "Слежка приостановлена.", 5f);
                    } else {
                        if (!stateService.getUserLocation().isEmpty()) {
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