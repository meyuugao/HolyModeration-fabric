package me.yuugao.holymoderation.client.util.serviceLocator.service;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

public class PunishmentsService extends Service {
    public void punish(String punishCommand, String player, String reason, boolean addVk, ServiceContext serviceContext) {
        if (reason.contains(" | Вопросы")) {
            serviceContext.getChatService().chatMessage(punishCommand + " " + player + " " + reason + " -s");
            return;
        }
        if (serviceContext.getStateService().getVkUrl().isEmpty() && !punishCommand.equals("/tempmute") && !punishCommand.equals("/tempmuteip")) {
            serviceContext.getLoggerService().printError("Не удалось наказать игрока, т.к. не установлена ссылка на вк. Добавьте ссылку на вк в бан самостоятельно в формате ' | Вопросы? vk.com/id' или попробуйте перезайти на сервер.");
            return;
        }
        serviceContext.getChatService().chatMessage(punishCommand + " " + player + " " + reason + (addVk ? " | Вопросы? " + serviceContext.getStateService().getVkUrl() + " -s" : " -s"));
    }

    public boolean punish(String punishCommand, String player, String time, String reason, boolean addVk, ServiceContext serviceContext) {
        char lastChar = time.charAt(time.length() - 1);
        String stringTime = time.substring(0, time.length() - 1);
        if (!String.valueOf(lastChar).matches("(?i)[smhd]") || time.length() > 5 || !serviceContext.getChatService().checkCorrectInt(stringTime)) {
            serviceContext.getLoggerService().printError("Неверный формат времени. Должно быть 1-9999s/S, 1-9999m/M, 1-9999h/H, 1-9999d/D");
            return false;
        }

        if (reason.contains(" | Вопросы")) {
            serviceContext.getChatService().chatMessage(punishCommand + " " + player + " " + time + " " + reason + " -s");
            return true;
        }
        if (serviceContext.getStateService().getVkUrl().isEmpty() && !punishCommand.equals("/tempmute") && !punishCommand.equals("/tempmuteip")) {
            serviceContext.getLoggerService().printError("Не удалось наказать игрока, т.к. не установлена ссылка на вк. Добавьте ссылку на вк в бан самостоятельно в формате ' | Вопросы? vk.com/id' или попробуйте перезайти на сервер.");
            return false;
        }
        serviceContext.getChatService().chatMessage(punishCommand + " " + player + " " + time + " " + reason + (addVk ? " | Вопросы? " + serviceContext.getStateService().getVkUrl() + " -s" : " -s"));
        return true;
    }
}