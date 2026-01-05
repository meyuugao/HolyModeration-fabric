package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.CommandSendEvent;
import me.yuugao.holymoderation.client.eventbus.event.ServerConnectEvent;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.NotificationType;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class NetModule extends Module {
    public static final Map<Integer, String> RANKS = new HashMap<>() {
        {
            put(1, AQUA + BOLD + "Стажёр");
            put(2, YELLOW + BOLD + "Мл. Сотрудник");
            put(3, GOLD + BOLD + "Сотрудник");
            put(4, GOLD + BOLD + "Сотрудник+");
            put(5, GOLD + BOLD + "Вед. Сотрудник");
            put(6, WHITE + BOLD + "Спектатор");
            put(7, RED + BOLD + "Ст. Сотрудник");
            put(8, RED + BOLD + "Админ");
            put(9, RED + BOLD + "Куратор");
        }
    };

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
        String eventCommand = event.getCommand();
        String[] commandSplit = eventCommand.split(" ");
        if (!eventCommand.startsWith("hm") || commandSplit.length < 2) return;

        String command = commandSplit[1];
        
        switch (command) {
            case ("net"): {
                refresh();
                break;
            }
            case ("me"): {
                CompletableFuture.runAsync(() -> {
                    try {
                        String texts = WHITE + "Ваш никнейм: " + AQUA + BOLD + journalProfile.get("nickname").toString() +
                                "\n" +
                                WHITE + "Ваша должность: " + RANKS.get((int) Double.parseDouble(journalProfile.get("rank").toString())) +
                                "\n" +
                                WHITE + "Ваш вк: " + AQUA + BOLD + journalProfile.get("fullname").toString() +
                                " (" + WHITE + "vk.com/id" + (long) Double.parseDouble(journalProfile.get("idVk").toString()) + AQUA + BOLD + ")" +
                                "\n" +
                                WHITE + "Ваш баланс: " + GREEN + BOLD + (int) Double.parseDouble(journalProfile.get("neponyatki").toString()) +
                                "\n" +
                                WHITE + "Количество выговоров: " + RED + BOLD + (int) Double.parseDouble(journalProfile.get("reprimands").toString()) +
                                "\n" +
                                WHITE + "Количество предупреждений: " + GOLD + BOLD + (int) Double.parseDouble(journalProfile.get("warns").toString()) +
                                "\n" +
                                WHITE + "Режим: " + YELLOW + BOLD + journalProfile.get("anarchyMode");

                        serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "ИНФОРМАЦИЯ О МОДЕРАТОРЕ", texts, 10f);
                    } catch (Exception e) {
                        serviceContext.getNotificationService().addNotification(NotificationType.EXCEPTION, DARK_RED + BOLD + "Исключение", "Исключение в SettingsManager/onMessageSend: " + DARK_RED + e, 5f);
                    }
                });
                break;
            }
            case ("stats"): {
                try {
                    Map<String, Object> revisesAll = (Map<String, Object>) journalStats.get("revisesAll");
                    Map<String, Object> revisesMonth = (Map<String, Object>) journalStats.get("revisesMonth");
                    Map<String, Object> revisesWeek = (Map<String, Object>) journalStats.get("revisesWeek");
                    Map<String, Object> revisesToday = (Map<String, Object>) journalStats.get("revisesToday");

                    String texts = "";

                    if (revisesAll != null && revisesMonth != null && revisesWeek != null && revisesToday != null) {
                        texts =
                                LIGHT_PURPLE + BOLD + "СТАТИСТИКА ПРОВЕРОК" +
                                        "\n" +
                                        WHITE + "Проверок за всё время: " + AQUA + BOLD + (int) Double.parseDouble(revisesAll.get("total").toString()) +
                                        " (лайт: " + (int) Double.parseDouble(revisesAll.get("lite").toString()) +
                                        ", лайт 1.20: " + (int) Double.parseDouble(revisesAll.get("lite120").toString()) +
                                        ", классик: " + (int) Double.parseDouble(revisesAll.get("classic").toString()) + ")" +
                                        "\n" +
                                        WHITE + "Проверок за последний месяц: " + AQUA + BOLD + (int) Double.parseDouble(revisesMonth.get("total").toString()) +
                                        " (лайт: " + (int) Double.parseDouble(revisesMonth.get("lite").toString()) +
                                        ", лайт 1.20: " + (int) Double.parseDouble(revisesMonth.get("lite120").toString()) +
                                        ", классик: " + (int) Double.parseDouble(revisesMonth.get("classic").toString()) + ")" +
                                        "\n" +
                                        WHITE + "Проверок за последнюю неделю: " + AQUA + BOLD + (int) Double.parseDouble(revisesWeek.get("total").toString()) +
                                        " (лайт: " + (int) Double.parseDouble(revisesWeek.get("lite").toString()) +
                                        ", лайт 1.20: " + (int) Double.parseDouble(revisesWeek.get("lite120").toString()) +
                                        ", классик: " + (int) Double.parseDouble(revisesWeek.get("classic").toString()) + ")" +
                                        "\n" +
                                        WHITE + "Проверок за сегодня: " + AQUA + BOLD + (int) Double.parseDouble(revisesToday.get("total").toString()) +
                                        " (лайт: " + (int) Double.parseDouble(revisesToday.get("lite").toString()) +
                                        ", лайт 1.20: " + (int) Double.parseDouble(revisesToday.get("lite120").toString()) +
                                        ", классик: " + (int) Double.parseDouble(revisesToday.get("classic").toString()) + ")" +
                                        "\n";
                    }

                    texts +=
                            LIGHT_PURPLE + BOLD + "СТАТИСТИКА МУТОВ И ГАРАНТОВ" +
                                    "\n" +
                                    WHITE + "Мутов за всё время: " + AQUA + BOLD + (int) Double.parseDouble(journalStats.get("mutesAll").toString()) +
                                    "\n" +
                                    WHITE + "Мутов за последний месяц: " + AQUA + BOLD + (int) Double.parseDouble(journalStats.get("mutesMonth").toString()) +
                                    "\n" +
                                    WHITE + "Мутов за сегодня: " + AQUA + BOLD + (int) Double.parseDouble(journalStats.get("mutesToday").toString()) +
                                    "\n" +
                                    WHITE + "Гарантов за всё время: " + AQUA + BOLD + (int) Double.parseDouble(journalStats.get("gaurantsAll").toString()) +
                                    "\n" +
                                    WHITE + "Гарантов за последний месяц: " + AQUA + BOLD + (int) Double.parseDouble(journalStats.get("gaurantsMonth").toString()) +
                                    "\n" +
                                    WHITE + "Гарантов за сегодня: " + AQUA + BOLD + (int) Double.parseDouble(journalStats.get("gaurantsToday").toString());

                    serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "СТАТИСТИКА МОДЕРАТОРА", texts, 10f
                    );
                } catch (Exception e) {
                    serviceContext.getNotificationService().addNotification(NotificationType.EXCEPTION, DARK_AQUA + BOLD + "Исключение", "Исключение в SettingsManager/onMessageSend: " + DARK_RED + e, 5f);
                }
                break;
            }
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

            journalProfile = serviceContext.getNetService().getJournalProfile();
            journalStats = serviceContext.getNetService().getJournalStats();
            serviceContext.getStateService().setRank((int) Double.parseDouble(journalProfile.get("rank").toString()));
            serviceContext.getStateService().setVkUrl("vk.com/id" + (long) Double.parseDouble(journalProfile.get("idVk").toString()));

            if (!serviceContext.getStateService().getUserNickname().equals(journalProfile.get("nickname").toString())) {
                serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка",
                        "Ваш никнейм не совпадает с никнеймом из журнала. Мод был заблокирован.", 3600f);
                serviceContext.getStateService().block();
                return;
            }

            serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Синхронизация завершена!", 5f);
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