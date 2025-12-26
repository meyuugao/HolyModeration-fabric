package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.MessageReceiveEvent;

public class ReportCopyModule extends Module {
    private boolean messageIsReportInfo = false;

    @Subscribe
    public void onMessageReceive(MessageReceiveEvent event) {
        String receivedText = serviceContext.getChatService().formatReceivedText(event.getMessage().getString());
        if (receivedText == null) {
            return;
        }

        if (receivedText.startsWith("▍ Заявитель:")) {
            messageIsReportInfo = true;
        }

        if (receivedText.contains("Подозреваемый:")) {
            serviceContext.getChatService().copyToClipboard(receivedText.split(": ")[1].split(" ")[0]);
        }

        if (messageIsReportInfo) {
            event.setCancelled(true);
        }

        if (receivedText.startsWith("▶ [ПКМ]") || receivedText.startsWith("◤          Подано")) {
            messageIsReportInfo = false;
            serviceContext.getLoggerService().printSuccess("Ник игрока скопирован.");
        }
    }
}