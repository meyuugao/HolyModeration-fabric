package me.yuugao.holymoderation.client.util.serviceLocator.service;

import static me.yuugao.holymoderation.client.util.Colors.BOLD;
import static me.yuugao.holymoderation.client.util.Colors.GREEN;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

import lombok.Getter;

@Getter
public class SpyService extends Service {
    private boolean enabled = false;
    private boolean checkingSpy = false;

    public void startSpy(String player) {
        StateService stateService = ServiceLocator.getStateService();
        SchedulerService schedulerService = ServiceLocator.getSchedulerService();
        NotificationsService notificationsService = ServiceLocator.getNotificationsService();

        resetSpy();
        stateService.setSpyPlayer(player);
        enabled = true;
        schedulerService.getScheduler().schedule(this::update, 250, TimeUnit.MILLISECONDS);
        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех"
                .formatted(GREEN, BOLD), "Слежка начата", 5f);
    }

    public void endSpy() {
        NotificationsService notificationsService = ServiceLocator.getNotificationsService();

        resetSpy();
        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех"
                .formatted(GREEN, BOLD), "Слежка остановлена.", 5f);
    }

    public void resetSpy() {
        StateService stateService = ServiceLocator.getStateService();

        stateService.setSpyPlayer(StringUtils.EMPTY);
        enabled = false;
        checkingSpy = false;
        stateService.setSpyPlayerActivity(StringUtils.EMPTY);
        stateService.setSpyPlayerStatus(StringUtils.EMPTY);
    }

    public void update() {
        StateService stateService = ServiceLocator.getStateService();
        ChatService chatService = ServiceLocator.getChatService();

        if (!enabled) return;

        checkingSpy = true;
        if (!stateService.isInHub() && stateService.isGameInitCompleted()) {
            if (!stateService.getUserLocation().equals(stateService.getSpyPlayerStatus()))
                chatService.chatMessage("/find %s".formatted(stateService.getSpyPlayer()));
            else
                chatService.chatMessage("/playtime %s".formatted(stateService.getSpyPlayer()));
        }
    }

    public void onFindResponse(String status) {
        StateService stateService = ServiceLocator.getStateService();

        stateService.setSpyPlayerStatus(status);
        stateService.setSpyPlayerActivity(StringUtils.EMPTY);
        checkingSpy = false;
    }

    public void onPlaytimeComplete() {
        checkingSpy = false;
    }

    public void onPause() {
        StateService stateService = ServiceLocator.getStateService();

        stateService.setSpyPlayerActivity(StringUtils.EMPTY);
        stateService.setSpyPlayerStatus("stop");
    }
}