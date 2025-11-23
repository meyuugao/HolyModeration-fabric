package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.BOLD;
import static me.yuugao.holymoderation.client.util.Colors.RED;


import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.MessageSendEvent;
import me.yuugao.holymoderation.client.eventbus.event.ServerConnectEvent;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
            holyLogger.printSuccess("Статистика из журнала успешно обновлена!");
        }
    }

    private void refresh() {
        CompletableFuture.runAsync(() -> {
            try {
                Path soundsDir = Paths.get("C:\\HolyModeration\\Sounds");
                if (!Files.exists(soundsDir)) {
                    Files.createDirectory(soundsDir);
                }
                //tip: качать те что указаны в файле, а не заранее определены
                netService.downloadSound("success.wav");
                netService.downloadSound("error.wav");
                netService.downloadSound("exception.wav");
                netService.downloadSound("twinksDone.wav");
                netService.downloadSound("update.wav");
            } catch (Exception e) {
                holyLogger.printException(RED + BOLD + "Исключение в NetSynchronizerModule/onServerConnect: " + e);
            }

            Map<String, Object> profile = netService.getJournalProfile();
            stateService.setJournalProfile(profile);
            stateService.setJournalStats(netService.getJournalStats());
            stateService.setRank((int) Double.parseDouble(profile.get("rank").toString()));
            stateService.setVkUrl("vk.com/id" + (long) Double.parseDouble(profile.get("idVk").toString()));
            stateService.setApiInitCompleted(true);
        });
    }
}