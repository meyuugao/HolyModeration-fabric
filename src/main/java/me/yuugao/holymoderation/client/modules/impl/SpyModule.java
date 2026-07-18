package me.yuugao.holymoderation.client.modules.impl;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.singleton.SpyDrawableElement;
import me.yuugao.holymoderation.client.modules.DrawableModule;
import me.yuugao.holymoderation.client.util.command.Argument;
import me.yuugao.holymoderation.client.util.command.CommandContext;
import me.yuugao.holymoderation.client.util.command.CommandProvider;
import me.yuugao.holymoderation.client.util.command.CommandRegistry;
import me.yuugao.holymoderation.client.util.command.CommandSpec;
import me.yuugao.holymoderation.client.util.service.*;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.config.impl.SettingsConfig;
import me.yuugao.holymoderation.client.util.service.eventbus.Subscribe;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.chat.MessageReceiveEvent;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.connection.ServerConnectEvent;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.connection.ServerDisconnectEvent;
import me.yuugao.holymoderation.client.util.service.state.PlayerStateService;
import me.yuugao.holymoderation.client.util.service.state.UserStateService;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

@Singleton
public class SpyModule extends DrawableModule<SpyDrawableElement> implements CommandProvider {
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

    @Override
    public void registerCommands(CommandRegistry registry) {
        registry.register(CommandSpec.of("spy", Argument.player("игрок")).group("Слежка").description("следить за игроком (без аргумента — остановить)").handler(this::cmdSpy));
        registry.register(CommandSpec.of("spyfrz").group("Слежка").description("заморозить игрока со слежки").handler(this::cmdSpyfrz));
    }

    private void cmdSpy(CommandContext ctx) {
        if (!ctx.hasArg(0)) {
            if (!playerStateService.getSpyPlayer().isEmpty()) {
                spyService.endSpy();
            } else {
                notificationsService.warning("Вы никого не отслеживаете.");
            }
            return;
        }

        String target = ctx.arg(0);

        if (userStateService.isInHub()) {
            notificationsService.warning("В хабе этого делать нельзя.");
            return;
        }

        if (!playerStateService.getCheckoutPlayer().isEmpty() && playerStateService.getCheckoutPlayer().equals(target)) {
            notificationsService.warning("Вы не можете начать следить за игроком на вашей проверке.");
            return;
        }

        if (!playerStateService.getSpyPlayer().isEmpty()) {
            notificationsService.warning("Вы уже следите за кем-то --> %s%s/hm spy%s%s%s.".formatted(GOLD, BOLD, WHITE, RED, BOLD));
            return;
        }

        spyService.startSpy(target);
    }

    private void cmdSpyfrz(CommandContext ctx) {
        if (userStateService.isInHub()) {
            notificationsService.warning("В хабе этого делать нельзя.");
            return;
        }

        if (playerStateService.getSpyPlayer().isEmpty()) {
            notificationsService.error("Вы ни за кем не следите.");
            return;
        }

        if (checkoutsService.startCheckOut(playerStateService.getSpyPlayer())) {
            spyService.endSpy();
        }
    }

    @Subscribe(priority = 98)
    public void onMessageReceive(MessageReceiveEvent event) {
        SettingsConfig settingsConfig = configManagerService.getSettingsConfig();

        String receivedText = chatService.formatReceivedText(event.getMessage().getString());
        if (receivedText == null) return;

        boolean isChecking = spyService.isCheckingSpy();

        if (isChecking) {
            if (receivedText.startsWith(HolyWorldPatterns.PLAYTIME_SEPARATOR)) {
                if (processingPlaytimeInfo) {
                    spyService.onPlaytimeComplete();
                    shouldUpdate = true;
                    processingPlaytimeInfo = false;
                } else {
                    processingPlaytimeInfo = true;
                }
            }

            if (HolyWorldPatterns.isFindResponseAboutOther(receivedText, userStateService.getUserNickname())) {
                event.setCancelled(true);
                String status = HolyWorldPatterns.parseFindStatus(receivedText);
                spyService.onFindResponse(status);
                shouldUpdate = true;
            }

            String playtimeLoc = HolyWorldPatterns.extractPlaytimeLocation(receivedText);
            if (playtimeLoc != null) {
                if (playtimeLoc.equals(HolyWorldPatterns.OFFLINE_LITERAL)) {
                    processingPlaytimeInfo = true;
                    spyService.onFindResponse("offline");
                    instantUpdate = true;
                } else {
                    if (lastKnownLocation.isEmpty()) lastKnownLocation = playtimeLoc;
                    else if (!playtimeLoc.equals(lastKnownLocation)) {
                        playerStateService.setSpyPlayerStatus(StringUtils.EMPTY);
                        lastKnownLocation = playtimeLoc;
                    }
                }
            }

            if (receivedText.startsWith(HolyWorldPatterns.PLAYTIME_LAST_PREFIX) && !playerStateService.getSpyPlayerStatus().isEmpty()) {
                playerStateService.setSpyPlayerActivity(HolyWorldPatterns.extractPlaytimeActivity(receivedText));
            }

            if (HolyWorldPatterns.isPlaytimeNoise(receivedText)) {
                event.setCancelled(true);
            }

            if (shouldUpdate) {
                if (userStateService.getUserLocation().equals(playerStateService.getSpyPlayerStatus())
                        && playerStateService.getSpyPlayerActivity().isEmpty()) {
                    instantUpdate = true;
                    if (settingsConfig.isAutoSpyTpEnabled()) {
                        chatService.chatMessage("/tpo %s".formatted(playerStateService.getSpyPlayer()));
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
                        notificationsService.success("Слежка приостановлена.");
                    } else {
                        if (!userStateService.getUserLocation().isEmpty()) {
                            spyService.update();
                            notificationsService.success("Слежка возобновлена.");
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