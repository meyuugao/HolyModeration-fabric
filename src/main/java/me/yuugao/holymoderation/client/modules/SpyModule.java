package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.CommandSendEvent;
import me.yuugao.holymoderation.client.eventbus.event.MessageReceiveEvent;
import me.yuugao.holymoderation.client.eventbus.event.ServerConnectEvent;
import me.yuugao.holymoderation.client.eventbus.event.ServerDisconnectEvent;
import me.yuugao.holymoderation.client.modules.drawable.DrawableModule;
import me.yuugao.holymoderation.client.modules.drawable.element.SpyDrawableElement;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.NotificationType;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

public class SpyModule extends DrawableModule<SpyDrawableElement> {
    private boolean enabled = false;
    private boolean checkingSpy = false;
    private boolean processingPlaytimeInfo = false;
    private boolean shouldUpdate = false;
    private boolean instantUpdate = false;
    private String lastKnownLocation = StringUtils.EMPTY;

    public SpyModule(ServiceContext serviceContext, SpyDrawableElement spyDrawableElement) {
        super(serviceContext, spyDrawableElement);
    }

    @Subscribe
    public void onCommandSend(CommandSendEvent event) {
        String eventCommand = event.getCommand();
        String[] commandSplit = eventCommand.split(" ", 3);
        if (!eventCommand.startsWith("hm") || commandSplit.length < 2) return;

        if (commandSplit[1].equals("spy")) {
            if (commandSplit.length == 2) {
                if (!serviceContext.getStateService().getSpyPlayer().isEmpty()) {
                    endSpy();
                } else {
                    serviceContext.getNotificationService().addNotification(NotificationType.WARNING, GOLD + BOLD + "Предупреждение", "Вы никого не отслеживаете.", 5f);
                }
                return;
            }

            if (serviceContext.getStateService().isInHub()) {
                serviceContext.getNotificationService().addNotification(NotificationType.WARNING, GOLD + BOLD + "Предупреждение", "В хабе этого делать нельзя.", 5f);
                return;
            }

            if (!serviceContext.getStateService().getPlayer().isEmpty() && serviceContext.getStateService().getPlayer().equals(commandSplit[2])) {
                serviceContext.getNotificationService().addNotification(NotificationType.WARNING, GOLD + BOLD + "Предупреждение", "Вы не можете начать следить за игроком на вашей проверке.", 5f);
                return;
            }

            if (!serviceContext.getStateService().getSpyPlayer().isEmpty()) {
                serviceContext.getNotificationService().addNotification(NotificationType.WARNING, GOLD + BOLD + "Предупреждение", "Вы уже следите за кем-то --> " + GOLD + BOLD + "/hm spy" + WHITE + RED + BOLD + ".", 5f);
                return;
            }

            startSpy(commandSplit[2]);
        } else if (commandSplit[1].equals("spyfrz")) {
            if (serviceContext.getStateService().isInHub()) {
                serviceContext.getNotificationService().addNotification(NotificationType.WARNING, GOLD + BOLD + "Предупреждение", "В хабе этого делать нельзя.", 5f);
                return;
            }

            if (serviceContext.getStateService().getSpyPlayer().isEmpty()) {
                serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы ни за кем не следите.", 5f);
                return;
            }

            if (serviceContext.getCheckoutsService().startCheckOut(serviceContext.getStateService().getSpyPlayer())) {
                endSpy();
            }
        }
    }

    @Subscribe(priority = 98)
    public void onMessageReceive(MessageReceiveEvent event) {
        String receivedText = serviceContext.getChatService().formatReceivedText(event.getMessage().getString());
        if (receivedText == null) return;

        if (checkingSpy) {
            if (receivedText.startsWith("----------")) {
                if (processingPlaytimeInfo) {
                    checkingSpy = false;
                    shouldUpdate = true;
                    processingPlaytimeInfo = false;
                } else processingPlaytimeInfo = true;
            }

            if (receivedText.startsWith("Игрок") && !receivedText.startsWith("Игрок " + serviceContext.getStateService().getUserNickname())) {
                event.setCancelled(true);
                if (receivedText.equals("Игрок оффлайн"))
                    serviceContext.getStateService().setSpyPlayerStatus("offline");
                else if (receivedText.split("сервере ")[1].startsWith("lobby"))
                    serviceContext.getStateService().setSpyPlayerStatus("lobby");
                else
                    serviceContext.getStateService().setSpyPlayerStatus(serviceContext.getChatService().formatLocation(receivedText.split("сервере ")[1]));
                serviceContext.getStateService().setSpyPlayerActivity(StringUtils.EMPTY);
                checkingSpy = false;
                shouldUpdate = true;
            }

            if (receivedText.startsWith("Текущая")) {
                String loc = receivedText.split(": ")[1];
                loc = loc.substring(1, loc.length() - 1);
                if (loc.equals("Оффлайн")) {
                    serviceContext.getStateService().setSpyPlayerStatus("offline");
                    serviceContext.getStateService().setSpyPlayerActivity(StringUtils.EMPTY);
                    instantUpdate = true;
                } else {
                    if (lastKnownLocation.isEmpty()) lastKnownLocation = loc;
                    else if (!loc.equals(lastKnownLocation)) {
                        serviceContext.getStateService().setSpyPlayerStatus(StringUtils.EMPTY);
                        lastKnownLocation = loc;
                    }
                }
            }

            if (receivedText.startsWith("Последняя") && !serviceContext.getStateService().getSpyPlayerStatus().isEmpty()) {
                serviceContext.getStateService().setSpyPlayerActivity(receivedText.split(": ")[1]);
            }

            if (receivedText.startsWith("Активность") || receivedText.startsWith("Общее время") ||
                    receivedText.startsWith("Текущая") || receivedText.startsWith("Время") ||
                    receivedText.startsWith("Последняя") || receivedText.startsWith("Последний") ||
                    receivedText.startsWith("----------") || receivedText.isEmpty()) {
                event.setCancelled(true);
            }

            if (shouldUpdate) {
                instantUpdate |= serviceContext.getStateService().getUserLocation().equals(serviceContext.getStateService().getSpyPlayerStatus())
                        && serviceContext.getStateService().getSpyPlayerActivity().isEmpty();
                serviceContext.getSchedulerService().getInstance().schedule(this::update, instantUpdate ? 500 : serviceContext.getConfigManager().getConfig().getSpyDelay(), instantUpdate ? TimeUnit.MILLISECONDS : TimeUnit.SECONDS);
                shouldUpdate = instantUpdate = false;
            }
        }
    }

    @Subscribe
    public void onServerConnect(ServerConnectEvent event) {
        if (event.isSwitch()) tryInit();
    }

    @Subscribe
    public void onServerDisconnect(ServerDisconnectEvent event) {
        if (!serviceContext.getStateService().getSpyPlayer().isEmpty()) {
            endSpy();
        }
    }

    private void tryInit() {
        serviceContext.getSchedulerService().getInstance().schedule(() -> {
            if (serviceContext.getStateService().isGameInitCompleted()) {
                if (enabled) {
                    if (serviceContext.getStateService().isInHub()) {
                        serviceContext.getStateService().setSpyPlayerActivity(lastKnownLocation = StringUtils.EMPTY);
                        serviceContext.getStateService().setSpyPlayerStatus("stop");
                        serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Слежка приостановлена.", 5f);
                    } else {
                        if (!serviceContext.getStateService().getUserLocation().isEmpty()) {
                            update();
                            serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Слежка возобновлена.", 5f);
                        } else {
                            tryInit();
                        }
                    }
                }
            } else {
                tryInit();
            }
        }, 50, TimeUnit.MILLISECONDS);
    }

    private void startSpy(String player) {
        resetSpy();
        serviceContext.getStateService().setSpyPlayer(player);
        enabled = true;
        this.drawableElement.onStartSpy();
        serviceContext.getSchedulerService().getInstance().schedule(this::update, 250, TimeUnit.MILLISECONDS);
        serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Слежка начата", 5f);
    }

    private void endSpy() {
        resetSpy();
        serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Слежка остановлена.", 5f);
    }

    private void resetSpy() {
        serviceContext.getStateService().setSpyPlayer(StringUtils.EMPTY);
        enabled = false;
        this.drawableElement.onResetSpy();
        lastKnownLocation = StringUtils.EMPTY;
        serviceContext.getStateService().setSpyPlayerActivity(StringUtils.EMPTY);
        serviceContext.getStateService().setSpyPlayerStatus(StringUtils.EMPTY);
    }

    private void update() {
        if (!enabled) return;

        checkingSpy = true;
        if (!serviceContext.getStateService().isInHub() && serviceContext.getStateService().isGameInitCompleted()) {
            if (!serviceContext.getStateService().getUserLocation().equals(serviceContext.getStateService().getSpyPlayerStatus()))
                serviceContext.getChatService().chatMessage("/find " + serviceContext.getStateService().getSpyPlayer());
            else
                serviceContext.getChatService().chatMessage("/playtime " + serviceContext.getStateService().getSpyPlayer());
        }
    }

    @Override
    public int getRenderPriority() {
        return 1001;
    }
}