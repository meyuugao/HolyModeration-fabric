package me.yuugao.holymoderation.client.modules.impl;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.service.*;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.config.impl.ApiConfig;
import me.yuugao.holymoderation.client.util.service.eventbus.Subscribe;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.chat.CommandSendEvent;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.connection.ServerConnectEvent;
import me.yuugao.holymoderation.client.util.service.state.ModStateService;
import me.yuugao.holymoderation.client.util.service.state.UserStateService;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class NetModule {
    public static final Map<Integer, String> RANKS = new HashMap<>() {{
        put(1, "%s%sСтажёр".formatted(AQUA, BOLD));
        put(2, "%s%sМл. Сотрудник".formatted(YELLOW, BOLD));
        put(3, "%s%sСотрудник".formatted(GOLD, BOLD));
        put(4, "%s%sСотрудник+".formatted(GOLD, BOLD));
        put(5, "%s%sВед. Сотрудник".formatted(GOLD, BOLD));
        put(6, "%s%sСпектатор".formatted(GRAY, BOLD));
        put(7, "%s%sСт. Сотрудник".formatted(RED, BOLD));
        put(8, "%s%sАдмин".formatted(RED, BOLD));
        put(9, "%s%sКуратор".formatted(RED, BOLD));
    }};
    private final NotificationsService notificationsService;
    private final NetService netService;
    private final ConfigManagerService configManagerService;
    private final UserStateService userStateService;
    private final ModStateService modStateService;
    private final ChatService chatService;
    private final AsyncExecutor asyncExecutor;

    @Subscribe
    public void onServerConnect(ServerConnectEvent event) {
        if (!event.isSwitch()) refresh();
    }

    @Subscribe
    public void onCommandSend(CommandSendEvent event) {
        ApiConfig apiConfig = configManagerService.getApiConfig();

        Map<String, Object> journalProfile = apiConfig.getJournalProfile();
        Map<String, Object> journalStats = apiConfig.getJournalStats();

        String eventCommand = event.getCommand();
        String[] commandSplit = eventCommand.split(" ");
        if (!eventCommand.startsWith("hm") || commandSplit.length < 2) return;

        String command = commandSplit[1];

        switch (command) {
            case "net" -> refresh();

            case "me" -> {
                if (journalProfile.isEmpty()) {
                    if (modStateService.isOnlineMode()) {
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                                "Профиль из журнала пуст! Попробуйте обновить её (%s%s%s/hm net%s)"
                                        .formatted(GOLD, GOLD, BOLD, WHITE), 5f);
                    } else {
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                                "Профиль из журнала пуст из-за того, что вы находитесь в оффлайн-режиме.\nПопробуйте установить API-ключ заново (%s%s%s/hm setapitoken %s%sapitoken%s)"
                                        .formatted(GOLD, GOLD, BOLD, GREEN, BOLD, WHITE), 5f);
                    }
                    return;
                }

                String texts = """
                        %sВаш никнейм: %s%s%s
                        %sВаша должность: %s
                        %sВаш вк: %s%s%s (%s%s%s%s)
                        %sВаш баланс: %s%s%s
                        %sКоличество выговоров: %s%s%s
                        %sКоличество предупреждений: %s%s%s
                        %sРежим: %s%s%s""".formatted(
                        WHITE, AQUA, BOLD, journalProfile.get("nickname"),
                        WHITE, RANKS.get((int) Double.parseDouble(journalProfile.get("rank").toString())),
                        WHITE, AQUA, BOLD, journalProfile.get("fullname"), WHITE, "vk.com/id%s".formatted(
                                (long) Double.parseDouble(journalProfile.get("idVk").toString())), AQUA, BOLD,
                        WHITE, GREEN, BOLD, (int) Double.parseDouble(journalProfile.get("neponyatki").toString()),
                        WHITE, RED, BOLD, (int) Double.parseDouble(journalProfile.get("reprimands").toString()),
                        WHITE, GOLD, BOLD, (int) Double.parseDouble(journalProfile.get("warns").toString()),
                        WHITE, YELLOW, BOLD, journalProfile.get("anarchyMode")
                );

                notificationsService.addNotification(NotificationType.SUCCESS, "%s%sИНФОРМАЦИЯ О МОДЕРАТОРЕ%s"
                        .formatted(GREEN, BOLD, modStateService.isOnlineMode() ? "" : " (%s%sЗАКЭШИРОВАННАЯ %s%s%s)"
                                .formatted(RED, BOLD, apiConfig.getLastJournalProfileUpdate(), GREEN, BOLD)), texts, 10f);
            }

            case "stats" -> {
                if (journalStats.isEmpty()) {
                    if (modStateService.isOnlineMode()) {
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                                "Статистика из журнала пуста! Попробуйте обновить её (%s%s%s/hm net%s)"
                                        .formatted(GOLD, GOLD, BOLD, WHITE), 5f);
                    } else {
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                                "Статистика из журнала пуста из-за того, что вы находитесь в оффлайн-режиме.\nПопробуйте установить API-ключ заново (%s%s%s/hm setapitoken %s%sapitoken%s)"
                                        .formatted(GOLD, GOLD, BOLD, GREEN, BOLD, WHITE), 5f);
                    }
                    return;
                }

                @SuppressWarnings("unchecked")
                Map<String, Map<String, Object>> typedJournalStats = (Map<String, Map<String, Object>>) (Map<?, ?>) journalStats;

                Map<String, Object> revisesAll = typedJournalStats.get("revisesAll");
                Map<String, Object> revisesMonth = typedJournalStats.get("revisesMonth");
                Map<String, Object> revisesWeek = typedJournalStats.get("revisesWeek");
                Map<String, Object> revisesToday = typedJournalStats.get("revisesToday");

                StringBuilder texts = new StringBuilder();

                if (revisesAll != null && revisesMonth != null && revisesWeek != null && revisesToday != null) {
                    texts.append("""
                            %s%sСТАТИСТИКА ПРОВЕРОК
                            %sПроверок за всё время: %s%s%s (лайт: %s, лайт 1.20: %s, классик: %s)
                            %sПроверок за последний месяц: %s%s%s (лайт: %s, лайт 1.20: %s, классик: %s)
                            %sПроверок за последнюю неделю: %s%s%s (лайт: %s, лайт 1.20: %s, классик: %s)
                            %sПроверок за сегодня: %s%s%s (лайт: %s, лайт 1.20: %s, классик: %s)
                            """.formatted(
                            LIGHT_PURPLE, BOLD,
                            WHITE, AQUA, BOLD, (int) Double.parseDouble(revisesAll.get("total").toString()),
                            (int) Double.parseDouble(revisesAll.get("lite").toString()),
                            (int) Double.parseDouble(revisesAll.get("lite120").toString()),
                            (int) Double.parseDouble(revisesAll.get("classic").toString()),
                            WHITE, AQUA, BOLD, (int) Double.parseDouble(revisesMonth.get("total").toString()),
                            (int) Double.parseDouble(revisesMonth.get("lite").toString()),
                            (int) Double.parseDouble(revisesMonth.get("lite120").toString()),
                            (int) Double.parseDouble(revisesMonth.get("classic").toString()),
                            WHITE, AQUA, BOLD, (int) Double.parseDouble(revisesWeek.get("total").toString()),
                            (int) Double.parseDouble(revisesWeek.get("lite").toString()),
                            (int) Double.parseDouble(revisesWeek.get("lite120").toString()),
                            (int) Double.parseDouble(revisesWeek.get("classic").toString()),
                            WHITE, AQUA, BOLD, (int) Double.parseDouble(revisesToday.get("total").toString()),
                            (int) Double.parseDouble(revisesToday.get("lite").toString()),
                            (int) Double.parseDouble(revisesToday.get("lite120").toString()),
                            (int) Double.parseDouble(revisesToday.get("classic").toString())
                    ));
                }

                texts.append("""
                        %s%sСТАТИСТИКА МУТОВ И ГАРАНТОВ
                        %sМутов за всё время: %s%s%s
                        %sМутов за последний месяц: %s%s%s
                        %sМутов за сегодня: %s%s%s
                        %sГарантов за всё время: %s%s%s
                        %sГарантов за последний месяц: %s%s%s
                        %sГарантов за сегодня: %s%s%s
                        """.formatted(
                        LIGHT_PURPLE, BOLD,
                        WHITE, AQUA, BOLD, (int) Double.parseDouble(journalStats.get("mutesAll").toString()),
                        WHITE, AQUA, BOLD, (int) Double.parseDouble(journalStats.get("mutesMonth").toString()),
                        WHITE, AQUA, BOLD, (int) Double.parseDouble(journalStats.get("mutesToday").toString()),
                        WHITE, AQUA, BOLD, (int) Double.parseDouble(journalStats.get("gaurantsAll").toString()),
                        WHITE, AQUA, BOLD, (int) Double.parseDouble(journalStats.get("gaurantsMonth").toString()),
                        WHITE, AQUA, BOLD, (int) Double.parseDouble(journalStats.get("gaurantsToday").toString())
                ));

                notificationsService.addNotification(NotificationType.SUCCESS, "%s%sСТАТИСТИКА МОДЕРАТОРА%s"
                        .formatted(GREEN, BOLD, modStateService.isOnlineMode() ? "" : " (%s%sЗАКЭШИРОВАННАЯ %s%s%s)"
                                .formatted(RED, BOLD, apiConfig.getLastJournalStatsUpdate(), GREEN, BOLD)), texts.toString(), 10f);
            }
        }
    }

    private void refresh() {
        ApiConfig apiConfig = configManagerService.getApiConfig();

        asyncExecutor.runAsync("NetModule/refresh", () -> {
            if (needUpdates()) return;

            netService.downloadSounds();

            if (configManagerService.getApiConfig().getApiToken().isEmpty()) {
                modStateService.setOnlineMode(false);
                notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                        "У вас не установлен API-ключ из журнала. Чтобы продолжить работу в онлайн-режиме, его необходимо установить (%s%s%s/hm setapitoken %s%sapitoken%s) и перезайти на сервер."
                                .formatted(GOLD, GOLD, BOLD, GREEN, BOLD, WHITE), 15f);
                return;
            }

            Map<String, Object> journalProfile = netService.getJournalProfile();
            Map<String, Object> journalStats = netService.getJournalStats();
            if (journalProfile.equals(Collections.emptyMap()) || journalStats.equals(Collections.emptyMap())) {
                modStateService.setOnlineMode(false);
                notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                        "У вас установлен некорректный API-ключ. Проверьте корректность введённых данных и попробуйте снова (%s%s%s/hm setapitoken %s%sapitoken%s)."
                                .formatted(GOLD, GOLD, BOLD, GREEN, BOLD, WHITE), 15f);
                return;
            } else {
                modStateService.setOnlineMode(true);

                String vk = "vk.com/id%s".formatted((long) Double.parseDouble(journalProfile.get("idVk").toString()));
                apiConfig.setVk(vk);
                apiConfig.setJournalProfile(journalProfile);
                apiConfig.setJournalStats(journalStats);
                configManagerService.saveConfig(apiConfig);
            }

            if (!userStateService.getUserNickname().equals(journalProfile.get("nickname").toString())) {
                notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                        "Ваш никнейм не совпадает с никнеймом из журнала. Мод был заблокирован. Пожалуйста, используйте свой API-ключ.", 3600f);
                modStateService.block();
                return;
            }

            notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD), "Синхронизация завершена!", 5f);
        });
    }

    private boolean needUpdates() {
        ApiConfig apiConfig = configManagerService.getApiConfig();
        AbstractMap.SimpleEntry<String, String> lastUpdates = netService.getLastUpdates();
        String lastVersion = lastUpdates.getKey();
        String description = lastUpdates.getValue();

        if (!apiConfig.getCurrentVersion().equals(lastVersion)) {
            notificationsService.addNotification(NotificationType.WARNING,
                    "%s%sВаша версия HolyModeration устарела. Новейшая версия: %s%s%s%s%s, ваша: %s%s%s".formatted(
                            GOLD, BOLD, DARK_GREEN, BOLD, lastVersion, GOLD, BOLD, DARK_GREEN, BOLD, apiConfig.getCurrentVersion()
                    ),
                    "%s%sОписание обновления: %s%s%s".formatted(AQUA, BOLD, LIGHT_PURPLE, BOLD, description.replace("\\n", "\n")),
                    3600f, "update.wav");

            chatService.clientMessage(chatService.openURLTextComponent(
                    "%s%sНовая версия!".formatted(GREEN, BOLD),
                    "Нажмите, чтобы перейти на страницу с новой версией мода.",
                    "https://github.com/meyuugao/HolyModeration-Releases/releases/tag/%s".formatted(lastVersion)
            ));

            modStateService.block();
            return true;
        }

        return false;
    }
}