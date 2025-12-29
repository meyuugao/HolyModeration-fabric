package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.CommandSendEvent;
import me.yuugao.holymoderation.client.eventbus.event.ServerConnectEvent;
import me.yuugao.holymoderation.client.util.serviceLocator.service.NotificationType;

import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class NetSynchronizerModule extends Module {
    @Subscribe
    public void onServerConnect(ServerConnectEvent event) {
        if (!event.isSwitch()) refresh();
    }

    @Subscribe
    public void onCommandSend(CommandSendEvent event) {
        String eventCommand = event.getCommand();
        String[] commandSplit = eventCommand.split(" ");
        if (!eventCommand.startsWith("hm") || commandSplit.length < 2) return;

        if (eventCommand.split(" ")[1].equals("net")) {
            refresh();
        }
    }

    private void refresh() {
        CompletableFuture.runAsync(() -> {
            if (needUpdates()) return;

            serviceContext.getNetService().downloadSounds();

            if (serviceContext.getConfigManager().getConfig().getApiToken().isEmpty()) {
                serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка",
                        "У вас не установлен API токен из журнала. Чтобы продолжить работу, его необходимо установить ("
                                + GOLD + GOLD + BOLD + "/hm setapitoken " + GREEN + BOLD + "apitoken" + WHITE + ") и перезайти на сервер.", 3600f);
                serviceContext.getStateService().block();
                return;
            }

            Map<String, Object> profile = serviceContext.getNetService().getJournalProfile();
            serviceContext.getStateService().setJournalProfile(profile);
            serviceContext.getStateService().setJournalStats(serviceContext.getNetService().getJournalStats());
            serviceContext.getStateService().setRank((int) Double.parseDouble(profile.get("rank").toString()));
            serviceContext.getStateService().setVkUrl("vk.com/id" + (long) Double.parseDouble(profile.get("idVk").toString()));

            serviceContext.getNotificationService().addNotification(
                    NotificationType.SUCCESS,
                    GREEN + BOLD + "Успех",
                    "Синхронизация завершена!",
                    5f
            );
        });
    }

    private boolean needUpdates() {
        AbstractMap.SimpleEntry<String, String> lastUpdates = serviceContext.getNetService().getLastUpdates();
        String lastVersion = lastUpdates.getKey();
        String description = lastUpdates.getValue();

        if (!serviceContext.getConfigManager().getConfig().getCurrentVersion().equals(lastVersion)) {
            serviceContext.getNotificationService().addNotification(NotificationType.WARNING, GOLD + BOLD +
                            "Ваша версия HolyModeration устарела. Новейшая версия: " + DARK_GREEN + BOLD + lastVersion +
                            GOLD + BOLD + ", ваша: " + DARK_GREEN + BOLD + serviceContext.getConfigManager().getConfig().getCurrentVersion(),
                    AQUA + BOLD + "Описание обновления: " + LIGHT_PURPLE + BOLD + description.replace("\\n", "\n"), 3600f, "update.wav");

            serviceContext.getChatService().clientMessage(serviceContext.getChatService().openURLTextComponent(
                    GREEN + BOLD + "Новая версия!",
                    "Нажмите, чтобы перейти на страницу с новой версией мода.",
                    "https://github.com/meyuugao/HolyModeration-Releases/releases/tag/" + lastVersion));

            serviceContext.getStateService().block();
            return true;
        }

        return false;
    }
}