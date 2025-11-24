package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.MessageSendEvent;
import me.yuugao.holymoderation.client.eventbus.event.ServerConnectEvent;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class NetSynchronizerModule extends Module {

    @Subscribe
    public void onServerConnect(ServerConnectEvent event) {
        if (!event.isSwitch()) refresh();
    }

    @Subscribe
    public void onMessageSend(MessageSendEvent event) {
        if (event.getContent().equals(".net")) {
            event.setCancelled(true);
            refresh();
        }
    }

    private void refresh() {
        CompletableFuture.runAsync(() -> {
            try {
                netService.downloadSounds();
            } catch (Exception e) {
                holyLogger.printException("Исключение в NetSynchronizerModule/onServerConnect: " + e);
            }

            Map<String, Object> profile = netService.getJournalProfile();
            stateService.setJournalProfile(profile);
            stateService.setJournalStats(netService.getJournalStats());
            stateService.setRank((int) Double.parseDouble(profile.get("rank").toString()));
            stateService.setVkUrl("vk.com/id" + (long) Double.parseDouble(profile.get("idVk").toString()));
            stateService.setApiInitCompleted(true);

            holyLogger.printSuccess("Синхронизация завершена!");
        });
    }
}