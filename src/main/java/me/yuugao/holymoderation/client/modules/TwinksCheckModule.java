package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;

import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.CommandSendEvent;
import me.yuugao.holymoderation.client.eventbus.event.MessageReceiveEvent;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.*;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TwinksCheckModule extends Module {
    private final Path outputDir = Paths.get("C:\\HolyModeration\\Temp");
    private final File tempFile = new File("C:\\HolyModeration\\Temp\\temp.txt");

    public TwinksCheckModule(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Subscribe
    public void onCommandSend(CommandSendEvent event) {
        StateService stateService = serviceContext.getStateService();
        NotificationsService notificationsService = serviceContext.getNotificationsService();
        ChatService chatService = serviceContext.getChatService();
        SchedulerService schedulerService = serviceContext.getSchedulerService();

        ScheduledExecutorService scheduler = schedulerService.getInstance();

        String eventCommand = event.getCommand();
        String[] commandSplit = eventCommand.split(" ");
        if (!eventCommand.startsWith("hm") || commandSplit.length < 2) return;

        if (commandSplit[1].equals("twinks")) {
            if (stateService.isInHub()) {
                notificationsService.addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(GOLD, BOLD),
                        "В хабе этого делать нельзя.", 5f);
                return;
            }

            File file = new File("C:\\HolyModeration\\checktwinks.txt");
            if (!file.exists()) {
                notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                        "Не найден файл 'checktwinks.txt' по пути 'C:\\HolyModeration'.", 5f);
                return;
            }

            notificationsService.addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(GOLD, BOLD),
                    "Проверка твинков началась.", 5f);

            try {
                if (tempFile.exists()) {
                    Files.delete(tempFile.toPath());
                }

                if (!Files.exists(outputDir)) {
                    Files.createDirectory(outputDir);
                }
                Files.createFile(tempFile.toPath());
            } catch (Exception e) {
                notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                        "Исключение в TwinksCheckModule/onMessageSend: %s%s".formatted(DARK_RED, e), 5f);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_16LE))) {
                br.mark(1);
                if (br.read() != 0xFEFF) {
                    br.reset();
                }

                String line = br.readLine();
                if (line == null) {
                    notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                            "Файл checktwinks.txt пустой.", 5f);
                    return;
                }
                String[] twinks = line.split(" ");
                stateService.setCheckingTwinks(true);

                for (int i = 0; i < twinks.length; i++) {
                    String original = twinks[i];
                    String sanitized = sanitizeNickname(original);
                    if (sanitized.isEmpty()) {
                        continue;
                    }

                    scheduler.schedule(() -> {
                        try {
                            String prefix = tempFile.length() > 0 ? System.lineSeparator() : StringUtils.EMPTY;
                            String header = prefix + "%%%" + sanitized + "%%%";
                            Files.writeString(tempFile.toPath(), header, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        } catch (Exception e) {
                            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                                    "Исключение в TwinksCheckModule/onMessageSend: %s%s".formatted(DARK_RED, e), 5f);
                        }
                        chatService.chatMessage("/history %s 100".formatted(sanitized));
                    }, i, TimeUnit.SECONDS);

                    if (twinks.length == i + 1) {
                        scheduler.schedule(() -> {
                            stateService.setCheckingTwinks(false);
                            parseHistory();
                        }, i + 1, TimeUnit.SECONDS);
                    }
                }
            } catch (Exception e) {
                notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                        "Исключение в TwinksCheckModule/onMessageSend: %s%s".formatted(DARK_RED, e), 5f);
            }
        }
    }

    @Subscribe(priority = 97)
    public void onMessageReceive(MessageReceiveEvent event) {
        ChatService chatService = serviceContext.getChatService();
        StateService stateService = serviceContext.getStateService();
        NotificationsService notificationsService = serviceContext.getNotificationsService();

        String message = chatService.formatReceivedText(event.getMessage().getString());
        if (message == null) {
            return;
        }

        if (stateService.isCheckingTwinks()) {
            if (message.startsWith(" -- [") || message.startsWith("Игрок") || message.startsWith("по причина:")
                    || message.startsWith("История") || message.startsWith("Окончание через")
                    || message.startsWith("Разбанен:") || message.startsWith("Размьючен:") || message.trim().isEmpty()) {
                event.setCancelled(true);
                try {
                    String content = System.lineSeparator() + message;
                    Files.writeString(tempFile.toPath(), content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                } catch (Exception e) {
                    notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                            "Исключение в TwinksCheckModule/onMessageReceive: %s%s".formatted(DARK_RED, e), 5f);
                }
            }
        }
    }

    private void parseHistory() {
        NotificationsService notificationsService = serviceContext.getNotificationsService();

        try {
            if (!tempFile.exists()) {
                notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                        "Временный файл не найден.", 5f);
                return;
            }

            List<String> lines = Files.readAllLines(tempFile.toPath());
            Files.delete(tempFile.toPath());

            List<String> currentBlock = new ArrayList<>();
            String currentFilename = StringUtils.EMPTY;

            for (String line : lines) {
                if (line.startsWith("%%%") && line.endsWith("%%%")) {
                    if (!currentFilename.isEmpty() && !currentBlock.isEmpty()) {
                        saveBlock(outputDir, currentFilename, currentBlock);
                    }
                    String raw = line.replaceAll("^%{3}|%{3}$", "");
                    currentFilename = sanitizeNickname(raw);
                    currentBlock = new ArrayList<>();
                } else if (!currentFilename.isEmpty()) {
                    currentBlock.add(line);
                }
            }

            if (!currentFilename.isEmpty() && !currentBlock.isEmpty()) {
                saveBlock(outputDir, currentFilename, currentBlock);
            }

            StringBuilder results = new StringBuilder();
            try (Stream<Path> stream = Files.list(outputDir)) {
                stream.filter(path -> path.toString().endsWith(".txt"))
                        .forEach(path -> {
                            try {
                                String filename = path.getFileName().toString().replace(".txt", StringUtils.EMPTY);
                                List<String> parseLines = Files.readAllLines(path);

                                String banStatus = checkBanStatus(filename, parseLines);

                                boolean noHistory =
                                        (parseLines.size() == 1 && parseLines.get(0).equals("История не найдена.")) ||
                                                (parseLines.size() == 2 && parseLines.get(0).startsWith("История ") && parseLines.get(1).isEmpty());

                                if (noHistory) {
                                    results.append(filename).append(banStatus).append(" {\n")
                                            .append("  История не найдена\n")
                                            .append("}\n\n");
                                } else {
                                    List<String> punishments = parsePunishments(filename, parseLines);
                                    results.append(filename).append(banStatus).append(" {\n");
                                    if (punishments.isEmpty()) {
                                        results.append("  Нет наказаний за последние 30 дней\n");
                                    } else {
                                        punishments.forEach(p -> results.append("  ").append(p).append("\n"));
                                    }
                                    results.append("}\n\n");
                                }
                            } catch (Exception e) {
                                notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение"
                                        .formatted(DARK_RED, BOLD), "Исключение в TwinksCheckModule/parseHistory: %s%s"
                                        .formatted(DARK_RED, e), 5f);
                            }
                        });
            }

            Path outputFile = Paths.get("C:\\HolyModeration\\results.txt");
            Files.writeString(outputFile, results.toString());
            FileUtils.deleteDirectory(outputDir.toFile());
            notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                    "Проверка твинков завершена, просмотрите результаты в C\\HolyModeration\\results.txt.",
                    5f, "twinksDone.wav");
        } catch (Exception e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в TwinksCheckModule/parseHistory: %s%s".formatted(DARK_RED, e), 5f);
        }
    }

    private static String checkBanStatus(String nickname, List<String> lines) {
        Pattern pattern = Pattern.compile("\\[(Активный|Истёкший)]");
        boolean isBanned = false;

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.contains("был забанен") && line.contains(nickname)) {
                Matcher matcher = pattern.matcher(line);
                if (!matcher.find() && i + 1 < lines.size()) {
                    matcher = pattern.matcher(lines.get(i + 1));
                }
                if (matcher.find()) {
                    String status = matcher.group(1);
                    if ("Активный".equals(status)) {
                        isBanned = true;
                        break;
                    }
                }
            }
        }

        return isBanned ? " - забанен" : " - не забанен";
    }

    private static List<String> parsePunishments(String nickname, List<String> lines) {
        List<String> punishments = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith(" -- [")) {
                List<String> entryLines = new ArrayList<>();
                entryLines.add(line);
                int j = i;
                while (j + 1 < lines.size()) {
                    String next = lines.get(j + 1);
                    if (next.startsWith(" -- [") || next.startsWith("История") || next.startsWith("Окончание") ||
                            next.startsWith("Разбанен:") || next.startsWith("Размьючен:")) {
                        break;
                    }
                    j++;
                    entryLines.add(next);
                }
                i = j;

                String timeAgo = extractTimeAgo(entryLines.get(0));
                String action = null;
                String reason = null;
                String by = getAdminFromEntry(entryLines);

                for (String el : entryLines) {
                    if (el.startsWith("Игрок ") && el.contains(" был ")) {
                        if (el.contains("был забанен")) action = "забанен";
                        else if (el.contains("был кикнут")) action = "кикнут";
                    }
                    if (el.startsWith("по причина:")) {
                        int first = el.indexOf('\'');
                        int last = el.lastIndexOf('\'');
                        if (first >= 0 && last > first) {
                            reason = el.substring(first + 1, last);
                        } else {
                            reason = el.replaceFirst("^по причина:\\s*", "").trim();
                        }
                    }
                }

                if (action != null && (action.equals("забанен") || action.equals("кикнут")) && timeAgo != null && isWithin30Days(timeAgo)) {
                    String actionType = action.equals("забанен") ? "БАН" : "КИК";
                    if (reason == null) reason = StringUtils.EMPTY;
                    punishments.add(String.format("%s (%s) by %s - %s назад",
                            actionType, reason, by, timeAgo));
                }
            }
        }

        return punishments;
    }

    private static String extractTimeAgo(String headerLine) {
        int start = headerLine.indexOf('[');
        int end = headerLine.indexOf(" назад");
        if (start >= 0 && end > start) {
            return headerLine.substring(start + 1, end).trim();
        }
        return null;
    }

    private static String getAdminFromEntry(List<String> entryLines) {
        for (String line : entryLines) {
            if (line.contains("игроком ")) {
                int idx = line.indexOf("игроком ");
                return line.substring(idx + "игроком ".length()).trim();
            } else if (line.contains("модератором ")) {
                int idx = line.indexOf("модератором ");
                return line.substring(idx + "модератором ".length()).trim();
            }
        }
        return "Console";
    }

    private static boolean isWithin30Days(String timeAgo) {
        int days = 0;
        int hours = 0;
        int minutes = 0;

        String[] parts = timeAgo.split(" ");
        for (int i = 0; i < parts.length; i++) {
            if (parts[i].equals("дн.") && i > 0) {
                try {
                    days = Integer.parseInt(parts[i - 1]);
                } catch (NumberFormatException e) {
                    days = 0;
                }
            } else if (parts[i].equals("ч.") && i > 0) {
                try {
                    hours = Integer.parseInt(parts[i - 1]);
                } catch (NumberFormatException e) {
                    hours = 0;
                }
            } else if (parts[i].equals("мин.") && i > 0) {
                try {
                    minutes = Integer.parseInt(parts[i - 1]);
                } catch (NumberFormatException e) {
                    minutes = 0;
                }
            }
        }

        double totalDays = days + (hours / 24.0) + (minutes / (24.0 * 60.0));
        return totalDays <= 30;
    }

    private void saveBlock(Path outputDir, String filename, List<String> content) {
        NotificationsService notificationsService = serviceContext.getNotificationsService();

        try {
            boolean validBlock = false;
            for (String line : content) {
                if (line.startsWith("История " + filename) || line.startsWith("История не найдена.")) {
                    validBlock = true;
                    break;
                }
            }

            if (!validBlock) {
                return;
            }

            if (filename.isEmpty()) {
                return;
            }

            Path outputFile = outputDir.resolve("%s.txt".formatted(filename));
            Files.write(outputFile, content);
        } catch (Exception e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в TwinksCheckModule/saveBlock: %s%s".formatted(DARK_RED, e), 5f);
        }
    }

    private static String sanitizeNickname(String nick) {
        if (nick == null) return StringUtils.EMPTY;
        return nick.replaceAll("[^A-Za-z0-9_]", "");
    }
}