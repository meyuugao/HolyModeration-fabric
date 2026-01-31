package me.yuugao.holymoderation.client.util.serviceLocator.service;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.config.Config;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CheckoutsService extends Service {

    public void endCheckOut() {
        StateService stateService = ServiceLocator.getStateService();
        ChatService chatService = ServiceLocator.getChatService();
        Config config = ServiceLocator.getConfigManager().getConfig();
        NotificationsService notificationsService = ServiceLocator.getNotificationsService();
        SchedulerService schedulerService = ServiceLocator.getSchedulerService();

        String checkoutPlayer = stateService.getCheckoutPlayer();

        if (!checkoutPlayer.isEmpty()) {
            chatService.chatMessage("/freezing %s".formatted(checkoutPlayer));
            chatService.chatMessage("/prova");
            if (config.isAutoVanishEnabled() && !stateService.isVanishEnabled()) {
                chatService.chatMessage("/v");
                stateService.setVanishEnabled(true);
            }
            if (config.isAutoGm3Enabled() && !stateService.isGm3Enabled()) {
                chatService.chatMessage("/gm 3");
                stateService.setGm3Enabled(true);
            }
        }

        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                "Вы успешно закончили проверку.", 5f);

        String player = new String(checkoutPlayer.toCharArray());

        schedulerService.getInstance().schedule(() -> {
            chatService.clientMessage(chatService.suggestTextComponent("%s%sЗакончить проверку с результатом 'чистый'"
                    .formatted(AQUA, BOLD), "Нажмите, чтобы закончить проверку с результатом 'чистый'", "/hm endcheckout clean"));
            chatService.clientMessage(chatService.suggestTextComponent("%s%sЗакончить проверку с результатом 'бан' + снести стеш"
                    .formatted(AQUA, BOLD), "Нажмите, чтобы закончить проверку с результатом 'бан' + снести стеш", "/hm endcheckout ban %s true".formatted(player)));
            chatService.clientMessage(chatService.suggestTextComponent("%s%sЗакончить проверку с результатом 'бан' + не сносить стеш"
                    .formatted(AQUA, BOLD), "Нажмите, чтобы закончить проверку с результатом 'бан' + не сносить стеш", "/hm endcheckout ban %s false".formatted(player)));
            chatService.clientMessage(chatService.suggestTextComponent("%s%sЗакончить проверку с результатом 'автобай'"
                    .formatted(AQUA, BOLD), "Нажмите, чтобы закончить проверку с результатом 'автобай'", "/hm endcheckout autobuy"));
            chatService.clientMessage(chatService.suggestTextComponent("%s%sЗакончить проверку с результатом 'автоселл'"
                    .formatted(AQUA, BOLD), "Нажмите, чтобы закончить проверку с результатом 'автоселл'", "/hm endcheckout autosell"));
        }, 1, TimeUnit.SECONDS);

        stateService.setCheckoutPlayer(StringUtils.EMPTY);
    }

    public boolean startCheckOut(String player) {
        StateService stateService = ServiceLocator.getStateService();
        ChatService chatService = ServiceLocator.getChatService();
        Config config = ServiceLocator.getConfigManager().getConfig();
        NotificationsService notificationsService = ServiceLocator.getNotificationsService();
        SchedulerService schedulerService = ServiceLocator.getSchedulerService();

        ScheduledExecutorService scheduler = schedulerService.getInstance();

        if (!stateService.getCheckoutPlayer().isEmpty()) {
            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                    "Вы уже проверяете какого-то игрока. Сначала закончите текущую проверку --> %s%s%s/unfreezing%s или %s%s%s/unfrz%s"
                            .formatted(GOLD, GOLD, BOLD, WHITE, GOLD, GOLD, BOLD, WHITE), 5f);
            return false;
        }

        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                "Вы успешно начали проверку.", 5f);

        stateService.setCheckoutPlayer(player);
        String checkoutPlayer = stateService.getCheckoutPlayer();

        chatService.chatMessage("/freezing %s".formatted(checkoutPlayer));
        if (config.isAutoTpEnabled()) {
            chatService.chatMessage("/warp logo");
        }
        chatService.chatMessage("/prova");

        scheduler.schedule(() -> {
            if (!checkoutPlayer.isEmpty()) {
                sendTexts(checkoutPlayer);
            }
        }, 5, TimeUnit.SECONDS);

        scheduler.schedule(() -> {
            if (!checkoutPlayer.isEmpty()) {
                if (config.isDupeIpEnabled()) {
                    chatService.chatMessage("/dupeip %s".formatted(checkoutPlayer));
                }
                chatService.chatMessage("/checkmute %s".formatted(checkoutPlayer));
                if (config.isAutoVanishEnabled() && stateService.isVanishEnabled()) {
                    chatService.chatMessage("/v");
                    stateService.setVanishEnabled(false);
                }
                if (config.isAutoGm3Enabled() && stateService.isGm3Enabled()) {
                    chatService.chatMessage("/gm 0");
                    stateService.setGm3Enabled(false);
                }
            }
        }, 8, TimeUnit.SECONDS);

        scheduler.schedule(() -> {
            if (!checkoutPlayer.isEmpty()) {
                chatService.clientMessage(chatService.suggestTextComponent("%s%sВнести проверку по репорту".formatted(AQUA, BOLD),
                        "Нажмите, чтобы внести проверку по репорту", "/hm startcheckout %s report".formatted(checkoutPlayer)));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sВнести обычную проверку".formatted(AQUA, BOLD),
                        "Нажмите, чтобы внести обычную проверку", "/hm startcheckout %s checkout".formatted(checkoutPlayer)));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sВнести проверку автобаера".formatted(AQUA, BOLD),
                        "Нажмите, чтобы внести проверку автобаера", "/hm startcheckout %s autobuy".formatted(checkoutPlayer)));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sВнести проверку автобаера".formatted(AQUA, BOLD),
                        "Нажмите, чтобы внести проверку автобаера", "/hm startcheckout %s autobuy".formatted(checkoutPlayer)));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sВнести проверку автоселлера".formatted(AQUA, BOLD),
                        "Нажмите, чтобы внести проверку автоселлера", "/hm startcheckout %s autosell".formatted(checkoutPlayer)));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sВнести проверку кандидата".formatted(AQUA, BOLD),
                        "Нажмите, чтобы внести проверку кандидата", "/hm startcheckout %s candidate".formatted(checkoutPlayer)));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sВнести проверку кастомки".formatted(AQUA, BOLD),
                        "Нажмите, чтобы внести проверку кастомки", "/hm startcheckout %s customka".formatted(checkoutPlayer)));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sВнести проверку персонала".formatted(AQUA, BOLD),
                        "Нажмите, чтобы внести проверку персонала", "/hm startcheckout %s personal".formatted(checkoutPlayer)));
                chatService.clientMessage(chatService.suggestTextComponent("%s%sВнести проверку игрока, у которого много пройденных проверок".formatted(AQUA, BOLD),
                        "Нажмите, чтобы внести проверку игрока, у которого много пройденных проверок", "/hm startcheckout %s toManyChecks".formatted(checkoutPlayer)));
            }
        }, 9, TimeUnit.SECONDS);

        return true;
    }

    public void sendTexts(String player) {
        ChatService chatService = ServiceLocator.getChatService();
        Config config = ServiceLocator.getConfigManager().getConfig();
        NotificationsService notificationsService = ServiceLocator.getNotificationsService();
        SchedulerService schedulerService = ServiceLocator.getSchedulerService();

        List<String> textsList = config.getTextsList();
        if (textsList.isEmpty()) {
            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                    "У вас нет настроенных текстов для отправки. Добавить текст --> %s%s%s%s%s"
                            .formatted(GOLD, GOLD, BOLD, "/hm textadd", WHITE), 5f);
        } else {
            for (int i = 0; i < textsList.size(); i++) {
                String text = textsList.get(i);
                schedulerService.getInstance().schedule(() -> chatService.chatMessage("/msg %s %s"
                        .formatted(player, text.replace("§", "&"))), i * 100L, TimeUnit.MILLISECONDS);
            }
        }
    }
}