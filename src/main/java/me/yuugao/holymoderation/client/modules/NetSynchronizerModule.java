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
        if (!event.isSwitch() && serviceContext.getStateService().isOnHW()) refresh();
    }

    @Subscribe
    public void onMessageSend(MessageSendEvent event) {
        if (event.getContent().equals(".net")) {
            event.setCancelled(true);
            refresh();
        }
    }

    private void refresh() {
        if (serviceContext.getConfigManager().getConfig().getApiToken().isEmpty()) {
            serviceContext.getLoggerService().printError("У вас не установлен API токен из журнала. Чтобы продолжить работу, его необходимо установить (" + GOLD + GOLD + BOLD + ".setapitoken " + GREEN + BOLD + "apitoken" + WHITE + RED + BOLD + ") и перезайти на сервер.");
            return;
        }

        CompletableFuture.runAsync(() -> {
            try {
                serviceContext.getNetService().downloadSounds();
            } catch (Exception e) {
                serviceContext.getLoggerService().printException("Исключение в NetSynchronizerModule/onServerConnect: " + e);
            }

            Map<String, Object> profile = serviceContext.getNetService().getJournalProfile();
            serviceContext.getStateService().setJournalProfile(profile);
            serviceContext.getStateService().setJournalStats(serviceContext.getNetService().getJournalStats());
            serviceContext.getStateService().setRank((int) Double.parseDouble(profile.get("rank").toString()));
            serviceContext.getStateService().setVkUrl("vk.com/id" + (long) Double.parseDouble(profile.get("idVk").toString()));
            serviceContext.getStateService().setApiInitCompleted(true);

            serviceContext.getLoggerService().printSuccess("Синхронизация завершена!");
        });
    }
}