package me.yuugao.holymoderation.client.util.serviceLocator.service;

import static me.yuugao.holymoderation.client.util.Colors.BOLD;
import static me.yuugao.holymoderation.client.util.Colors.RED;


import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

public class PunishmentsService extends Service {

    public void punish(String punishCommand, String player, String reason, boolean addVk) {
        StateService stateService = ServiceLocator.getStateService();
        ChatService chatService = ServiceLocator.getChatService();
        NotificationService notificationService = ServiceLocator.getNotificationService();

        if (reason.contains(" | Вопросы")) {
            chatService.chatMessage(punishCommand + " " + player + " " + reason + " -s");
            return;
        }
        if (stateService.getVkUrl().isEmpty() && !punishCommand.equals("/tempmute") && !punishCommand.equals("/tempmuteip")) {
            notificationService.addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Не удалось наказать игрока, т.к. не установлена ссылка на вк. Добавьте ссылку на вк в бан самостоятельно в формате ' | Вопросы? vk.com/id' или попробуйте перезайти на сервер.", 5f);
            return;
        }
        chatService.chatMessage(punishCommand + " " + player + " " + reason + (addVk ? " | Вопросы? " + stateService.getVkUrl() + " -s" : " -s"));
    }

    public boolean punish(String punishCommand, String player, String time, String reason, boolean addVk) {
        StateService stateService = ServiceLocator.getStateService();
        ChatService chatService = ServiceLocator.getChatService();
        NotificationService notificationService = ServiceLocator.getNotificationService();

        char lastChar = time.charAt(time.length() - 1);
        String stringTime = time.substring(0, time.length() - 1);
        if (!String.valueOf(lastChar).matches("(?i)[smhd]") || time.length() > 5 || !chatService.checkCorrectInt(stringTime)) {
            notificationService.addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Неверный формат времени. Должно быть 1-9999s/S, 1-9999m/M, 1-9999h/H, 1-9999d/D", 5f);
            return false;
        }

        String r = reason.replace("-s", "").trim();
        if (r.endsWith("-v")) {
            chatService.chatMessage(punishCommand + " " + player + " " + time + " " + r.replace("-v", "").trim() + " -s");
            return true;
        }
        if (stateService.getVkUrl().isEmpty() && !punishCommand.equals("/tempmute") && !punishCommand.equals("/tempmuteip")) {
            notificationService.addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Не удалось наказать игрока, т.к. не установлена ссылка на вк. Добавьте ссылку на вк в бан самостоятельно в формате ' | Вопросы? vk.com/id' или попробуйте перезайти на сервер.", 5f);
            return false;
        }
        chatService.chatMessage(punishCommand + " " + player + " " + time + " " + r + (addVk ? " | Вопросы? " + stateService.getVkUrl() + " -s" : " -s"));
        return true;
    }
}