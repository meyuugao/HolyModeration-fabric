package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.BOLD;
import static me.yuugao.holymoderation.client.util.Colors.GREEN;


import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.MessageReceiveEvent;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.ChatService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.NotificationType;
import me.yuugao.holymoderation.client.util.serviceLocator.service.NotificationsService;

public class ReportCopyModule extends Module {
    private boolean messageIsReportInfo = false;

    public ReportCopyModule(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Subscribe(priority = 96)
    public void onMessageReceive(MessageReceiveEvent event) {
        ChatService chatService = serviceContext.getChatService();
        NotificationsService notificationsService = serviceContext.getNotificationsService();

        String receivedText = chatService.formatReceivedText(event.getMessage().getString());

        if (receivedText == null) return;

        if (receivedText.startsWith("▍ Заявитель:")) {
            messageIsReportInfo = true;
        }

        if (receivedText.contains("Подозреваемый:")) {
            String nickname = receivedText.split(": ")[1].split(" ")[0];
            chatService.copyToClipboard(nickname);
            notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                    "Ник игрока из репорта скопирован: %s".formatted(nickname), 5f);
        }

        if (messageIsReportInfo) {
            event.setCancelled(true);
        }

        if (receivedText.startsWith("▶ [ПКМ]") || receivedText.startsWith("◤          Подано")) {
            messageIsReportInfo = false;
        }
    }
}