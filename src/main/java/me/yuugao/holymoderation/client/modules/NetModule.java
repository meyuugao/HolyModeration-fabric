package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.config.Config;
import me.yuugao.holymoderation.client.config.ConfigManager;
import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.CommandSendEvent;
import me.yuugao.holymoderation.client.eventbus.event.ServerConnectEvent;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.*;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class NetModule extends Module {
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

    private Map<String, Object> journalProfile = new HashMap<>();
    private Map<String, Object> journalStats = new HashMap<>();

    public NetModule(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Subscribe
    public void onServerConnect(ServerConnectEvent event) {
        if (!event.isSwitch()) refresh();
    }

    @Subscribe
    public void onCommandSend(CommandSendEvent event) {
        NotificationsService notificationsService = serviceContext.getNotificationsService();

        String eventCommand = event.getCommand();
        String[] commandSplit = eventCommand.split(" ");
        if (!eventCommand.startsWith("hm") || commandSplit.length < 2) return;

        String command = commandSplit[1];

        switch (command) {
            case "net" -> refresh();

            case "me" -> CompletableFuture.runAsync(() -> {
                try {
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

                    notificationsService.addNotification(NotificationType.SUCCESS, "%s%sИНФОРМАЦИЯ О МОДЕРАТОРЕ"
                            .formatted(GREEN, BOLD), texts, 10f);
                } catch (Exception e) {
                    notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                            "Исключение в SettingsManager/onCommandSend: %s%S".formatted(DARK_RED, e), 5f);
                }
            });

            case "stats" -> {
                try {
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

                    notificationsService.addNotification(NotificationType.SUCCESS, "%s%sСТАТИСТИКА МОДЕРАТОРА"
                            .formatted(GREEN, BOLD), texts.toString(), 10f);
                } catch (Exception e) {
                    notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                            "Исключение в SettingsManager/onCommandSend: %s%s".formatted(DARK_RED, e), 5f);
                }
            }
        }
    }

    private void refresh() {
        NetService netService = serviceContext.getNetService();
        ConfigManager configManager = serviceContext.getConfigManager();
        NotificationsService notificationsService = serviceContext.getNotificationsService();
        StateService stateService = serviceContext.getStateService();

        CompletableFuture.runAsync(() -> {
            if (needUpdates()) return;

            netService.downloadSounds();

            if (configManager.getConfig().getApiToken().isEmpty()) {
                notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                        "У вас не установлен API токен из журнала. Чтобы продолжить работу, его необходимо установить (%s%s%s/hm setapitoken %s%sapitoken%s) и перезайти на сервер."
                                .formatted(GOLD, GOLD, BOLD, GREEN, BOLD, WHITE), 3600f);
                stateService.block();
                return;
            }

            journalProfile = netService.getJournalProfile();
            journalStats = netService.getJournalStats();
            stateService.setRank((int) Double.parseDouble(journalProfile.get("rank").toString()));
            stateService.setVkUrl("vk.com/id%s".formatted((long) Double.parseDouble(journalProfile.get("idVk").toString())));

            if (!stateService.getUserNickname().equals(journalProfile.get("nickname").toString())) {
                notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                        "Ваш никнейм не совпадает с никнеймом из журнала. Мод был заблокирован.", 3600f);
                stateService.block();
                return;
            }

            notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD), "Синхронизация завершена!", 5f);
        });
    }

    private boolean needUpdates() {
        NetService netService = serviceContext.getNetService();
        ConfigManager configManager = serviceContext.getConfigManager();
        NotificationsService notificationsService = serviceContext.getNotificationsService();
        ChatService chatService = serviceContext.getChatService();
        StateService stateService = serviceContext.getStateService();

        Config config = configManager.getConfig();
        AbstractMap.SimpleEntry<String, String> lastUpdates = netService.getLastUpdates();
        String lastVersion = lastUpdates.getKey();
        String description = lastUpdates.getValue();

        if (!config.getCurrentVersion().equals(lastVersion)) {
            notificationsService.addNotification(NotificationType.WARNING,
                    "%s%sВаша версия HolyModeration устарела. Новейшая версия: %s%s%s%s%s, ваша: %s%s%s".formatted(
                            GOLD, BOLD, DARK_GREEN, BOLD, lastVersion, GOLD, BOLD, DARK_GREEN, BOLD, config.getCurrentVersion()
                    ),
                    "%s%sОписание обновления: %s%s%s".formatted(AQUA, BOLD, LIGHT_PURPLE, BOLD, description.replace("\\n", "\n")),
                    3600f, "update.wav");

            chatService.clientMessage(chatService.openURLTextComponent(
                    "%s%sНовая версия!".formatted(GREEN, BOLD),
                    "Нажмите, чтобы перейти на страницу с новой версией мода.",
                    "https://github.com/meyuugao/HolyModeration-Releases/releases/tag/%s".formatted(lastVersion)
            ));

            stateService.block();
            return true;
        }

        return false;
    }
}