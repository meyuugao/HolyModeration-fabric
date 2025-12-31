package me.yuugao.holymoderation.client.util.serviceLocator.service;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
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

        ServiceLocator.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Вы успешно закончили проверку.", 5f);

        String player = new String(serviceContext.getStateService().getPlayer().toCharArray());

        serviceContext.getSchedulerService().getInstance().schedule(() -> {
            serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Закончить проверку с результатом 'чистый'", "Нажмите, чтобы закончить проверку с результатом 'чистый'", "/hm endcheckout clean"));
            serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Закончить проверку с результатом 'бан' + снести стеш", "Нажмите, чтобы закончить проверку с результатом 'бан' + снести стеш", "/hm endcheckout ban " + player + " true"));
            serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Закончить проверку с результатом 'бан' + не сносить стеш", "Нажмите, чтобы закончить проверку с результатом 'бан' + не сносить стеш", "/hm endcheckout ban " + player + " false"));
            serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Закончить проверку с результатом 'автобай'", "Нажмите, чтобы закончить проверку с результатом 'автобай'", "/hm endcheckout autobuy"));
            serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Закончить проверку с результатом 'автоселл'", "Нажмите, чтобы закончить проверку с результатом 'автоселл'", "/hm endcheckout autosell"));
        }, 1, TimeUnit.SECONDS);

        serviceContext.getStateService().setPlayer(StringUtils.EMPTY);
    }

    public boolean startCheckOut(String player, ServiceContext serviceContext) {
        if (!serviceContext.getStateService().getPlayer().isEmpty()) {
            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Вы уже проверяете какого-то игрока. Сначала закончите текущую проверку --> " + GOLD + GOLD + BOLD + "/unfreezing" + WHITE + " или " + GOLD + GOLD + BOLD + "/unfrz" + WHITE, 5f);
            return false;
        }

        ServiceLocator.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Вы успешно начали проверку.", 5f);

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
                serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести проверку по репорту", "Нажмите, чтобы внести проверку по репорту", "/hm startcheckout " + serviceContext.getStateService().getPlayer() + " report"));
                serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести обычную проверку", "Нажмите, чтобы внести обычную проверку", "/hm startcheckout " + serviceContext.getStateService().getPlayer() + " checkout"));
                serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести проверку автобаера", "Нажмите, чтобы внести проверку автобаера", "/hm startcheckout " + serviceContext.getStateService().getPlayer() + " autobuy"));
                serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести проверку автобаера", "Нажмите, чтобы внести проверку автобаера", "/hm startcheckout " + serviceContext.getStateService().getPlayer() + " autobuy"));
                serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести проверку автоселлера", "Нажмите, чтобы внести проверку автоселлера", "/hm startcheckout " + serviceContext.getStateService().getPlayer() + " autosell"));
                serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести проверку кандидата", "Нажмите, чтобы внести проверку кандидата", "/hm startcheckout " + serviceContext.getStateService().getPlayer() + " candidate"));
                serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести проверку кастомки", "Нажмите, чтобы внести проверку кастомки", "/hm startcheckout " + serviceContext.getStateService().getPlayer() + " customka"));
                serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести проверку персонала", "Нажмите, чтобы внести проверку персонала", "/hm startcheckout " + serviceContext.getStateService().getPlayer() + " personal"));
                serviceContext.getChatService().clientMessage(serviceContext.getChatService().suggestTextComponent(AQUA + BOLD + "Внести проверку игрока, у которого много пройденных проверок", "Нажмите, чтобы внести проверку игрока, у которого много пройденных проверок", "/hm startcheckout " + serviceContext.getStateService().getPlayer() + " toManyChecks"));
            }
        }, 9, TimeUnit.SECONDS);

        return true;
    }

    public void sendTexts(String player, ServiceContext serviceContext) {
        List<String> textsList = serviceContext.getConfigManager().getConfig().getTextsList();
        if (textsList.isEmpty()) {
            serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "У вас нет настроенных текстов для отправки. Добавить текст --> " + GOLD + GOLD + BOLD + "/hm textadd" + WHITE, 5f);
        } else {
            for (int i = 0; i < textsList.size(); i++) {
                String text = textsList.get(i);
                serviceContext.getSchedulerService().getInstance().schedule(() -> serviceContext.getChatService().chatMessage("/msg " + player + " " + text.replace("§", "&")), i * 100L, TimeUnit.MILLISECONDS);
            }
        }
    }
}