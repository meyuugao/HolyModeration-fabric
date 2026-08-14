package me.yuugao.holymoderation.client.util.service;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HolyWorldPatterns {

    private static final Pattern HISTORY_REASON_FALLBACK = Pattern.compile("^по причина:\\s*");

    public static final List<String> IGNORED_PREFIXES = List.of(
            "[ALL] ʟ", "[Тихий] ❖", "SC |", "HW >", " ▬▬▬", "▬▬▬", "[PMS]:", "◀", "[HM]", "[HAC]", "[я"
    );
    public static final String FREEZE_OK = "Игрок заморожен!";
    public static final String FREEZE_NOT_FOUND = "Игрок не найден!";
    public static final String FREEZE_LEAVE_PREFIX = "▶ Замороженный игрок ";
    public static final String HUB_CHECK_START = "▶ Ожидайте завершения проверки... Пожалуйста, не двигайтесь.";
    public static final String HUB_CAPTCHA = "▶ Введите цифры с картинки в чат! Для открытия чата, нажмите <T>";
    public static final String FIND_OFFLINE = "Игрок оффлайн";
    public static final String FIND_PREFIX = "Игрок";
    public static final String FIND_ON_SERVER_SUFFIX = "сервере ";
    public static final String PLAYTIME_CURRENT_PREFIX = "Текущая";
    public static final String PLAYTIME_LAST_PREFIX = "Последняя";
    public static final String PLAYTIME_SEPARATOR = "----------";
    public static final String OFFLINE_LITERAL = "Оффлайн";
    public static final String LOC_LITE120_FULL = "l2anarchy";
    public static final String LOC_LITE_FULL = "lanarchy";
    public static final String LOC_CLASSIC_FULL = "anarchy";
    public static final String LOC_LPVP = "lpvp";
    public static final String LOC_SUFFIX = "anarchy";
    public static final String CHECKBAN_CLEAN_TARGET = "Цель не забанена!";
    public static final String CHECKBAN_CLEAN_HISTORY = "История не найдена.";
    public static final String CHECKBAN_INFO_PREFIX = "Игрок [";
    public static final String CHECKBAN_REASON_PREFIX = "Причина:";
    public static final String CHECKBAN_IPBAN_PREFIX = "IP бан:";
    public static final String HISTORY_MARKER = " -- [";
    public static final String HISTORY_PLAYER_PREFIX = "Игрок";
    public static final String HISTORY_REASON_PREFIX = "по причина:";
    public static final String HISTORY_HEADER_PREFIX = "История";
    public static final String HISTORY_NOT_FOUND = "История не найдена.";
    public static final String HISTORY_ENDING_PREFIX = "Окончание";
    public static final String HISTORY_UNBANNED_PREFIX = "Разбанен:";
    public static final String HISTORY_UNMUTED_PREFIX = "Размьючен:";
    public static final String HISTORY_BAN_MARK = "был забанен";
    public static final String HISTORY_KICK_MARK = "был кикнут";
    public static final String HISTORY_MUTE_MARK = "был замьючен";
    public static final String HISTORY_PLAYER_WAS = " был ";
    public static final Pattern BAN_STATUS_PATTERN = Pattern.compile("\\[(Активный|Истёкший)]");
    private HolyWorldPatterns() {
    }


    public static boolean isFreezeLeave(String text, String nickname) {
        return text.startsWith(FREEZE_LEAVE_PREFIX + nickname);
    }


    public static boolean isHubGate(String text) {
        return text.equals(HUB_CHECK_START) || text.equals(HUB_CAPTCHA);
    }

    public static String parseFindStatus(String findLine) {
        if (findLine.equals(FIND_OFFLINE)) return "offline";
        String[] parts = findLine.split(FIND_ON_SERVER_SUFFIX);
        if (parts.length < 2) return null;
        String raw = parts[1];
        if (raw.startsWith("lobby")) return "lobby";
        return formatLocation(raw);
    }

    public static boolean isFindResponseAboutOther(String text, String selfNickname) {
        return text.startsWith(FIND_PREFIX) && !text.startsWith("Игрок " + selfNickname);
    }

    public static boolean isFindResponseAboutSelf(String text, String selfNickname) {
        return text.startsWith("Игрок " + selfNickname);
    }

    public static boolean isPlaytimeNoise(String text) {
        return text.startsWith("Активность") || text.startsWith("Общее время")
                || text.startsWith(PLAYTIME_CURRENT_PREFIX) || text.startsWith("Время")
                || text.startsWith(PLAYTIME_LAST_PREFIX) || text.startsWith("Последний")
                || text.startsWith(PLAYTIME_SEPARATOR) || text.isEmpty();
    }


    public static String extractPlaytimeLocation(String currentLine) {
        if (!currentLine.startsWith(PLAYTIME_CURRENT_PREFIX)) return null;
        String loc = currentLine.split(": ")[1];
        return loc.substring(1, loc.length() - 1);
    }

    public static String extractPlaytimeActivity(String lastLine) {
        if (!lastLine.startsWith(PLAYTIME_LAST_PREFIX)) return null;
        return lastLine.split(": ")[1];
    }

    public static String formatLocation(String location) {
        if (location.equals(LOC_LITE120_FULL)) return "lite120-1";
        if (location.equals(LOC_LITE_FULL)) return "lite-1";
        if (location.equals(LOC_CLASSIC_FULL)) return "classic-1";
        if (location.equals(LOC_LPVP)) return "lpvp";
        if (location.startsWith("l2")) return "lite120-%s".formatted(location.split(LOC_SUFFIX)[1]);
        if (location.startsWith("l")) return "lite-%s".formatted(location.split(LOC_SUFFIX)[1]);
        return "classic-%s".formatted(location.split(LOC_SUFFIX)[1]);
    }


    public static boolean isCheckbanAbort(String text) {
        return text.equals(CHECKBAN_CLEAN_TARGET) || text.equals(CHECKBAN_CLEAN_HISTORY);
    }


    public static String extractBanReason(String reasonLine) {
        if (!reasonLine.startsWith(CHECKBAN_REASON_PREFIX)) return null;
        return reasonLine.split("Причина: ")[1].split(" \\| ")[0];
    }

    public static boolean isHistoryMessage(String message) {
        return message.startsWith(HISTORY_MARKER) || message.startsWith(HISTORY_PLAYER_PREFIX)
                || message.startsWith(HISTORY_REASON_PREFIX) || message.startsWith(HISTORY_HEADER_PREFIX)
                || message.startsWith(HISTORY_ENDING_PREFIX + " через")
                || message.startsWith(HISTORY_UNBANNED_PREFIX) || message.startsWith(HISTORY_UNMUTED_PREFIX)
                || message.trim().isEmpty();
    }


    public static boolean isNoHistory(List<String> lines) {
        if (lines.size() == 1 && lines.get(0).equals(HISTORY_NOT_FOUND)) return true;
        return lines.size() == 2 && lines.get(0).startsWith(HISTORY_HEADER_PREFIX + " ") && lines.get(1).isEmpty();
    }


    public static boolean isHistoryBlockBoundary(String line) {
        return line.startsWith(HISTORY_MARKER) || line.startsWith(HISTORY_HEADER_PREFIX)
                || line.startsWith(HISTORY_ENDING_PREFIX) || line.startsWith(HISTORY_UNBANNED_PREFIX)
                || line.startsWith(HISTORY_UNMUTED_PREFIX);
    }


    public static boolean isHistoryPlayerLine(String line) {
        return line.startsWith(HISTORY_PLAYER_PREFIX + " ") && line.contains(HISTORY_PLAYER_WAS);
    }


    public static PunishmentKind classifyPunishment(String line) {
        if (!isHistoryPlayerLine(line)) return null;
        if (line.contains(HISTORY_BAN_MARK)) return PunishmentKind.BAN;
        if (line.contains(HISTORY_KICK_MARK)) return PunishmentKind.KICK;
        if (line.contains(HISTORY_MUTE_MARK)) return PunishmentKind.MUTE;
        return null;
    }


    public static String extractHistoryReason(String line) {
        if (!line.startsWith(HISTORY_REASON_PREFIX)) return StringUtils.EMPTY;
        int first = line.indexOf('\'');
        int last = line.lastIndexOf('\'');
        if (first >= 0 && last > first) {
            return line.substring(first + 1, last);
        }
        return HISTORY_REASON_FALLBACK.matcher(line).replaceAll("").trim();
    }


    public static boolean isBanStatusActive(String line) {
        Matcher m = BAN_STATUS_PATTERN.matcher(line);
        return m.find() && "Активный".equals(m.group(1));
    }

    public enum PunishmentKind {BAN, KICK, MUTE}
}
