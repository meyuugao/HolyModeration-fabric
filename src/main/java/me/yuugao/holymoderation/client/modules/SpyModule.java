package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.config.ConfigManager;
import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.CommandSendEvent;
import me.yuugao.holymoderation.client.eventbus.event.MessageReceiveEvent;
import me.yuugao.holymoderation.client.eventbus.event.ServerConnectEvent;
import me.yuugao.holymoderation.client.eventbus.event.ServerDisconnectEvent;
import me.yuugao.holymoderation.client.modules.drawable.DrawableModule;
import me.yuugao.holymoderation.client.modules.drawable.element.SpyDrawableElement;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.*;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

public class SpyModule extends DrawableModule<SpyDrawableElement> {
    private boolean enabled = false;
    private boolean checkingSpy = false;
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
        NotificationsService notificationsService = serviceContext.getNotificationsService();
        CheckoutsService checkoutsService = serviceContext.getCheckoutsService();

        String eventCommand = event.getCommand();
        String[] commandSplit = eventCommand.split(" ", 3);
        if (!eventCommand.startsWith("hm") || commandSplit.length < 2) return;

        if (commandSplit[1].equals("spy")) {
            if (commandSplit.length == 2) {
                if (!stateService.getSpyPlayer().isEmpty()) {
                    endSpy();
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

            startSpy(commandSplit[2]);
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
                endSpy();
            }
        }
    }

    @Subscribe(priority = 98)
    public void onMessageReceive(MessageReceiveEvent event) {
        ChatService chatService = serviceContext.getChatService();
        StateService stateService = serviceContext.getStateService();
        SchedulerService schedulerService = serviceContext.getSchedulerService();
        ConfigManager configManager = serviceContext.getConfigManager();

        String receivedText = chatService.formatReceivedText(event.getMessage().getString());
        if (receivedText == null) return;

        if (checkingSpy) {
            if (receivedText.startsWith("----------")) {
                if (processingPlaytimeInfo) {
                    checkingSpy = false;
                    shouldUpdate = true;
                    processingPlaytimeInfo = false;
                } else processingPlaytimeInfo = true;
            }

            if (receivedText.startsWith("Игрок") && !receivedText.startsWith("Игрок %s".formatted(stateService.getUserNickname()))) {
                event.setCancelled(true);
                if (receivedText.equals("Игрок оффлайн"))
                    stateService.setSpyPlayerStatus("offline");
                else if (receivedText.split("сервере ")[1].startsWith("lobby"))
                    stateService.setSpyPlayerStatus("lobby");
                else
                    stateService.setSpyPlayerStatus(chatService.formatLocation(receivedText.split("сервере ")[1]));
                stateService.setSpyPlayerActivity(StringUtils.EMPTY);
                checkingSpy = false;
                shouldUpdate = true;
            }

            if (receivedText.startsWith("Текущая")) {
                String loc = receivedText.split(": ")[1];
                loc = loc.substring(1, loc.length() - 1);
                if (loc.equals("Оффлайн")) {
                    stateService.setSpyPlayerStatus("offline");
                    stateService.setSpyPlayerActivity(StringUtils.EMPTY);
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
                instantUpdate |= stateService.getUserLocation().equals(stateService.getSpyPlayerStatus())
                        && stateService.getSpyPlayerActivity().isEmpty();
                schedulerService.getInstance().schedule(this::update, instantUpdate ? 500 :
                        configManager.getConfig().getSpyDelay(), instantUpdate ? TimeUnit.MILLISECONDS : TimeUnit.SECONDS);
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
        StateService stateService = serviceContext.getStateService();
        if (!stateService.getSpyPlayer().isEmpty()) {
            endSpy();
        }
    }

    private void tryInit() {
        SchedulerService schedulerService = serviceContext.getSchedulerService();
        StateService stateService = serviceContext.getStateService();
        NotificationsService notificationsService = serviceContext.getNotificationsService();

        schedulerService.getInstance().schedule(() -> {
            if (stateService.isGameInitCompleted()) {
                if (enabled) {
                    if (stateService.isInHub()) {
                        stateService.setSpyPlayerActivity(lastKnownLocation = StringUtils.EMPTY);
                        stateService.setSpyPlayerStatus("stop");
                        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех"
                                .formatted(GREEN, BOLD), "Слежка приостановлена.", 5f);
                    } else {
                        if (!stateService.getUserLocation().isEmpty()) {
                            update();
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

    private void startSpy(String player) {
        StateService stateService = serviceContext.getStateService();
        SchedulerService schedulerService = serviceContext.getSchedulerService();
        NotificationsService notificationsService = serviceContext.getNotificationsService();

        resetSpy();
        stateService.setSpyPlayer(player);
        enabled = true;
        this.drawableElement.onStartSpy();
        schedulerService.getInstance().schedule(this::update, 250, TimeUnit.MILLISECONDS);
        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех"
                .formatted(GREEN, BOLD), "Слежка начата", 5f);
    }

    private void endSpy() {
        NotificationsService notificationsService = serviceContext.getNotificationsService();

        resetSpy();
        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех"
                .formatted(GREEN, BOLD), "Слежка остановлена.", 5f);
    }

    private void resetSpy() {
        StateService stateService = serviceContext.getStateService();

        stateService.setSpyPlayer(StringUtils.EMPTY);
        enabled = false;
        this.drawableElement.onResetSpy();
        lastKnownLocation = StringUtils.EMPTY;
        stateService.setSpyPlayerActivity(StringUtils.EMPTY);
        stateService.setSpyPlayerStatus(StringUtils.EMPTY);
    }

    private void update() {
        StateService stateService = serviceContext.getStateService();
        ChatService chatService = serviceContext.getChatService();

        if (!enabled) return;

        checkingSpy = true;
        if (!stateService.isInHub() && stateService.isGameInitCompleted()) {
            if (!stateService.getUserLocation().equals(stateService.getSpyPlayerStatus()))
                chatService.chatMessage("/find %s".formatted(stateService.getSpyPlayer()));
            else
                chatService.chatMessage("/playtime %s".formatted(stateService.getSpyPlayer()));
        }
    }

    @Override
    public int getRenderPriority() {
        return 1001;
    }
}