package me.yuugao.holymoderation.client.modules;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.CommandSendEvent;
import me.yuugao.holymoderation.client.eventbus.event.MessageReceiveEvent;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.*;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.hash.Hashing;

public class TwinksCheckModule extends Module {
    private final Pattern STATUS_PATTERN = Pattern.compile("\\[(Активный|Истёкший)]");

    private final Path workDir = Paths.get("C:\\HolyModeration\\Twinks");
    private final File checkFile = workDir.resolve("checktwinks.txt").toFile();
    private final File tempFile = workDir.resolve("temp.txt").toFile();

    public TwinksCheckModule(ServiceContext serviceContext) {
        super(serviceContext);
        try {
            Files.createDirectory(workDir);
        } catch (IOException e) {
            serviceContext.getLoggerService().exception("Исключение в TwinksCheckModule/init: %s".formatted(e));
        }
    }

    @Subscribe
    public void onCommandSend(CommandSendEvent event) {
        StateService stateService = serviceContext.getStateService();
        NotificationsService notificationsService = serviceContext.getNotificationsService();
        ChatService chatService = serviceContext.getChatService();
        SchedulerService schedulerService = serviceContext.getSchedulerService();
        GoogleSheetsService googleSheetsService = serviceContext.getGoogleSheetsService();

        ScheduledExecutorService scheduler = schedulerService.getScheduler();

        String[] parts = event.getCommand().split(" ");
        if (parts.length < 2 || !parts[0].equals("hm") || !parts[1].equals("twinks")) return;

        if (stateService.isInHub()) {
            notificationsService.addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(GOLD, BOLD),
                    "В хабе этого делать нельзя.", 5f);
            return;
        }

        if (!checkFile.exists()) {
            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                    "Не найден файл 'checktwinks.txt' по пути '%s'.".formatted(workDir), 5f);
            return;
        }

        notificationsService.addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(GOLD, BOLD),
                "Проверка твинков началась.", 5f);

        try {
            Files.deleteIfExists(tempFile.toPath());
            Files.createFile(tempFile.toPath());
        } catch (Exception e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в TwinksCheckModule/onCommandSend: %s%s".formatted(DARK_RED, e), 5f);
            return;
        }

        List<String> nicknames = readNicknamesFromFile(notificationsService);
        if (nicknames.isEmpty()) {
            notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                    "Файл checktwinks.txt пустой.", 5f);
            return;
        }

        stateService.setCheckingTwinks(true);
        int lastIndex = nicknames.size() - 1;

        scheduler.submit(() -> {
            Set<String> blopNicknames = loadBlopNicknames(googleSheetsService, notificationsService);

            for (int i = 0; i <= lastIndex; i++) {
                String nickname = nicknames.get(i);
                boolean isInBLOP = checkIsInBLOP(nickname, blopNicknames);

                scheduler.schedule(() -> {
                    writeTempPlayer(nickname, isInBLOP);
                    chatService.chatMessage("/history %s 100".formatted(nickname));
                }, i, TimeUnit.SECONDS);

                if (i == lastIndex) {
                    scheduler.schedule(() -> {
                        stateService.setCheckingTwinks(false);
                        List<PlayerEntry> results = parseHistory();
                        saveResults(results);
                    }, i + 1, TimeUnit.SECONDS);
                }
            }
        });
    }

    private Set<String> loadBlopNicknames(GoogleSheetsService googleSheetsService, NotificationsService notificationsService) {
        Set<String> nicknames = new HashSet<>();

        GoogleSheetsService.Spreadsheet spreadsheet = googleSheetsService.getPublicSpreadsheet(
                "https://docs.google.com/spreadsheets/d/1UiUszqOVKgtIuMKfihMq-Soc9gRvXIi4CI2lVsYe5Ug");
        if (spreadsheet == null) {
            notificationsService.addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(GOLD, BOLD),
                    "Не удалось загрузить BLOP таблицу. Проверка BLOP пропущена.", 5f);
            return nicknames;
        }

        List<GoogleSheetsService.CellData> columnA = spreadsheet.getColumn("A");
        for (int i = 3; i < columnA.size(); i++) {
            String text = columnA.get(i).text().trim();
            if (text.isEmpty()) continue;

            for (String part : text.split("/")) {
                String nickname = part.trim().toLowerCase();
                if (!nickname.isEmpty()) {
                    nicknames.add(nickname);
                }
            }
        }

        return nicknames;
    }

    private boolean checkIsInBLOP(String nickname, Set<String> blopNicknames) {
        if (blopNicknames.isEmpty()) return false;
        return blopNicknames.contains(nickname.toLowerCase().trim());
    }

    @Subscribe(priority = 97)
    public void onMessageReceive(MessageReceiveEvent event) {
        StateService stateService = serviceContext.getStateService();
        if (!stateService.isCheckingTwinks()) return;

        ChatService chatService = serviceContext.getChatService();
        String message = chatService.formatReceivedText(event.getMessage().getString());
        if (message == null) return;

        if (isHistoryMessage(message)) {
            event.setCancelled(true);
            writeTempData(message);
        }
    }

    private boolean isHistoryMessage(String message) {
        return message.startsWith(" -- [") || message.startsWith("Игрок") || message.startsWith("по причина:")
                || message.startsWith("История") || message.startsWith("Окончание через")
                || message.startsWith("Разбанен:") || message.startsWith("Размьючен:") || message.trim().isEmpty();
    }

    private List<String> readNicknamesFromFile(NotificationsService notificationsService) {
        List<String> nicknames = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(Files.newInputStream(checkFile.toPath()), StandardCharsets.UTF_16LE))) {
            br.mark(1);
            if (br.read() != 0xFEFF) br.reset();

            String line = br.readLine();
            if (line != null) {
                for (String nick : line.split(" ")) {
                    String sanitized = sanitizeNickname(nick);
                    if (!sanitized.isEmpty()) {
                        nicknames.add(sanitized);
                    }
                }
            }
        } catch (Exception e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в TwinksCheckModule/readNicknamesFromFile: %s%s".formatted(DARK_RED, e), 5f);
        }
        return nicknames;
    }

    private void writeTempPlayer(String nickname, boolean isInBLOP) {
        try {
            boolean isEmpty = tempFile.length() == 0;
            String header = isEmpty
                    ? "PLAYER: " + nickname + " | BLOP: " + isInBLOP
                    : System.lineSeparator() + "PLAYER: " + nickname + " | BLOP: " + isInBLOP;
            Files.writeString(tempFile.toPath(), header, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            serviceContext.getNotificationsService().addNotification(NotificationType.EXCEPTION,
                    "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в TwinksCheckModule/writeTempPlayer: %s%s".formatted(DARK_RED, e), 5f);
        }
    }

    private void writeTempData(String message) {
        try {
            Files.writeString(tempFile.toPath(), System.lineSeparator() + "DATA: " + message,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (Exception e) {
            serviceContext.getNotificationsService().addNotification(NotificationType.EXCEPTION,
                    "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в TwinksCheckModule/writeTempData: %s%s".formatted(DARK_RED, e), 5f);
        }
    }

    private List<PlayerEntry> parseHistory() {
        List<PlayerEntry> results = new ArrayList<>();
        if (!tempFile.exists()) return results;

        try {
            List<String> lines = Files.readAllLines(tempFile.toPath());
            Files.deleteIfExists(tempFile.toPath());

            String currentNickname = null;
            boolean currentIsInBLOP = false;
            List<String> currentBlock = new ArrayList<>();

            for (String line : lines) {
                if (line.startsWith("PLAYER: ")) {
                    if (currentNickname != null && !currentBlock.isEmpty()) {
                        results.add(buildPlayerEntry(currentNickname, currentIsInBLOP, currentBlock));
                    }
                    String[] playerParts = line.substring(8).split(" \\| BLOP: ");
                    currentNickname = playerParts[0];
                    currentIsInBLOP = playerParts.length > 1 && Boolean.parseBoolean(playerParts[1]);
                    currentBlock.clear();
                } else if (line.startsWith("DATA: ")) {
                    currentBlock.add(line.substring(6));
                }
            }

            if (currentNickname != null && !currentBlock.isEmpty()) {
                results.add(buildPlayerEntry(currentNickname, currentIsInBLOP, currentBlock));
            }
        } catch (Exception e) {
            serviceContext.getNotificationsService().addNotification(NotificationType.EXCEPTION,
                    "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в TwinksCheckModule/parseHistory: %s%s".formatted(DARK_RED, e), 5f);
        }

        return results;
    }

    private PlayerEntry buildPlayerEntry(String nickname, boolean isInBLOP, List<String> lines) {
        PlayerEntry entry = new PlayerEntry(nickname);
        entry.isInBLOP = isInBLOP;

        if (isNoHistory(lines)) return entry;

        entry.historyFound = true;
        entry.isBanned = checkBanStatus(nickname, lines);
        entry.fullHistory = parsePunishments(lines, false);
        entry.recentHistory = parsePunishments(lines, true);

        return entry;
    }

    private boolean isNoHistory(List<String> lines) {
        return (lines.size() == 1 && lines.get(0).equals("История не найдена."))
                || (lines.size() == 2 && lines.get(0).startsWith("История ") && lines.get(1).isEmpty());
    }

    private boolean checkBanStatus(String nickname, List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.contains("был забанен") || !line.contains(nickname)) continue;

            Matcher matcher = STATUS_PATTERN.matcher(line);
            if (!matcher.find() && i + 1 < lines.size()) {
                matcher = STATUS_PATTERN.matcher(lines.get(i + 1));
            }
            if (matcher.find() && "Активный".equals(matcher.group(1))) {
                return true;
            }
        }
        return false;
    }

    private List<PunishmentEntry> parsePunishments(List<String> lines, boolean filter30Days) {
        List<PunishmentEntry> history = new ArrayList<>();

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.startsWith(" -- [")) continue;

            List<String> entryLines = collectEntryLines(lines, i);
            i += entryLines.size() - 1;

            PunishmentEntry entry = parsePunishmentEntry(entryLines, filter30Days);
            if (entry != null) {
                history.add(entry);
            }
        }

        return history;
    }

    private List<String> collectEntryLines(List<String> lines, int startIndex) {
        List<String> entryLines = new ArrayList<>();
        entryLines.add(lines.get(startIndex));

        for (int j = startIndex + 1; j < lines.size(); j++) {
            String next = lines.get(j);
            if (next.startsWith(" -- [") || next.startsWith("История") || next.startsWith("Окончание")
                    || next.startsWith("Разбанен:") || next.startsWith("Размьючен:")) {
                break;
            }
            entryLines.add(next);
        }

        return entryLines;
    }

    private PunishmentEntry parsePunishmentEntry(List<String> entryLines, boolean filter30Days) {
        String timeAgo = extractTimeAgo(entryLines.get(0));
        if (timeAgo == null) return null;

        if (filter30Days && !isWithin30Days(timeAgo)) return null;

        PunishmentType type = null;
        String reason = StringUtils.EMPTY;

        for (String line : entryLines) {
            if (line.startsWith("Игрок ") && line.contains(" был ")) {
                if (line.contains("был забанен")) type = PunishmentType.BAN;
                else if (line.contains("был кикнут")) type = PunishmentType.KICK;
                else if (line.contains("был замьючен")) type = PunishmentType.MUTE;
            }
            if (line.startsWith("по причина:")) {
                reason = extractReason(line);
            }
        }

        if (type == null) return null;

        String by = getAdminFromEntry(entryLines);
        boolean isActive = checkIsActive(entryLines);

        return new PunishmentEntry(type, reason, by, timeAgo, isActive);
    }

    private String extractReason(String line) {
        int first = line.indexOf('\'');
        int last = line.lastIndexOf('\'');
        if (first >= 0 && last > first) {
            return line.substring(first + 1, last);
        }
        return line.replaceFirst("^по причина:\\s*", "").trim();
    }

    private boolean checkIsActive(List<String> entryLines) {
        for (String line : entryLines) {
            Matcher matcher = STATUS_PATTERN.matcher(line);
            if (matcher.find()) {
                return "Активный".equals(matcher.group(1));
            }
        }
        return false;
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
                return line.substring(line.indexOf("игроком ") + 8).trim();
            }
            if (line.contains("модератором ")) {
                return line.substring(line.indexOf("модератором ") + 12).trim();
            }
        }
        return "Console";
    }

    private static boolean isWithin30Days(String timeAgo) {
        int days = 0, hours = 0, minutes = 0;
        String[] parts = timeAgo.split(" ");

        for (int i = 1; i < parts.length; i++) {
            try {
                int value = Integer.parseInt(parts[i - 1]);
                switch (parts[i]) {
                    case "дн." -> days = value;
                    case "ч." -> hours = value;
                    case "мин." -> minutes = value;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        return days + (hours / 24.0) + (minutes / 1440.0) <= 30;
    }

    private static String sanitizeNickname(String nick) {
        return nick == null ? StringUtils.EMPTY : nick.replaceAll("[^A-Za-z0-9_]", "");
    }

    private void saveResults(List<PlayerEntry> results) {
        try {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < results.size(); i++) {
                if (i > 0) sb.append(System.lineSeparator());
                appendPlayerEntry(sb, results.get(i));
            }

            String content = sb.toString();
            String hash = Hashing.sha256().hashString(content, StandardCharsets.UTF_8).toString();
            File resultFile = workDir.resolve(hash + ".txt").toFile();
            Files.writeString(resultFile.toPath(), content);

            serviceContext.getNotificationsService().addNotification(NotificationType.SUCCESS,
                    "%s%sУспех".formatted(GREEN, BOLD),
                    "Результат проверки твинков сохранены: %s".formatted(resultFile.getName()), 5f);
        } catch (Exception e) {
            serviceContext.getNotificationsService().addNotification(NotificationType.EXCEPTION,
                    "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в TwinksCheckModule/saveResults: %s%s".formatted(DARK_RED, e), 5f);
        }
    }

    private void appendPlayerEntry(StringBuilder sb, PlayerEntry entry) {
        sb.append("PLAYER: ").append(entry.nickname).append(System.lineSeparator());
        sb.append("BANNED: ").append(entry.isBanned).append(System.lineSeparator());
        sb.append("IN_BLOP: ").append(entry.isInBLOP).append(System.lineSeparator());
        sb.append("HISTORY_FOUND: ").append(entry.historyFound).append(System.lineSeparator());

        sb.append("RECENT_HISTORY: ").append(entry.recentHistory.size()).append(System.lineSeparator());
        for (PunishmentEntry p : entry.recentHistory) {
            sb.append("RH: ").append(formatPunishment(p)).append(System.lineSeparator());
        }

        sb.append("FULL_HISTORY: ").append(entry.fullHistory.size()).append(System.lineSeparator());
        for (PunishmentEntry p : entry.fullHistory) {
            sb.append("FH: ").append(formatPunishment(p)).append(System.lineSeparator());
        }
    }

    private String formatPunishment(PunishmentEntry p) {
        return p.type().name() + "|" + escapePipe(p.reason()) + "|" + escapePipe(p.by()) + "|"
                + escapePipe(p.timeAgo()) + "|" + p.isActive();
    }

    public List<PlayerEntry> loadResults(File file) {
        List<PlayerEntry> results = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            PlayerEntryParser parser = new PlayerEntryParser();

            for (String line : lines) {
                parser.processLine(line);
                if (parser.isComplete()) {
                    results.add(parser.build());
                    parser.reset();
                }
            }

            PlayerEntry last = parser.build();
            if (last != null) {
                results.add(last);
            }
        } catch (Exception e) {
            serviceContext.getNotificationsService().addNotification(NotificationType.EXCEPTION,
                    "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в TwinksCheckModule/loadResults: %s%s".formatted(DARK_RED, e), 5f);
        }

        return results;
    }

    private static String escapePipe(String s) {
        return s.replace("|", "\\|");
    }

    private static String unescapePipe(String s) {
        return s.replace("\\|", "|");
    }

    public static class PlayerEntry {
        public final String nickname;
        public boolean isInBLOP;
        public boolean historyFound;
        public boolean isBanned;
        public List<PunishmentEntry> recentHistory = new ArrayList<>();
        public List<PunishmentEntry> fullHistory = new ArrayList<>();

        public PlayerEntry(String nickname) {
            this.nickname = nickname;
        }
    }

    public record PunishmentEntry(PunishmentType type, String reason, String by, String timeAgo, boolean isActive) {
    }

    public enum PunishmentType {
        BAN, MUTE, KICK
    }

    private static class PlayerEntryParser {
        String nickname;
        boolean isInBLOP;
        boolean isBanned;
        boolean historyFound;
        List<PunishmentEntry> recentHistory = new ArrayList<>();
        List<PunishmentEntry> fullHistory = new ArrayList<>();
        int recentCount;
        int fullCount;
        String section;

        void processLine(String line) {
            if (line.startsWith("PLAYER: ")) {
                nickname = line.substring(8);
            } else if (line.startsWith("BANNED: ")) {
                isBanned = Boolean.parseBoolean(line.substring(8));
            } else if (line.startsWith("IN_BLOP: ")) {
                isInBLOP = Boolean.parseBoolean(line.substring(9));
            } else if (line.startsWith("HISTORY_FOUND: ")) {
                historyFound = Boolean.parseBoolean(line.substring(15));
            } else if (line.startsWith("RECENT_HISTORY: ")) {
                recentCount = Integer.parseInt(line.substring(16));
                section = "recent";
            } else if (line.startsWith("FULL_HISTORY: ")) {
                fullCount = Integer.parseInt(line.substring(14));
                section = "full";
            } else if (line.startsWith("RH: ") && "recent".equals(section) && recentHistory.size() < recentCount) {
                recentHistory.add(parsePunishment(line.substring(4)));
            } else if (line.startsWith("FH: ") && "full".equals(section) && fullHistory.size() < fullCount) {
                fullHistory.add(parsePunishment(line.substring(4)));
            }
        }

        boolean isComplete() {
            return nickname != null && fullHistory.size() == fullCount && fullCount > 0;
        }

        PlayerEntry build() {
            if (nickname == null) return null;
            PlayerEntry entry = new PlayerEntry(nickname);
            entry.isInBLOP = isInBLOP;
            entry.isBanned = isBanned;
            entry.historyFound = historyFound;
            entry.recentHistory = new ArrayList<>(recentHistory);
            entry.fullHistory = new ArrayList<>(fullHistory);
            return entry;
        }

        void reset() {
            nickname = null;
            isInBLOP = false;
            isBanned = false;
            historyFound = false;
            recentHistory.clear();
            fullHistory.clear();
            recentCount = 0;
            fullCount = 0;
            section = null;
        }

        private PunishmentEntry parsePunishment(String data) {
            String[] parts = data.split("\\|", 5);
            if (parts.length != 5) return null;
            return new PunishmentEntry(
                    PunishmentType.valueOf(parts[0]),
                    unescapePipe(parts[1]),
                    unescapePipe(parts[2]),
                    unescapePipe(parts[3]),
                    Boolean.parseBoolean(parts[4])
            );
        }
    }
}