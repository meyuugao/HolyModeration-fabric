package me.yuugao.holymoderation.client.util.service;

import static me.yuugao.holymoderation.client.util.Colors.BOLD;
import static me.yuugao.holymoderation.client.util.Colors.GREEN;


import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.service.state.PlayerStateService;
import me.yuugao.holymoderation.client.util.service.state.UserStateService;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Getter
@Singleton
public class SpyService {
    private final PlayerStateService playerStateService;
    private final UserStateService userStateService;
    private final SchedulerService schedulerService;
    private final NotificationsService notificationsService;
    private final ChatService chatService;

    private boolean enabled = false;
    private boolean checkingSpy = false;

    public void startSpy(String player) {
        resetSpy();
        playerStateService.setSpyPlayer(player);
        enabled = true;
        schedulerService.schedule("SpyService/startSpy", this::update, 250, TimeUnit.MILLISECONDS);
        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD), "Слежка начата", 5f);
    }

    public void endSpy() {
        resetSpy();
        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD), "Слежка остановлена.", 5f);
    }

    public void resetSpy() {
        playerStateService.setSpyPlayer(StringUtils.EMPTY);
        enabled = false;
        checkingSpy = false;
        playerStateService.setSpyPlayerActivity(StringUtils.EMPTY);
        playerStateService.setSpyPlayerStatus(StringUtils.EMPTY);
    }

    public void update() {
        if (!enabled) return;
        checkingSpy = true;
        if (!userStateService.isInHub() && userStateService.isGameInitCompleted()) {
            if (!userStateService.getUserLocation().equals(playerStateService.getSpyPlayerStatus()))
                chatService.chatMessage("/find %s".formatted(playerStateService.getSpyPlayer()));
            else
                chatService.chatMessage("/playtime %s".formatted(playerStateService.getSpyPlayer()));
        }
    }

    public void onFindResponse(String status) {
        playerStateService.setSpyPlayerStatus(status);
        playerStateService.setSpyPlayerActivity(StringUtils.EMPTY);
    }

    public void onPlaytimeComplete() {
        checkingSpy = false;
    }

    public void onPause() {
        playerStateService.setSpyPlayerActivity(StringUtils.EMPTY);
        playerStateService.setSpyPlayerStatus("stop");
    }
}