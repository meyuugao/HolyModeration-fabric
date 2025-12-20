package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.MessageSendEvent;
import me.yuugao.holymoderation.client.eventbus.event.ServerConnectEvent;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class NetSynchronizerModule extends Module {

    @Subscribe
    public void onServerConnect(ServerConnectEvent event) {
        if (!event.isSwitch() && stateService.isOnHW()) refresh();
    }

    @Subscribe
    public void onMessageSend(MessageSendEvent event) {
        if (event.getContent().equals(".net")) {
            event.setCancelled(true);
            refresh();
        }
    }

    private void refresh() {
        if (configManager.getConfig().getApiToken().isEmpty()) {
            loggerService.printError("У вас не установлен API токен из журнала. Чтобы продолжить работу, его необходимо установить (" + GOLD + GOLD + BOLD + ".setapitoken " + GREEN + BOLD + "apitoken" + WHITE + RED + BOLD + ") и перезайти на сервер.");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                netService.downloadSounds();
            } catch (Exception e) {
                loggerService.printException("Исключение в NetSynchronizerModule/onServerConnect: " + e);
            }

            Map<String, Object> profile = netService.getJournalProfile();
            stateService.setJournalProfile(profile);
            stateService.setJournalStats(netService.getJournalStats());
            stateService.setRank((int) Double.parseDouble(profile.get("rank").toString()));
            stateService.setVkUrl("vk.com/id" + (long) Double.parseDouble(profile.get("idVk").toString()));
            stateService.setApiInitCompleted(true);

            loggerService.printSuccess("Синхронизация завершена!");
        });
    }
}