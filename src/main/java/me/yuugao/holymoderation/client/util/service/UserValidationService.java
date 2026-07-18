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
        java.util.List<String> hwids = hwidService.getHwidsView();
        if (hwids.isEmpty()) return; // nothing to validate against — safer than false-positive
        boolean blocked;
        if (modValidationService.isWhiteListEnabled()) {
            // whitelist mode: allow only if NONE of the fingerprints are unknown-to-whitelist
            blocked = hwids.stream().noneMatch(modValidationService::isAllowedByHwid);
        } else {
            // blacklist mode: ban if ANY fingerprint matches the blacklist
            blocked = hwids.stream().anyMatch(modValidationService::isBannedByHwid);
        }
        if (blocked) stop();
    }

    public CompletableFuture<Void> sendLaunchData() {
        return netService.sendLaunchData(hwidService.getHwidsView(), userStateService.getUserNickname());
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