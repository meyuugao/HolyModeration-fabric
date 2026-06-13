package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.service.state.ModStateService;
import me.yuugao.holymoderation.client.util.service.state.UserStateService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class UserValidationService {
    private final ModBlackListService modBlackListService;
    private final HwidService hwidService;
    private final UserStateService userStateService;
    private final AsyncExecutor asyncExecutor;
    private final NetService netService;
    private final MinecraftService minecraftService;
    private final ModStateService modStateService;

    public void onMinecraftStart() {
        modBlackListService.updateLists().thenRun(this::validateHwid);
    }

    public void onJoinServer() {
        sendLaunchData();
        modBlackListService.updateLists().thenRun(this::validateNickname);
    }

    private void validateHwid() {
        String hwid = hwidService.getHwid();
        if (modBlackListService.isBannedByHwid(hwid)) {
            //tip: stop();
        }
    }

    private void sendLaunchData() {
        String hwid = hwidService.getHwid();
        asyncExecutor.runAsync("StateModule/TrOBV", () -> netService.sendLaunchData(hwid, userStateService.getUserNickname()));
    }

    private void validateNickname() {
        String nickname = userStateService.getUserNickname();
        if (modBlackListService.isBannedByNickname(nickname)) {
            //tip: stop();
        }
    }

    private void stop() {
        minecraftService.getClient().execute(() -> {
            modStateService.block();
            minecraftService.getClient().stop();
        });
    }
}