package me.yuugao.holymoderation.client.util.service;


import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.state.ModStateService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class PunishmentsService {
    private final ModStateService modStateService;
    private final ChatService chatService;
    private final NotificationsService notificationsService;
    private final ConfigManagerService configManagerService;

    public boolean punish(String punishCommand, String player, String reason, boolean addVk) {
        if (addVk) {
            String customVk = configManagerService.getSettingsConfig().getCustomVk();
            String vkUrl = !customVk.isBlank() ? customVk : configManagerService.getApiConfig().getVk();
            String lastUpdateOfCachedVk = configManagerService.getApiConfig().getLastVkUpdate();
            if (vkUrl.isEmpty()) {
                notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                        "Не удалось наказать игрока, т.к. не установлена ссылка на вк. Добавьте ссылку на вк в бан самостоятельно, попробуйте перезайти на сервер или установите ссылку на вк самостоятельно (%s%s%s/hm setvk %s%svk%s)"
                                .formatted(GOLD, GOLD, BOLD, GREEN, BOLD, WHITE), 5f);
                return false;
            } else if (customVk.isBlank() && !modStateService.isOnlineMode()) {
                notificationsService.addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(GOLD, BOLD),
                        "Не удалось получить ссылку на вк из журнала, поэтому была использована закэшированная ссылка на вк из конфига, последний раз обновлённая %s".formatted(lastUpdateOfCachedVk), 5f);
            }

            chatService.chatMessage("%s %s %s | Вопросы? %s -s".formatted(punishCommand, player, reason, vkUrl));
        } else {
            chatService.chatMessage("%s %s %s -s".formatted(punishCommand, player, reason));
        }

        return true;
    }

    public boolean punish(String punishCommand, String player, String time, String reason, boolean addVk) {
        if (!isTimeCorrect(time)) {
            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                    "Неверный формат времени. Должно быть 1-9999s/S, 1-9999m/M, 1-9999h/H, 1-9999d/D", 5f);
            return false;
        }

        return punish(punishCommand, player, "%s %s".formatted(time, reason), addVk);
    }

    public boolean isTimeCorrect(String time) {
        char lastChar = time.charAt(time.length() - 1);
        String stringTime = time.substring(0, time.length() - 1);
        return String.valueOf(lastChar).matches("(?i)[smhd]") && stringTime.length() <= 5 && chatService.checkCorrectInt(stringTime);
    }
}