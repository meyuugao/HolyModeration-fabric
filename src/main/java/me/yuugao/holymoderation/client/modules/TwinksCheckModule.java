package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.CommandSendEvent;
import me.yuugao.holymoderation.client.eventbus.event.MessageReceiveEvent;
import me.yuugao.holymoderation.client.util.serviceLocator.service.NotificationType;

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
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class TwinksCheckModule extends Module {
    private final Path outputDir = Paths.get("C:\\HolyModeration\\Temp");
    private final File tempFile = new File("C:\\HolyModeration\\Temp\\temp.txt");

    @Subscribe
    public void onCommandSend(CommandSendEvent event) {
        String eventCommand = event.getCommand();
        String[] commandSplit = eventCommand.split(" ");
        if (!eventCommand.startsWith("hm") || commandSplit.length < 2) return;

        if (commandSplit[1].equals("twinks")) {
            if (serviceContext.getStateService().isInHub()) {
                serviceContext.getNotificationService().addNotification(NotificationType.WARNING, GOLD + BOLD + "Предупреждение", "В хабе этого делать нельзя.", 5f);
                return;
            }

            File file = new File("C:\\HolyModeration\\checktwinks.txt");
            if (!file.exists()) {
                serviceContext.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Не найден файл 'checktwinks.txt' по пути 'C:\\HolyModeration'.", 5f);
                return;
            }

            serviceContext.getNotificationService().addNotification(NotificationType.WARNING, GOLD + BOLD + "Предупреждение", "Проверка твинков началась.", 5f);

            try {
                if (tempFile.exists()) {
                    Files.delete(tempFile.toPath());
                }

                if (!Files.exists(outputDir)) {
                    Files.createDirectory(outputDir);
                }
                Files.createFile(tempFile.toPath());
            } catch (Exception e) {
                serviceContext.getNotificationService().addNotification(NotificationType.EXCEPTION, DARK_RED + BOLD + "Исключение", "Исключение в TwinksCheckModule/onMessageSend: " + DARK_RED + e, 5f);
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_16LE))) {
                br.mark(1);
                if (br.read() != 0xFEFF) {
                    br.reset();
                }

                String[] twinks = br.readLine().split(" ");
                serviceContext.getStateService().setCheckingTwinks(true);
                loop:
                for (int i = 0; i < twinks.length; i++) {
                    String twink = twinks[i];
                    for (char ch : serviceContext.getChatService().Chars) {
                        if (twink.contains(String.valueOf(ch))) {
                            continue loop;
                        }
                    }

                    serviceContext.getSchedulerService().getInstance().schedule(() -> {
                        try {
                            String content = (tempFile.length() > 0 ? System.lineSeparator() : "") + "%%%" + twink + "%%%";
                            Files.writeString(tempFile.toPath(), content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                        } catch (Exception e) {
                            serviceContext.getNotificationService().addNotification(NotificationType.EXCEPTION, DARK_RED + BOLD + "Исключение", "Исключение в TwinksCheckModule/onMessageSend: " + DARK_RED + e, 5f);
                        }
                        serviceContext.getChatService().chatMessage("/history " + twink + " 100");
                    }, i, TimeUnit.SECONDS);
                    if (twinks.length == i + 1) {
                        serviceContext.getSchedulerService().getInstance().schedule(() -> {
                            serviceContext.getStateService().setCheckingTwinks(false);
                            parseHistory();
                        }, i + 1, TimeUnit.SECONDS);
                    }
                }
            } catch (Exception e) {
                serviceContext.getNotificationService().addNotification(NotificationType.EXCEPTION, DARK_RED + BOLD + "Исключение", "Исключение в TwinksCheckModule/onMessageSend: " + DARK_RED + e, 5f);
            }
        }
    }

    @Subscribe
    public void onMessageReceive(MessageReceiveEvent event) {
        String message = serviceContext.getChatService().formatReceivedText(event.getMessage().getString());
        if (message == null) {
            return;
        }

        if (serviceContext.getStateService().isCheckingTwinks()) {
            if (message.startsWith(" -- [") || message.startsWith("Игрок") || message.startsWith("по причина:") || message.startsWith("История") || message.startsWith("Окончание через") || message.startsWith("Разбанен:") || message.startsWith("Размьючен:") || message.trim().isEmpty()) {
                event.setCancelled(true);
                try {
                    String content = System.lineSeparator() + message;
                    Files.writeString(tempFile.toPath(), content, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                } catch (Exception e) {
                    serviceContext.getNotificationService().addNotification(NotificationType.EXCEPTION, DARK_RED + BOLD + "Исключение", "Исключение в TwinksCheckModule/onMessageReceive: " + DARK_RED + e, 5f);
                }
            }
        }
    }

    private void parseHistory() {
        try {
            List<String> lines = Files.readAllLines(tempFile.toPath());
            Files.delete(tempFile.toPath());
            List<String> currentBlock = new ArrayList<>();
            String currentFilename = StringUtils.EMPTY;

            for (String line : lines) {
                if (line.startsWith("%%%")) {
                    if (!currentFilename.isEmpty() && !currentBlock.isEmpty()) {
                        saveBlock(outputDir, currentFilename, currentBlock);
                    }

                    currentFilename = line.substring(3, line.length() - 3);
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
                                String nickname = path.getFileName().toString()
                                        .replace(".txt", "");
                                List<String> parseLines = Files.readAllLines(path);

                                String banStatus = checkBanStatus(nickname, parseLines);

                                if (parseLines.size() == 1 && parseLines.get(0).equals("История не найдена.") ||
                                        parseLines.size() == 2 && parseLines.get(0).startsWith("История ") &&
                                                parseLines.get(1).isEmpty()) {
                                    results.append(nickname).append(banStatus).append(" {\n")
                                            .append("  История не найдена\n")
                                            .append("}\n\n");
                                } else {
                                    List<String> punishments = parsePunishments(nickname, parseLines);
                                    results.append(nickname).append(banStatus).append(" {\n");
                                    if (punishments.isEmpty()) {
                                        results.append("  Нет наказаний за последние 30 дней\n");
                                    } else {
                                        punishments.forEach(p -> results.append("  ").append(p).append("\n"));
                                    }
                                    results.append("}\n\n");
                                }
                            } catch (Exception e) {
                                serviceContext.getNotificationService().addNotification(NotificationType.EXCEPTION, DARK_RED + BOLD + "Исключение", "Исключение в TwinksCheckModule/parseHistory: " + DARK_RED + e, 5f);
                            }
                        });
            }

            Path outputFile = Paths.get("C:\\HolyModeration\\results.txt");
            Files.writeString(outputFile, results.toString());
            FileUtils.deleteDirectory(outputDir.toFile());
            serviceContext.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Проверка твинков завершена, просмотрите результаты в C\\HolyModeration\\results.txt.", 5f, "twinksDone.wav");
        } catch (Exception e) {
            serviceContext.getNotificationService().addNotification(NotificationType.EXCEPTION, DARK_RED + BOLD + "Исключение", "Исключение в TwinksCheckModule/parseHistory: " + DARK_RED + e, 5f);
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
                    if (status.equals("Активный")) {
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
        Pattern pattern = Pattern.compile(
                "-- \\[(.*?) назад] --.*?" +
                        "Игрок " + Pattern.quote(nickname) + " был (\\S+).*?" +
                        "по причина:\\s*'(.*?)'",
                Pattern.DOTALL
        );

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith(" -- [")) {
                StringBuilder entry = new StringBuilder(line);
                while (i + 1 < lines.size() && !lines.get(i + 1).startsWith(" -- [") &&
                        !lines.get(i + 1).startsWith("История") &&
                        !lines.get(i + 1).startsWith("Окончание") &&
                        !lines.get(i + 1).startsWith("Разбанен:") &&
                        !lines.get(i + 1).startsWith("Размьючен:")) {
                    i++;
                    entry.append("\n").append(lines.get(i));
                }

                String block = entry.toString();
                Matcher matcher = pattern.matcher(block);
                if (matcher.find()) {
                    String timeAgo = matcher.group(1);
                    String action = matcher.group(2);
                    String reason = matcher.group(3);

                    if (action.matches("(забанен|кикнут)") && isWithin30Days(timeAgo)) {
                        String actionType = action.equals("забанен") ? "БАН" : "КИК";

                        String by = getString(block);

                        punishments.add(String.format("%s (%s) by %s - %s назад",
                                actionType, reason, by, timeAgo));
                    }
                }
            }
        }
        return punishments;
    }

    private static String getString(String block) {
        String by = "Console";
        String[] blockLines = block.split("\n");
        if (blockLines.length > 1) {
            String adminLine = blockLines[1];
            if (adminLine.contains("игроком ")) {
                by = adminLine.split("игроком ")[1].trim();
            } else if (adminLine.contains("модератором ")) {
                by = adminLine.split("модератором ")[1].trim();
            }
        }
        return by;
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
        try {
            boolean validBlock = false;
            for (String line : content) {
                if (line.startsWith("История " + filename) ||
                        line.startsWith("История не найдена.")) {
                    validBlock = true;
                    break;
                }
            }

            if (!validBlock) {
                return;
            }

            Path outputFile = outputDir.resolve(filename + ".txt");
            Files.write(outputFile, content);
        } catch (Exception e) {
            serviceContext.getNotificationService().addNotification(NotificationType.EXCEPTION, DARK_RED + BOLD + "Исключение", "Исключение в TwinksCheckModule/saveBlock: " + DARK_RED + e, 5f);
        }
    }
}