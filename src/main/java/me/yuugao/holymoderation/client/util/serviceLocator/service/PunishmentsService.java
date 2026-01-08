package me.yuugao.holymoderation.client.util.serviceLocator.service;

import static me.yuugao.holymoderation.client.util.Colors.BOLD;
import static me.yuugao.holymoderation.client.util.Colors.RED;


import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

public class PunishmentsService extends Service {
    public void punish(String punishCommand, String player, String reason, boolean addVk, ServiceContext serviceContext) {
        if (reason.contains(" | Вопросы")) {
            serviceContext.getChatService().chatMessage(punishCommand + " " + player + " " + reason + " -s");
            return;
        }
        if (serviceContext.getStateService().getVkUrl().isEmpty() && !punishCommand.equals("/tempmute") && !punishCommand.equals("/tempmuteip")) {
            ServiceLocator.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Не удалось наказать игрока, т.к. не установлена ссылка на вк. Добавьте ссылку на вк в бан самостоятельно в формате ' | Вопросы? vk.com/id' или попробуйте перезайти на сервер.", 5f);
            return;
        }
        serviceContext.getChatService().chatMessage(punishCommand + " " + player + " " + reason + (addVk ? " | Вопросы? " + serviceContext.getStateService().getVkUrl() + " -s" : " -s"));
    }

    public boolean punish(String punishCommand, String player, String time, String reason, boolean addVk, ServiceContext serviceContext) {
        char lastChar = time.charAt(time.length() - 1);
        String stringTime = time.substring(0, time.length() - 1);
        if (!String.valueOf(lastChar).matches("(?i)[smhd]") || time.length() > 5 || !serviceContext.getChatService().checkCorrectInt(stringTime)) {
            ServiceLocator.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Неверный формат времени. Должно быть 1-9999s/S, 1-9999m/M, 1-9999h/H, 1-9999d/D", 5f);
            return false;
        }

        String r = reason.replace("-s", "").trim();
        if (r.endsWith("-v")) {
            serviceContext.getChatService().chatMessage(punishCommand + " " + player + " " + time + " " + r.replace("-v", "").trim() + " -s");
            return true;
        }
        if (serviceContext.getStateService().getVkUrl().isEmpty() && !punishCommand.equals("/tempmute") && !punishCommand.equals("/tempmuteip")) {
            ServiceLocator.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Не удалось наказать игрока, т.к. не установлена ссылка на вк. Добавьте ссылку на вк в бан самостоятельно в формате ' | Вопросы? vk.com/id' или попробуйте перезайти на сервер.", 5f);
            return false;
        }
        serviceContext.getChatService().chatMessage(punishCommand + " " + player + " " + time + " " + r + (addVk ? " | Вопросы? " + serviceContext.getStateService().getVkUrl() + " -s" : " -s"));
        return true;
    }
}