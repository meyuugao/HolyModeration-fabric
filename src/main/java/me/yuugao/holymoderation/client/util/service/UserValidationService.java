package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.service.state.ModStateService;
import me.yuugao.holymoderation.client.util.service.state.UserStateService;
import me.yuugao.holymoderation.client.util.viewer.FrameViewer;

import java.util.concurrent.CompletableFuture;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class UserValidationService {
    private final ModValidationService modValidationService;
    private final HwidService hwidService;
    private final UserStateService userStateService;
    private final NetService netService;
    private final MinecraftService minecraftService;
    private final ModStateService modStateService;
    private final FrameViewer frameViewer;

    public void onMinecraftStart() {
        modValidationService.updateLists().thenRun(this::validateHwid);
    }

    public void onJoinServer() {
        sendLaunchData().thenRun(() -> modValidationService.updateLists().thenRun(this::validateNickname));
    }

    private void validateHwid() {
        String hwid = hwidService.getHwid();
        if (modValidationService.isWhiteListEnabled()
                ? !modValidationService.isAllowedByHwid(hwid)
                : modValidationService.isBannedByHwid(hwid)) {
            stop();
        }
    }

    public CompletableFuture<Void> sendLaunchData() {
        String hwid = hwidService.getHwid();
        return netService.sendLaunchData(hwid, userStateService.getUserNickname());
    }

    private void validateNickname() {
        String nickname = userStateService.getUserNickname();
        if (modValidationService.isWhiteListEnabled()
                ? !modValidationService.isAllowedByNickname(nickname)
                : modValidationService.isBannedByNickname(nickname)) {
            stop();
        }
    }

    private void stop() {
        minecraftService.getClient().execute(() -> {
            modStateService.forceBlock();
            frameViewer.open();
             minecraftService.getClient().stop();
        });
    }
}