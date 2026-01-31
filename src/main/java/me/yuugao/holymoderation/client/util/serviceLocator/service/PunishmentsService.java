package me.yuugao.holymoderation.client.util.serviceLocator.service;

import static me.yuugao.holymoderation.client.util.Colors.BOLD;
import static me.yuugao.holymoderation.client.util.Colors.RED;


import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

import org.apache.commons.lang3.StringUtils;

public class PunishmentsService extends Service {

    public void punish(String punishCommand, String player, String reason, boolean addVk) {
        StateService stateService = ServiceLocator.getStateService();
        ChatService chatService = ServiceLocator.getChatService();
        NotificationsService notificationsService = ServiceLocator.getNotificationsService();

        if (reason.contains(" | Вопросы")) {
            chatService.chatMessage("%s %s %s -s".formatted(punishCommand, player, reason));
            return;
        }
        if (stateService.getVkUrl().isEmpty() && !punishCommand.equals("/tempmute") && !punishCommand.equals("/tempmuteip")) {
            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD), "Не удалось наказать игрока, т.к. не установлена ссылка на вк. Добавьте ссылку на вк в бан самостоятельно в формате ' | Вопросы? vk.com/id' или попробуйте перезайти на сервер.", 5f);
            return;
        }
        chatService.chatMessage("%s %s %s %s".formatted(punishCommand, player, reason, addVk ? " | Вопросы? %s -s".formatted(stateService.getVkUrl()) : " -s"));
    }

    public boolean punish(String punishCommand, String player, String time, String reason, boolean addVk) {
        StateService stateService = ServiceLocator.getStateService();
        ChatService chatService = ServiceLocator.getChatService();
        NotificationsService notificationsService = ServiceLocator.getNotificationsService();

        char lastChar = time.charAt(time.length() - 1);
        String stringTime = time.substring(0, time.length() - 1);
        if (!String.valueOf(lastChar).matches("(?i)[smhd]") || time.length() > 5 || !chatService.checkCorrectInt(stringTime)) {
            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD), "Неверный формат времени. Должно быть 1-9999s/S, 1-9999m/M, 1-9999h/H, 1-9999d/D", 5f);
            return false;
        }

        String r = reason.replace("-s", StringUtils.EMPTY).trim();
        if (r.endsWith("-v")) {
            chatService.chatMessage("%s %s %s %s -s".formatted(punishCommand, player, time, r.replace("-v", StringUtils.EMPTY).trim()));
            return true;
        }
        if (stateService.getVkUrl().isEmpty() && !punishCommand.equals("/tempmute") && !punishCommand.equals("/tempmuteip")) {
            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD), "Не удалось наказать игрока, т.к. не установлена ссылка на вк. Добавьте ссылку на вк в бан самостоятельно в формате ' | Вопросы? vk.com/id' или попробуйте перезайти на сервер.", 5f);
            return false;
        }
        chatService.chatMessage("%s %s %s %s%s".formatted(punishCommand, player, time, r, addVk ? " | Вопросы? %s -s".formatted(stateService.getVkUrl()) : " -s"));
        return true;
    }
}