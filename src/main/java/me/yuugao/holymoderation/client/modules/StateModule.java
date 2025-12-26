package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.HolyModerationClient;
import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.MessageReceiveEvent;
import me.yuugao.holymoderation.client.eventbus.event.MessageSendEvent;
import me.yuugao.holymoderation.client.eventbus.event.ServerConnectEvent;
import me.yuugao.holymoderation.client.eventbus.event.ServerDisconnectEvent;

import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import org.apache.commons.lang3.StringUtils;

public class StateModule extends Module {
    private boolean blocked = false;
    private boolean enabled = true;

    @Subscribe(priority = 100)
    public void onServerConnect(ServerConnectEvent event) {
        if (!event.isSwitch()) {
            if (serviceContext.getMinecraftService().getPlayer() != null) {
                serviceContext.getStateService().setModerNickname(serviceContext.getMinecraftService().getPlayer().getName().getString());
            }

            serviceContext.getStateService().setConnected(true);
            checkServerAddress(event);

            boolean shouldBlock = !serviceContext.getStateService().isOnHW() || needUpdate() || !enabled;
            if (!blocked && shouldBlock) {
                block();
            } else if (blocked && !shouldBlock) {
                unblock();
                serviceContext.getEventBus().invokeEvent(event);
            }
        }

        serviceContext.getStateService().setGameInitCompleted(false);

        if (serviceContext.getMinecraftService().getClient().interactionManager != null && serviceContext.getMinecraftService().getClient().interactionManager.getCurrentGameMode().equals(GameMode.ADVENTURE)) {
            serviceContext.getStateService().setInHub(true);
            serviceContext.getStateService().setModerLocation(StringUtils.EMPTY);
        } else {
            serviceContext.getStateService().setInHub(false);
            serviceContext.getChatService().chatMessage("/find " + serviceContext.getStateService().getModerNickname());
        }

        serviceContext.getStateService().setGameInitCompleted(true);
    }

    @Subscribe(priority = 100)
    public void onServerDisconnect(ServerDisconnectEvent event) {
        if (serviceContext.getStateService().isConnected()) {
            serviceContext.getStateService().reset();
        }
    }

    @Subscribe(priority = 100)
    public void onMessageSend(MessageSendEvent event) {
        if (!serviceContext.getStateService().isOnHW()) return;

        if (!serviceContext.getStateService().isGameInitCompleted()) {
            event.setCancelled(true);
            serviceContext.getLoggerService().printError("Не спеши, инициализация игры ещё не завершилась!");
            return;
        }

        String[] messageSplit = event.getContent().split(" ", 2);
        switch (messageSplit[0]) {
            case (".setapitoken"): {
                event.setCancelled(true);
                if (messageSplit.length == 1) {
                    serviceContext.getLoggerService().printError("Вы не ввели токен.");
                    return;
                }
                String apiToken = messageSplit[1];
                if (apiToken.contains(" ")) {
                    serviceContext.getLoggerService().printError("В API токене обнаружены пробелы, пожалуйста, указывайте его без пробелов.");
                    return;
                }
                serviceContext.getConfigManager().getConfig().setApiToken(apiToken);
                serviceContext.getConfigManager().saveCfg(serviceContext.getConfigManager().getConfig());
                serviceContext.getSoundService().playSound("success.wav", 70);

                if (serviceContext.getMinecraftService().getClient().getNetworkHandler() != null) {
                    serviceContext.getMinecraftService().getClient().getNetworkHandler().getConnection().disconnect(Text.of(AQUA + BOLD + "Вы успешно установили API токен. Пожалуйста, перезайдите на сервер."));
                }
                break;
            }

            case (".enable"): {
                event.setCancelled(true);
                enabled = true;
                unblock();
                serviceContext.getLoggerService().printSuccess("Мод включен!");
                break;
            }

            case (".disable"): {
                event.setCancelled(true);
                enabled = false;
                block();
                serviceContext.getLoggerService().printSuccess(RED + BOLD + "Мод выключен!");
                break;
            }
        }
    }

    @Subscribe(priority = 100)
    public void onMessageReceive(MessageReceiveEvent event) {
        String receivedText = serviceContext.getChatService().formatReceivedText(event.getMessage().getString());
        if (receivedText == null) {
            return;
        }

        if (receivedText.equals("▶ Ожидайте завершения проверки... Пожалуйста, не двигайтесь.") || receivedText.equals("▶ Введите цифры с картинки в чат! Для открытия чата, нажмите <T>")) {
            serviceContext.getStateService().setInHub(true);
            serviceContext.getStateService().setGameInitCompleted(true);
            serviceContext.getStateService().setModerLocation(StringUtils.EMPTY);
        }

        if (serviceContext.getStateService().getModerLocation().isEmpty()) {
            if (receivedText.startsWith("Игрок " + serviceContext.getStateService().getModerNickname())) {
                event.setCancelled(true);
                serviceContext.getStateService().setModerLocation(serviceContext.getChatService().formatLocation(receivedText.split("сервере ")[1])); //tip: определяет но всё равно в чат пишет
            }
        }
    }

    private void checkServerAddress(ServerConnectEvent event) {
        serviceContext.getStateService().setOnHW(event.getServerInfo().address.matches("(?i).*hol(l)?yworld.*"));
    }

    private boolean needUpdate() {
        String lastVersion = serviceContext.getNetService().getLastUpdates().getKey();
        String description = serviceContext.getNetService().getLastUpdates().getValue();

        if (!serviceContext.getConfigManager().getConfig().getCurrentVersion().equals(lastVersion)) {
            serviceContext.getChatService().clientMessage(RED + BOLD + "Ваша версия HolyModeration устарела. Новейшая версия: " + DARK_GREEN + BOLD + lastVersion + RED + BOLD + ", ваша: " + DARK_GREEN + BOLD + serviceContext.getConfigManager().getConfig().getCurrentVersion());
            serviceContext.getChatService().clientMessage(AQUA + BOLD + "Описание обновления: " + LIGHT_PURPLE + BOLD + description.replace("\\n", "\n"));
            serviceContext.getChatService().clientMessage(serviceContext.getChatService().openURLTextComponent(
                    GREEN + BOLD + "ССЫЛКА НА НОВУЮ ВЕРСИЮ",
                    "Нажмите, чтобы перейти на страницу с новой версией мода.",
                    "https://github.com/meyuugao/HolyModeration-Releases/releases/tag/" + lastVersion));
            serviceContext.getSoundService().playSound("update.wav", 70);
            return true;
        }

        return false;
    }

    private void block() {
        blocked = true;
        serviceContext.getEventBus().clear();
        serviceContext.getEventBus().register(this);
    }

    private void unblock() {
        blocked = false;
        HolyModerationClient.registerEventListeners(serviceContext.getEventBus());
    }
}