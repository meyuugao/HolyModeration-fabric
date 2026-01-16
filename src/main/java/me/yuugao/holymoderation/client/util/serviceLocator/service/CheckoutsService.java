package me.yuugao.holymoderation.client.util.serviceLocator.service;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.config.Config;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class CheckoutsService extends Service {

    public void endCheckOut() {
        StateService stateService = ServiceLocator.getStateService();
        ChatService chatService = ServiceLocator.getChatService();
        Config config = ServiceLocator.getConfigManager().getConfig();
        NotificationService notificationService = ServiceLocator.getNotificationService();
        SchedulerService schedulerService = ServiceLocator.getSchedulerService();

        if (!stateService.getPlayer().isEmpty()) {
            chatService.chatMessage("/freezing " + stateService.getPlayer());
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

        notificationService.addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Вы успешно закончили проверку.", 5f);

        String player = new String(stateService.getPlayer().toCharArray());

        schedulerService.getInstance().schedule(() -> {
            chatService.clientMessage(chatService.suggestTextComponent(AQUA + BOLD + "Закончить проверку с результатом 'чистый'", "Нажмите, чтобы закончить проверку с результатом 'чистый'", "/hm endcheckout clean"));
            chatService.clientMessage(chatService.suggestTextComponent(AQUA + BOLD + "Закончить проверку с результатом 'бан' + снести стеш", "Нажмите, чтобы закончить проверку с результатом 'бан' + снести стеш", "/hm endcheckout ban " + player + " true"));
            chatService.clientMessage(chatService.suggestTextComponent(AQUA + BOLD + "Закончить проверку с результатом 'бан' + не сносить стеш", "Нажмите, чтобы закончить проверку с результатом 'бан' + не сносить стеш", "/hm endcheckout ban " + player + " false"));
            chatService.clientMessage(chatService.suggestTextComponent(AQUA + BOLD + "Закончить проверку с результатом 'автобай'", "Нажмите, чтобы закончить проверку с результатом 'автобай'", "/hm endcheckout autobuy"));
            chatService.clientMessage(chatService.suggestTextComponent(AQUA + BOLD + "Закончить проверку с результатом 'автоселл'", "Нажмите, чтобы закончить проверку с результатом 'автоселл'", "/hm endcheckout autosell"));
        }, 1, TimeUnit.SECONDS);

        stateService.setPlayer(StringUtils.EMPTY);
    }

    public boolean startCheckOut(String player) {
        StateService stateService = ServiceLocator.getStateService();
        ChatService chatService = ServiceLocator.getChatService();
        Config config = ServiceLocator.getConfigManager().getConfig();
        NotificationService notificationService = ServiceLocator.getNotificationService();
        SchedulerService schedulerService = ServiceLocator.getSchedulerService();

        if (!stateService.getPlayer().isEmpty()) {
            notificationService.addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы уже проверяете какого-то игрока. Сначала закончите текущую проверку --> " + GOLD + GOLD + BOLD + "/unfreezing" + WHITE + " или " + GOLD + GOLD + BOLD + "/unfrz" + WHITE, 5f);
            return false;
        }

        notificationService.addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Вы успешно начали проверку.", 5f);

        stateService.setPlayer(player);

        chatService.chatMessage("/freezing " + stateService.getPlayer());
        if (config.isAutoTpEnabled()) {
            chatService.chatMessage("/warp logo");
        }
        chatService.chatMessage("/prova");

        schedulerService.getInstance().schedule(() -> {
            if (!stateService.getPlayer().isEmpty()) {
                sendTexts(stateService.getPlayer());
            }
        }, 5, TimeUnit.SECONDS);

        schedulerService.getInstance().schedule(() -> {
            if (!stateService.getPlayer().isEmpty()) {
                if (config.isDupeIpEnabled()) {
                    chatService.chatMessage("/dupeip " + stateService.getPlayer());
                }
                chatService.chatMessage("/checkmute " + stateService.getPlayer());
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

        schedulerService.getInstance().schedule(() -> {
            if (!stateService.getPlayer().isEmpty()) {
                chatService.clientMessage(chatService.suggestTextComponent(AQUA + BOLD + "Внести проверку по репорту", "Нажмите, чтобы внести проверку по репорту", "/hm startcheckout " + stateService.getPlayer() + " report"));
                chatService.clientMessage(chatService.suggestTextComponent(AQUA + BOLD + "Внести обычную проверку", "Нажмите, чтобы внести обычную проверку", "/hm startcheckout " + stateService.getPlayer() + " checkout"));
                chatService.clientMessage(chatService.suggestTextComponent(AQUA + BOLD + "Внести проверку автобаера", "Нажмите, чтобы внести проверку автобаера", "/hm startcheckout " + stateService.getPlayer() + " autobuy"));
                chatService.clientMessage(chatService.suggestTextComponent(AQUA + BOLD + "Внести проверку автобаера", "Нажмите, чтобы внести проверку автобаера", "/hm startcheckout " + stateService.getPlayer() + " autobuy"));
                chatService.clientMessage(chatService.suggestTextComponent(AQUA + BOLD + "Внести проверку автоселлера", "Нажмите, чтобы внести проверку автоселлера", "/hm startcheckout " + stateService.getPlayer() + " autosell"));
                chatService.clientMessage(chatService.suggestTextComponent(AQUA + BOLD + "Внести проверку кандидата", "Нажмите, чтобы внести проверку кандидата", "/hm startcheckout " + stateService.getPlayer() + " candidate"));
                chatService.clientMessage(chatService.suggestTextComponent(AQUA + BOLD + "Внести проверку кастомки", "Нажмите, чтобы внести проверку кастомки", "/hm startcheckout " + stateService.getPlayer() + " customka"));
                chatService.clientMessage(chatService.suggestTextComponent(AQUA + BOLD + "Внести проверку персонала", "Нажмите, чтобы внести проверку персонала", "/hm startcheckout " + stateService.getPlayer() + " personal"));
                chatService.clientMessage(chatService.suggestTextComponent(AQUA + BOLD + "Внести проверку игрока, у которого много пройденных проверок", "Нажмите, чтобы внести проверку игрока, у которого много пройденных проверок", "/hm startcheckout " + stateService.getPlayer() + " toManyChecks"));
            }
        }, 9, TimeUnit.SECONDS);

        return true;
    }

    public void sendTexts(String player) {
        ChatService chatService = ServiceLocator.getChatService();
        Config config = ServiceLocator.getConfigManager().getConfig();
        NotificationService notificationService = ServiceLocator.getNotificationService();
        SchedulerService schedulerService = ServiceLocator.getSchedulerService();

        List<String> textsList = config.getTextsList();
        if (textsList.isEmpty()) {
            notificationService.addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "У вас нет настроенных текстов для отправки. Добавить текст --> " + GOLD + GOLD + BOLD + "/hm textadd" + WHITE, 5f);
        } else {
            for (int i = 0; i < textsList.size(); i++) {
                String text = textsList.get(i);
                schedulerService.getInstance().schedule(() -> chatService.chatMessage("/msg " + player + " " + text.replace("§", "&")), i * 100L, TimeUnit.MILLISECONDS);
            }
        }
    }
}