package me.yuugao.holymoderation.client.util.serviceLocator.service;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.config.SettingsConfig;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class CheckoutsService extends Service {
    List<ScheduledFuture<?>> futures = new ArrayList<>();

    public void endCheckOut(boolean playerNotFound) {
        StateService stateService = ServiceLocator.getStateService();
        ChatService chatService = ServiceLocator.getChatService();
        NotificationsService notificationsService = ServiceLocator.getNotificationsService();
        SchedulerService schedulerService = ServiceLocator.getSchedulerService();
        SpyService spyService = ServiceLocator.getSpyService();

        SettingsConfig settingsConfig = ServiceLocator.getConfigManager().getSettingsConfig();
        String checkoutPlayer = stateService.getCheckoutPlayer();

        if (!checkoutPlayer.isEmpty()) {
            if (!playerNotFound) {
                chatService.chatMessage("/freezing %s".formatted(checkoutPlayer));
            }
            chatService.chatMessage("/prova");
            if (settingsConfig.isAutoVanishEnabled() && !stateService.isVanishEnabled()) {
                chatService.chatMessage("/v");
                stateService.setVanishEnabled(true);
            }
            if (settingsConfig.isAutoGm3Enabled() && !stateService.isGm3Enabled()) {
                chatService.chatMessage("/gm 3");
                stateService.setGm3Enabled(true);
            }
        }

        futures.forEach(future -> future.cancel(true));

        if (!playerNotFound) {
            notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                    "Вы успешно закончили проверку.", 5f);

            String player = new String(checkoutPlayer.toCharArray());

            schedulerService.getScheduler().schedule(() -> {
                chatService.clientMessage(chatService.suggestTextComponent("%s%sЗакончить проверку с результатом 'чистый'"
                                .formatted(AQUA, BOLD), "Нажмите, чтобы закончить проверку с результатом 'чистый'",
                        "/hm endcheckout clean"));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sЗакончить проверку с результатом 'бан' + снести стеш"
                                .formatted(AQUA, BOLD), "Нажмите, чтобы закончить проверку с результатом 'бан' + снести стеш",
                        "/hm endcheckout ban %s true".formatted(player)));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sЗакончить проверку с результатом 'бан' + не сносить стеш"
                                .formatted(AQUA, BOLD), "Нажмите, чтобы закончить проверку с результатом 'бан' + не сносить стеш",
                        "/hm endcheckout ban %s false".formatted(player)));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sЗакончить проверку с результатом 'автобай'"
                                .formatted(AQUA, BOLD), "Нажмите, чтобы закончить проверку с результатом 'автобай'",
                        "/hm endcheckout autobuy"));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sЗакончить проверку с результатом 'автоселл'"
                                .formatted(AQUA, BOLD), "Нажмите, чтобы закончить проверку с результатом 'автоселл'",
                        "/hm endcheckout autosell"));
            }, 1, TimeUnit.SECONDS);
        } else {
            notificationsService.addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(GOLD, BOLD),
                    "Проверка отменена, потому что игрок не был найден.", 5f);
        }

        if (stateService.getSpyPlayer().equals(stateService.getCheckoutPlayer())) {
            spyService.endSpy();
        }
        stateService.setCheckoutPlayer(StringUtils.EMPTY);
    }

    public boolean startCheckOut(String player) {
        StateService stateService = ServiceLocator.getStateService();
        ChatService chatService = ServiceLocator.getChatService();
        NotificationsService notificationsService = ServiceLocator.getNotificationsService();
        SchedulerService schedulerService = ServiceLocator.getSchedulerService();

        SettingsConfig settingsConfig = ServiceLocator.getConfigManager().getSettingsConfig();
        ScheduledExecutorService scheduler = schedulerService.getScheduler();

        if (!stateService.getCheckoutPlayer().isEmpty()) {
            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                    "Вы уже проверяете какого-то игрока. Сначала закончите текущую проверку --> %s%s%s/unfreezing%s или %s%s%s/unfrz%s"
                            .formatted(GOLD, GOLD, BOLD, WHITE, GOLD, GOLD, BOLD, WHITE), 5f);
            return false;
        }

        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                "Вы успешно начали проверку.", 5f);

        stateService.setCheckoutPlayer(player);

        chatService.chatMessage("/freezing %s".formatted(stateService.getCheckoutPlayer()));
        if (settingsConfig.isAutoCheckoutTpEnabled()) {
            chatService.chatMessage("/warp logo");
        }
        chatService.chatMessage("/prova");

        futures.add(scheduler.schedule(() -> {
            if (!stateService.getCheckoutPlayer().isEmpty()) {
                sendTexts(stateService.getCheckoutPlayer());
            }
        }, 5, TimeUnit.SECONDS));

        futures.add(scheduler.schedule(() -> {
            if (!stateService.getCheckoutPlayer().isEmpty()) {
                if (settingsConfig.isDupeIpEnabled()) {
                    chatService.chatMessage("/dupeip %s".formatted(stateService.getCheckoutPlayer()));
                }
                chatService.chatMessage("/checkmute %s".formatted(stateService.getCheckoutPlayer()));
                if (settingsConfig.isAutoVanishEnabled() && stateService.isVanishEnabled()) {
                    chatService.chatMessage("/v");
                    stateService.setVanishEnabled(false);
                }
                if (settingsConfig.isAutoGm3Enabled() && stateService.isGm3Enabled()) {
                    chatService.chatMessage("/gm 0");
                    stateService.setGm3Enabled(false);
                }
            }
        }, 8, TimeUnit.SECONDS));

        futures.add(scheduler.schedule(() -> {
            if (!stateService.getCheckoutPlayer().isEmpty()) {
                chatService.clientMessage(chatService.suggestTextComponent("%s%sВнести проверку по репорту".formatted(AQUA, BOLD),
                        "Нажмите, чтобы внести проверку по репорту", "/hm startcheckout %s report".formatted(stateService.getCheckoutPlayer())));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sВнести обычную проверку".formatted(AQUA, BOLD),
                        "Нажмите, чтобы внести обычную проверку", "/hm startcheckout %s checkout".formatted(stateService.getCheckoutPlayer())));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sВнести проверку автобаера".formatted(AQUA, BOLD),
                        "Нажмите, чтобы внести проверку автобаера", "/hm startcheckout %s autobuy".formatted(stateService.getCheckoutPlayer())));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sВнести проверку автобаера".formatted(AQUA, BOLD),
                        "Нажмите, чтобы внести проверку автобаера", "/hm startcheckout %s autobuy".formatted(stateService.getCheckoutPlayer())));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sВнести проверку автоселлера".formatted(AQUA, BOLD),
                        "Нажмите, чтобы внести проверку автоселлера", "/hm startcheckout %s autosell".formatted(stateService.getCheckoutPlayer())));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sВнести проверку кандидата".formatted(AQUA, BOLD),
                        "Нажмите, чтобы внести проверку кандидата", "/hm startcheckout %s candidate".formatted(stateService.getCheckoutPlayer())));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sВнести проверку кастомки".formatted(AQUA, BOLD),
                        "Нажмите, чтобы внести проверку кастомки", "/hm startcheckout %s customka".formatted(stateService.getCheckoutPlayer())));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sВнести проверку персонала".formatted(AQUA, BOLD),
                        "Нажмите, чтобы внести проверку персонала", "/hm startcheckout %s personal".formatted(stateService.getCheckoutPlayer())));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sВнести проверку игрока, у которого много пройденных проверок".formatted(AQUA, BOLD),
                        "Нажмите, чтобы внести проверку игрока, у которого много пройденных проверок", "/hm startcheckout %s toManyChecks".formatted(stateService.getCheckoutPlayer())));
            }
        }, 9, TimeUnit.SECONDS));

        return true;
    }

    public void sendTexts(String player) {
        ChatService chatService = ServiceLocator.getChatService();
        NotificationsService notificationsService = ServiceLocator.getNotificationsService();
        SchedulerService schedulerService = ServiceLocator.getSchedulerService();

        SettingsConfig settingsConfig = ServiceLocator.getConfigManager().getSettingsConfig();

        List<String> textsList = settingsConfig.getTextsList();
        if (textsList.isEmpty()) {
            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                    "У вас нет настроенных текстов для отправки. Добавить текст --> %s%s%s%s%s"
                            .formatted(GOLD, GOLD, BOLD, "/hm textadd", WHITE), 5f);
        } else {
            for (int i = 0; i < textsList.size(); i++) {
                String text = textsList.get(i);
                schedulerService.getScheduler().schedule(() -> chatService.chatMessage("/msg %s %s"
                        .formatted(player, text.replace("§", "&"))), i * 100L, TimeUnit.MILLISECONDS);
            }
        }
    }
}