package me.yuugao.holymoderation.client.modules.impl;

import static me.yuugao.holymoderation.client.util.Colors.BOLD;
import static me.yuugao.holymoderation.client.util.Colors.DARK_RED;


import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.PostConstruct;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.command.CommandContext;
import me.yuugao.holymoderation.client.util.command.CommandProvider;
import me.yuugao.holymoderation.client.util.command.CommandRegistry;
import me.yuugao.holymoderation.client.util.command.CommandSpec;
import me.yuugao.holymoderation.client.util.service.*;
import me.yuugao.holymoderation.client.util.service.eventbus.Subscribe;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.chat.MessageReceiveEvent;
import me.yuugao.holymoderation.client.util.service.state.UserStateService;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

import com.google.common.hash.Hashing;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class TwinksCheckModule implements CommandProvider {
    private static final Path WORK_DIR = Paths.get(System.getProperty("user.home"), "HolyModeration", "Twinks");
    private static final File CHECK_FILE = WORK_DIR.resolve("checktwinks.txt").toFile();
    private static final File TEMP_FILE = WORK_DIR.resolve("temp.txt").toFile();
    private static final double HOURS_PER_DAY = 24.0;
    private static final double MINUTES_PER_DAY = 1440.0;
    private static final int RECENT_DAYS_WINDOW = 30;
    private final UserStateService userStateService;
    private final NotificationsService notificationsService;
    private final ChatService chatService;
    private final SchedulerService schedulerService;
    private final GoogleSheetsService googleSheetsService;

    private boolean checkingTwinks = false;

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

        return days + (hours / HOURS_PER_DAY) + (minutes / MINUTES_PER_DAY) <= RECENT_DAYS_WINDOW;
    }

    private static String sanitizeNickname(String nick) {
        return nick == null ? StringUtils.EMPTY : nick.replaceAll("[^A-Za-z0-9_]", "");
    }

    private static String escapePipe(String s) {
        return s.replace("|", "\\|");
    }

    private static String unescapePipe(String s) {
        return s.replace("\\|", "|");
    }

    private static String sanitizeReason(String reason) {
        if (reason == null) return StringUtils.EMPTY;
        String r = reason;
        int q = r.indexOf("Вопросы?");
        if (q >= 0) r = r.substring(0, q);
        r = r.replace("\\|", "|").replace('|', ' ');
        r = r.replaceAll("\\s+", " ").trim();
        return r;
    }

    @PostConstruct
    private void init() {
        try {
            if (!Files.exists(WORK_DIR)) {
                Files.createDirectories(WORK_DIR);
            }
        } catch (IOException e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в TwinksCheckModule/init: %s%s".formatted(DARK_RED, e), 5f);
        }
    }

    @Override
    public void registerCommands(CommandRegistry registry) {
        registry.register(CommandSpec.of("twinks").group("Твинки").description("проверить твинки из checktwinks.txt").handler(this::cmdTwinks));
    }

    public static Path getWorkDir() {
        return WORK_DIR;
    }

    public static List<PlayerEntry> parseResultsFile(Path path) {
        List<PlayerEntry> results = new ArrayList<>();
        if (path == null || !Files.exists(path)) return results;
        try {
            List<String> lines = Files.readAllLines(path);
            PlayerEntryParser parser = new PlayerEntryParser();
            for (String line : lines) {
                if (line.startsWith("PLAYER: ") && parser.nickname != null) {
                    PlayerEntry entry = parser.build();
                    if (entry != null) results.add(entry);
                    parser.reset();
                }
                parser.processLine(line);
            }
            PlayerEntry entry = parser.build();
            if (entry != null) results.add(entry);
        } catch (IOException ignored) {
        }
        return results;
    }

    private void cmdTwinks(CommandContext ctx) {
        runCheck();
    }

    public void runCheck() {
        if (checkingTwinks) {
            notificationsService.error("Дождитесь окончания проверки твинков.");
            return;
        }

        if (userStateService.isInHub()) {
            notificationsService.warning("В хабе этого делать нельзя.");
            return;
        }

        if (!CHECK_FILE.exists()) {
            notificationsService.error("Не найден файл 'checktwinks.txt' по пути '%s'.".formatted(WORK_DIR));
            return;
        }

        notificationsService.warning("Проверка твинков началась.");

        List<String> nicknames = readNicknamesFromFile();
        if (nicknames.isEmpty()) {
            notificationsService.error("Файл checktwinks.txt пустой.");
            return;
        }

        try {
            Files.deleteIfExists(TEMP_FILE.toPath());
            Files.createFile(TEMP_FILE.toPath());
        } catch (IOException e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в TwinksCheckModule/cmdTwinks: %s%s".formatted(DARK_RED, e), 5f);
        }

        checkingTwinks = true;
        int lastIndex = nicknames.size() - 1;

        schedulerService.submit("TwinksCheckModule/cmdTwinks", () -> {
            Set<String> blopNicknames = loadBlopNicknames();

            for (int i = 0; i <= lastIndex; i++) {
                String nickname = nicknames.get(i);
                boolean isInBLOP = checkIsInBLOP(nickname, blopNicknames);

                schedulerService.schedule("TwinksCheckModule/cmdTwinks", () -> {
                    writeTempPlayer(nickname, isInBLOP);
                    chatService.chatMessage("/history %s 100".formatted(nickname));
                }, i, TimeUnit.SECONDS);

                if (i == lastIndex) {
                    schedulerService.schedule("TwinksCheckModule/cmdTwinks", () -> {
                        checkingTwinks = false;
                        List<PlayerEntry> results = parseHistory();
                        saveResults(results);
                    }, i + 1, TimeUnit.SECONDS);
                }
            }
        });
    }

    private Set<String> loadBlopNicknames() {
        Set<String> nicknames = new HashSet<>();

        GoogleSheetsService.Spreadsheet spreadsheet = googleSheetsService.getPublicSpreadsheet(
                "https://docs.google.com/spreadsheets/d/1UiUszqOVKgtIuMKfihMq-Soc9gRvXIi4CI2lVsYe5Ug");
        if (spreadsheet == null) {
            notificationsService.warning("Не удалось загрузить BLOP таблицу. Проверка BLOP пропущена.");
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
        if (!checkingTwinks) return;

        String message = chatService.formatReceivedText(event.getMessage().getString());
        if (message == null) return;

        if (isHistoryMessage(message)) {
            event.setCancelled(true);
            writeTempData(message);
        }
    }

    private boolean isHistoryMessage(String message) {
        return HolyWorldPatterns.isHistoryMessage(message);
    }

    private List<String> readNicknamesFromFile() {
        List<String> nicknames = new ArrayList<>();

        for (Charset charset : List.of(StandardCharsets.UTF_8, StandardCharsets.UTF_16LE)) {
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(Files.newInputStream(CHECK_FILE.toPath()), charset))) {

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

                return nicknames;
            } catch (IOException e) {
            }
        }

        notificationsService.addNotification(
                NotificationType.EXCEPTION,
                "%s%sИсключение".formatted(DARK_RED, BOLD),
                "Не удалось прочитать файл ни как UTF-8, ни как UTF-16LE",
                5f
        );

        return nicknames;
    }

    private void writeTempPlayer(String nickname, boolean isInBLOP) {
        try {
            boolean isEmpty = TEMP_FILE.length() == 0;
            String header = isEmpty
                    ? "PLAYER: " + nickname + " | BLOP: " + isInBLOP
                    : System.lineSeparator() + "PLAYER: " + nickname + " | BLOP: " + isInBLOP;
            Files.writeString(TEMP_FILE.toPath(), header, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            notificationsService.addNotification(NotificationType.EXCEPTION,
                    "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в TwinksCheckModule/writeTempPlayer: %s%s".formatted(DARK_RED, e), 5f);
        }
    }

    private void writeTempData(String message) {
        try {
            Files.writeString(TEMP_FILE.toPath(), System.lineSeparator() + "DATA: " + message,
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            notificationsService.addNotification(NotificationType.EXCEPTION,
                    "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в TwinksCheckModule/writeTempData: %s%s".formatted(DARK_RED, e), 5f);
        }
    }

    private List<PlayerEntry> parseHistory() {
        List<PlayerEntry> results = new ArrayList<>();
        if (!TEMP_FILE.exists()) return results;

        try {
            List<String> lines = Files.readAllLines(TEMP_FILE.toPath());
            Files.deleteIfExists(TEMP_FILE.toPath());

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
        } catch (IOException e) {
            notificationsService.addNotification(NotificationType.EXCEPTION,
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
        entry.isMuted = entry.fullHistory.stream().anyMatch(p -> p.type() == PunishmentType.MUTE && p.isActive());
        if (!entry.isBanned) {
            entry.isBanned = entry.fullHistory.stream().anyMatch(p -> p.type() == PunishmentType.BAN && p.isActive());
        }

        return entry;
    }

    private boolean isNoHistory(List<String> lines) {
        return HolyWorldPatterns.isNoHistory(lines);
    }

    private boolean checkBanStatus(String nickname, List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!line.contains(HolyWorldPatterns.HISTORY_BAN_MARK) || !line.contains(nickname)) continue;

            Matcher matcher = HolyWorldPatterns.BAN_STATUS_PATTERN.matcher(line);
            if (!matcher.find() && i + 1 < lines.size()) {
                matcher = HolyWorldPatterns.BAN_STATUS_PATTERN.matcher(lines.get(i + 1));
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
            if (!line.startsWith(HolyWorldPatterns.HISTORY_MARKER)) continue;

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
            if (HolyWorldPatterns.isHistoryBlockBoundary(next)) {
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
            HolyWorldPatterns.PunishmentKind kind = HolyWorldPatterns.classifyPunishment(line);
            if (kind != null) {
                type = switch (kind) {
                    case BAN -> PunishmentType.BAN;
                    case KICK -> PunishmentType.KICK;
                    case MUTE -> PunishmentType.MUTE;
                };
            }
            if (line.startsWith(HolyWorldPatterns.HISTORY_REASON_PREFIX)) {
                reason = sanitizeReason(HolyWorldPatterns.extractHistoryReason(line));
            }
        }

        if (type == null) return null;

        String by = getAdminFromEntry(entryLines);
        boolean isActive = checkIsActive(entryLines);

        return new PunishmentEntry(type, reason, by, timeAgo, isActive);
    }

    private boolean checkIsActive(List<String> entryLines) {
        for (String line : entryLines) {
            if (HolyWorldPatterns.isBanStatusActive(line)) {
                return true;
            }
        }
        return false;
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
            File resultFile = WORK_DIR.resolve(hash + ".txt").toFile();
            Files.writeString(resultFile.toPath(), content);

            notificationsService.success("Результат проверки твинков сохранены: %s".formatted(resultFile.getName()));
        } catch (IOException e) {
            notificationsService.addNotification(NotificationType.EXCEPTION,
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

    public enum PunishmentType {
        BAN, MUTE, KICK
    }

    public static class PlayerEntry {
        public final String nickname;
        public boolean isInBLOP;
        public boolean historyFound;
        public boolean isBanned;
        public boolean isMuted;
        public List<PunishmentEntry> recentHistory = new ArrayList<>();
        public List<PunishmentEntry> fullHistory = new ArrayList<>();

        public PlayerEntry(String nickname) {
            this.nickname = nickname;
        }
    }

    public record PunishmentEntry(PunishmentType type, String reason, String by, String timeAgo, boolean isActive) {
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
            entry.isMuted = entry.fullHistory.stream().anyMatch(p -> p.type() == PunishmentType.MUTE && p.isActive());
            if (!entry.isBanned) {
                entry.isBanned = entry.fullHistory.stream().anyMatch(p -> p.type() == PunishmentType.BAN && p.isActive());
            }
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
                    sanitizeReason(unescapePipe(parts[1])),
                    unescapePipe(parts[2]),
                    unescapePipe(parts[3]),
                    Boolean.parseBoolean(parts[4])
            );
        }
    }
}