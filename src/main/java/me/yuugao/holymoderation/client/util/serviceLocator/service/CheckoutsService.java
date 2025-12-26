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
            for (String text : new String[]{"закончитьчистый", "закончитьбанда " + serviceContext.getStateService().getPlayer(), "закончитьбаннет " + serviceContext.getStateService().getPlayer(), "закончитьавтобай", "закончитьавтоселл"}) {
                serviceContext.getChatService().clientMessage(text);
            }
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
                for (String text : new String[]{"внестирепорт", "внестипроверка", "внестиавтобай", "внестиавтоселл", "внестикандидат", "внестикастомка", "внестиперсонал", "внестимногопроверок"}) {
                    serviceContext.getChatService().clientMessage(text);
                }
            }
        }, 9, TimeUnit.SECONDS);
    }

    public void sendTexts(String player, ServiceContext serviceContext) {
        if (serviceContext.getConfigManager().getConfig().getTexts().isEmpty()) {
            serviceContext.getLoggerService().printError("У вас нет настроенных текстов для отправки. Добавить текст --> " + GOLD + GOLD + BOLD + ".textadd" + WHITE);
        } else {
            for (String text : serviceContext.getConfigManager().getConfig().getTexts().split("%%", 0)) {
                serviceContext.getChatService().chatMessage("/msg " + player + " " + text.replace("§", "&"));
            }
        }
    }
}