package me.yuugao.holymoderation.client.util.service;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.config.impl.SettingsConfig;
import me.yuugao.holymoderation.client.util.service.state.PlayerStateService;
import me.yuugao.holymoderation.client.util.service.state.UserStateService;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class CheckoutsService {
    private final PlayerStateService playerStateService;
    private final UserStateService userStateService;
    private final ChatService chatService;
    private final NotificationsService notificationsService;
    private final SchedulerService schedulerService;
    private final SpyService spyService;
    private final ConfigManagerService configManagerService;

    private final List<ScheduledFuture<?>> futures = new ArrayList<>();

    public void endCheckOut(boolean playerNotFound, boolean sendUnfreeze) {
        SettingsConfig settingsConfig = configManagerService.getSettingsConfig();
        String checkoutPlayer = playerStateService.getCheckoutPlayer();

        if (!checkoutPlayer.isEmpty()) {
            chatService.chatMessage("/prova");
            if (sendUnfreeze) {
                chatService.chatMessage("/freezing %s".formatted(checkoutPlayer));
            }
            if (settingsConfig.isAutoVanishEnabled() && !userStateService.isVanishEnabled()) {
                chatService.chatMessage("/v");
                userStateService.setVanishEnabled(true);
            }
            if (settingsConfig.isAutoGm3Enabled() && !userStateService.isGm3Enabled()) {
                chatService.chatMessage("/gm 3");
                userStateService.setGm3Enabled(true);
            }
        }

        futures.forEach(future -> future.cancel(true));

        if (!playerNotFound) {
            notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                    "Вы успешно закончили проверку.", 5f);

            String player = new String(checkoutPlayer.toCharArray());

            schedulerService.schedule("CheckoutsService/endCheckout", () -> {
                chatService.clientMessage(chatService.suggestTextComponent("%s%sЗакончить проверку с результатом 'чистый'"
                                .formatted(AQUA, BOLD), "Нажмите, чтобы закончить проверку с результатом 'чистый'",
                        "/hm endcheckout clean"));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sЗакончить проверку с результатом 'бан' + снести стеш"
                                .formatted(AQUA, BOLD), "Нажмите, чтобы закончить проверку с результатом 'бан' + снести стеш",
                        "/hm endcheckout ban %s true".formatted(player)));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sЗакончить проверку с результатом 'бан' + не сносить стеш"
                                .formatted(AQUA, BOLD), "Нажмите, чтобы закончить проверку с результатом 'бан' + не сносить стеш",
                        "/hm endcheckout ban %s false".formatted(player)));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sЗакончить проверку с результатом 'автобай' + снести стеш"
                                .formatted(AQUA, BOLD), "Нажмите, чтобы закончить проверку с результатом 'автобай'",
                        "/hm endcheckout autobuy true"));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sЗакончить проверку с результатом 'автобай' + не сносить стеш"
                                .formatted(AQUA, BOLD), "Нажмите, чтобы закончить проверку с результатом 'автобай'",
                        "/hm endcheckout autobuy false"));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sЗакончить проверку с результатом 'автоселл' + снести стеш"
                                .formatted(AQUA, BOLD), "Нажмите, чтобы закончить проверку с результатом 'автоселл'",
                        "/hm endcheckout autosell true"));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sЗакончить проверку с результатом 'автоселл' + не сносить стеш"
                                .formatted(AQUA, BOLD), "Нажмите, чтобы закончить проверку с результатом 'автоселл'",
                        "/hm endcheckout autosell false"));
            }, 1, TimeUnit.SECONDS);
        } else {
            notificationsService.addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(GOLD, BOLD),
                    "Проверка отменена, потому что игрок не был найден.", 5f);
        }

        if (playerStateService.getSpyPlayer().equals(playerStateService.getCheckoutPlayer())) {
            spyService.endSpy();
        }
        playerStateService.setCheckoutPlayer(StringUtils.EMPTY);
    }

    public boolean startCheckOut(String player) {
        SettingsConfig settingsConfig = configManagerService.getSettingsConfig();

        if (!playerStateService.getCheckoutPlayer().isEmpty()) {
            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                    "Вы уже проверяете какого-то игрока. Сначала закончите текущую проверку --> %s%s%s/unfreezing%s или %s%s%s/unfrz%s"
                            .formatted(GOLD, GOLD, BOLD, WHITE, GOLD, GOLD, BOLD, WHITE), 5f);
            return false;
        }

        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                "Вы успешно начали проверку.", 5f);

        playerStateService.setCheckoutPlayer(player);

        chatService.chatMessage("/freezing %s".formatted(playerStateService.getCheckoutPlayer()));
        if (settingsConfig.isAutoCheckoutTpEnabled()) {
            chatService.chatMessage("/warp logo");
        }
        chatService.chatMessage("/prova");

        futures.add(schedulerService.schedule("CheckoutService/startCheckOut", () -> {
            if (!playerStateService.getCheckoutPlayer().isEmpty()) {
                sendTexts(playerStateService.getCheckoutPlayer());
            }
        }, 5, TimeUnit.SECONDS));

        futures.add(schedulerService.schedule("CheckoutService/startCheckOut", () -> {
            if (!playerStateService.getCheckoutPlayer().isEmpty()) {
                if (settingsConfig.isDupeIpEnabled()) {
                    chatService.chatMessage("/dupeip %s".formatted(playerStateService.getCheckoutPlayer()));
                }
                chatService.chatMessage("/checkmute %s".formatted(playerStateService.getCheckoutPlayer()));
                if (settingsConfig.isAutoVanishEnabled() && userStateService.isVanishEnabled()) {
                    chatService.chatMessage("/v");
                    userStateService.setVanishEnabled(false);
                }
                if (settingsConfig.isAutoGm3Enabled() && userStateService.isGm3Enabled()) {
                    chatService.chatMessage("/gm 0");
                    userStateService.setGm3Enabled(false);
                }
            }
        }, 8, TimeUnit.SECONDS));

        futures.add(schedulerService.schedule("CheckoutService/startCheckOut", () -> {
            if (!playerStateService.getCheckoutPlayer().isEmpty()) {
                chatService.clientMessage(chatService.suggestTextComponent("%s%sВнести проверку по репорту".formatted(AQUA, BOLD),
                        "Нажмите, чтобы внести проверку по репорту", "/hm startcheckout %s report".formatted(playerStateService.getCheckoutPlayer())));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sВнести обычную проверку".formatted(AQUA, BOLD),
                        "Нажмите, чтобы внести обычную проверку", "/hm startcheckout %s checkout".formatted(playerStateService.getCheckoutPlayer())));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sВнести проверку автобаера".formatted(AQUA, BOLD),
                        "Нажмите, чтобы внести проверку автобаера", "/hm startcheckout %s autobuy".formatted(playerStateService.getCheckoutPlayer())));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sВнести проверку автоселлера".formatted(AQUA, BOLD),
                        "Нажмите, чтобы внести проверку автоселлера", "/hm startcheckout %s autosell".formatted(playerStateService.getCheckoutPlayer())));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sВнести проверку кандидата".formatted(AQUA, BOLD),
                        "Нажмите, чтобы внести проверку кандидата", "/hm startcheckout %s candidate".formatted(playerStateService.getCheckoutPlayer())));
            }
        }, 9, TimeUnit.SECONDS));

        return true;
    }

    public void sendTexts(String player) {
        SettingsConfig settingsConfig = configManagerService.getSettingsConfig();
        List<String> textsList = settingsConfig.getTextsList();
        if (textsList.isEmpty()) {
            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                    "У вас нет настроенных текстов для отправки. Добавить текст --> %s%s%s%s%s"
                            .formatted(GOLD, GOLD, BOLD, "/hm textadd", WHITE), 5f);
        } else {
            for (int i = 0; i < textsList.size(); i++) {
                String text = textsList.get(i);
                schedulerService.schedule("CheckoutsService/sendTexts", () -> chatService.chatMessage("/msg %s %s"
                        .formatted(player, text.replace("§", "&"))), i * 100L, TimeUnit.MILLISECONDS);
            }
        }
    }
}