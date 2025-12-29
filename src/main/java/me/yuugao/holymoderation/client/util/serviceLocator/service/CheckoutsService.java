package me.yuugao.holymoderation.client.util.serviceLocator.service;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.TimeUnit;

public class CheckoutsService extends Service {
    public void endCheckOut(ServiceContext serviceContext) {
        if (!serviceContext.getStateService().getPlayer().isEmpty()) {
            serviceContext.getChatService().chatMessage("/freezing " + serviceContext.getStateService().getPlayer());
            serviceContext.getChatService().chatMessage("/prova");
            if (serviceContext.getConfigManager().getConfig().isAutoVanishEnabled() && !serviceContext.getStateService().isVanishEnabled()) {
                serviceContext.getChatService().chatMessage("/v");
                serviceContext.getStateService().setVanishEnabled(true);
            }
            if (serviceContext.getConfigManager().getConfig().isAutoGm3Enabled() && !serviceContext.getStateService().isGm3Enabled()) {
                serviceContext.getChatService().chatMessage("/gm 3");
                serviceContext.getStateService().setGm3Enabled(true);
            }
        }

        serviceContext.getSchedulerService().getInstance().schedule(() -> {
            serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Закончить проверку с результатом 'чистый'", "Нажмите, чтобы закончить проверку с результатом 'чистый'", "/endcheckout clean"));
            serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Закончить проверку с результатом 'бан' + снести стеш", "Нажмите, чтобы закончить проверку с результатом 'бан' + снести стеш", "/endcheckout ban " + serviceContext.getStateService().getPlayer() + " true"));
            serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Закончить проверку с результатом 'бан' + не сносить стеш", "Нажмите, чтобы закончить проверку с результатом 'бан' + не сносить стеш", "/endcheckout ban " + serviceContext.getStateService().getPlayer() + " false"));
            serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Закончить проверку с результатом 'автобай'", "Нажмите, чтобы закончить проверку с результатом 'автобай'", "/endcheckout autobuy"));
            serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Закончить проверку с результатом 'автоселл'", "Нажмите, чтобы закончить проверку с результатом 'автоселл'", "/endcheckout autosell"));
            serviceContext.getStateService().setPlayer(StringUtils.EMPTY);
        }, 1, TimeUnit.SECONDS);
    }

    public void startCheckOut(String player, ServiceContext serviceContext) {
        serviceContext.getStateService().setPlayer(player);

        serviceContext.getChatService().chatMessage("/freezing " + serviceContext.getStateService().getPlayer());
        if (serviceContext.getConfigManager().getConfig().isAutoTpEnabled()) {
            serviceContext.getChatService().chatMessage("/warp logo");
        }
        serviceContext.getChatService().chatMessage("/prova");

        serviceContext.getSchedulerService().getInstance().schedule(() -> {
            if (!serviceContext.getStateService().getPlayer().isEmpty()) {
                sendTexts(serviceContext.getStateService().getPlayer(), serviceContext);
            }
        }, 5, TimeUnit.SECONDS);

        serviceContext.getSchedulerService().getInstance().schedule(() -> {
            if (!serviceContext.getStateService().getPlayer().isEmpty()) {
                if (serviceContext.getConfigManager().getConfig().isDupeIpEnabled()) {
                    serviceContext.getChatService().chatMessage("/dupeip " + serviceContext.getStateService().getPlayer());
                }
                serviceContext.getChatService().chatMessage("/checkmute " + serviceContext.getStateService().getPlayer());
                if (serviceContext.getConfigManager().getConfig().isAutoVanishEnabled() && serviceContext.getStateService().isVanishEnabled()) {
                    serviceContext.getChatService().chatMessage("/v");
                    serviceContext.getStateService().setVanishEnabled(false);
                }
                if (serviceContext.getConfigManager().getConfig().isAutoGm3Enabled() && serviceContext.getStateService().isGm3Enabled()) {
                    serviceContext.getChatService().chatMessage("/gm 0");
                    serviceContext.getStateService().setGm3Enabled(false);
                }
            }
        }, 8, TimeUnit.SECONDS);

        serviceContext.getSchedulerService().getInstance().schedule(() -> {
            if (!serviceContext.getStateService().getPlayer().isEmpty()) {
                serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести проверку по репорту", "Нажмите, чтобы внести проверку по репорту", "/startcheckout " + serviceContext.getStateService().getPlayer() + " report"));
                serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести обычную проверку", "Нажмите, чтобы внести обычную проверку", "/startcheckout " + serviceContext.getStateService().getPlayer() + " checkout"));
                serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести проверку автобаера", "Нажмите, чтобы внести проверку автобаера", "/startcheckout " + serviceContext.getStateService().getPlayer() + " autobuy"));
                serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести проверку автобаера", "Нажмите, чтобы внести проверку автобаера", "/startcheckout " + serviceContext.getStateService().getPlayer() + " autobuy"));
                serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести проверку автоселлера", "Нажмите, чтобы внести проверку автоселлера", "/startcheckout " + serviceContext.getStateService().getPlayer() + " autosell"));
                serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести проверку кандидата", "Нажмите, чтобы внести проверку кандидата", "/startcheckout " + serviceContext.getStateService().getPlayer() + " candidate"));
                serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести проверку кастомки", "Нажмите, чтобы внести проверку кастомки", "/startcheckout " + serviceContext.getStateService().getPlayer() + " customka"));
                serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести проверку персонала", "Нажмите, чтобы внести проверку персонала", "/startcheckout " + serviceContext.getStateService().getPlayer() + " personal"));
                serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести проверку игрока, у которого много пройденных проверок", "Нажмите, чтобы внести проверку игрока, у которого много пройденных проверок", "/startcheckout " + serviceContext.getStateService().getPlayer() + " toManyChecks"));
            }
        }, 9, TimeUnit.SECONDS);
    }

    public void sendTexts(String player, ServiceContext serviceContext) {
        if (serviceContext.getConfigManager().getConfig().getTexts().isEmpty()) {
            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "У вас нет настроенных текстов для отправки. Добавить текст --> " + GOLD + GOLD + BOLD + "/hm textadd" + WHITE, 5f);
        } else {
            for (String text : serviceContext.getConfigManager().getConfig().getTexts().split("%%", 0)) {
                serviceContext.getChatService().chatMessage("/msg " + player + " " + text.replace("§", "&"));
            }
        }
    }
}