package me.yuugao.holymoderation.client.util.service;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Central registry of HolyWorld server chat/format literals and parsing helpers.
 * <p>
 * All text the mod recognises from the HolyWorld server (chat prefixes, /find and /playtime
 * responses, /history markers, /checkban output, freeze messages, location names) lives here.
 * When the server changes a string, this is the single file to edit — instead of hunting
 * across ChatService, SpyModule, CheckoutsModule, StateModule and TwinksCheckModule.
 * <p>
 * Helpers are pure functions: behaviour matches the previous inline checks exactly.
 */
public final class HolyWorldPatterns {

    // ===== Chat prefixes to ignore (formatReceivedText) =====
    public static final List<String> IGNORED_PREFIXES = List.of(
            "[ALL] ʟ", "[Тихий] ❖", "SC |", "HW >", " ▬▬▬", "▬▬▬", "[PMS]:", "◀", "[HM]", "[HAC]", "[я"
    );
    // ===== Freeze / checkout flow =====
    public static final String FREEZE_OK = "Игрок заморожен!";
    public static final String FREEZE_NOT_FOUND = "Игрок не найден!";
    public static final String FREEZE_LEAVE_PREFIX = "▶ Замороженный игрок ";
    public static final String HUB_CHECK_START = "▶ Ожидайте завершения проверки... Пожалуйста, не двигайтесь.";
    public static final String HUB_CAPTCHA = "▶ Введите цифры с картинки в чат! Для открытия чата, нажмите <T>";
    // ===== /find response parsing =====
    public static final String FIND_OFFLINE = "Игрок оффлайн";
    public static final String FIND_PREFIX = "Игрок";
    public static final String FIND_ON_SERVER_SUFFIX = "сервере ";
    // ===== /playtime response parsing =====
    public static final String PLAYTIME_CURRENT_PREFIX = "Текущая";
    public static final String PLAYTIME_LAST_PREFIX = "Последняя";
    public static final String PLAYTIME_SEPARATOR = "----------";
    public static final String OFFLINE_LITERAL = "Оффлайн";
    // ===== Location name normalization (server codes -> mod tokens) =====
    public static final String LOC_LITE120_FULL = "l2anarchy";
    public static final String LOC_LITE_FULL = "lanarchy";
    public static final String LOC_CLASSIC_FULL = "anarchy";
    public static final String LOC_LPVP = "lpvp";
    public static final String LOC_SUFFIX = "anarchy";
    // ===== /checkban parsing =====
    public static final String CHECKBAN_CLEAN_TARGET = "Цель не забанена!";
    public static final String CHECKBAN_CLEAN_HISTORY = "История не найдена.";
    public static final String CHECKBAN_INFO_PREFIX = "Игрок [";
    public static final String CHECKBAN_REASON_PREFIX = "Причина:";
    public static final String CHECKBAN_IPBAN_PREFIX = "IP бан:";
    // ===== /history parsing (TwinksCheckModule) =====
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

    /**
     * "Замороженный игрок <nick>" leave-during-check message.
     */
    public static boolean isFreezeLeave(String text, String nickname) {
        return text.startsWith(FREEZE_LEAVE_PREFIX + nickname);
    }

    /**
     * Hub-captcha / check-in-progress messages that put the player into the hub state.
     */
    public static boolean isHubGate(String text) {
        return text.equals(HUB_CHECK_START) || text.equals(HUB_CAPTCHA);
    }

    /**
     * Parse a /find response line into a status token.
     *
     * @param findLine raw server line like "Игрок находится на сервере l2anarchy1" or "Игрок оффлайн"
     * @return "offline" / "lobby" / a formatted location ("lite120-1") from formatLocation,
     * or null if the line is not a /find response.
     */
    public static String parseFindStatus(String findLine) {
        if (findLine.equals(FIND_OFFLINE)) return "offline";
        String[] parts = findLine.split(FIND_ON_SERVER_SUFFIX);
        if (parts.length < 2) return null;
        String raw = parts[1];
        if (raw.startsWith("lobby")) return "lobby";
        return formatLocation(raw);
    }

    /**
     * True for the /find header ("Игрок ... сервере ...") that is not about the local player.
     */
    public static boolean isFindResponseAboutOther(String text, String selfNickname) {
        return text.startsWith(FIND_PREFIX) && !text.startsWith("Игрок " + selfNickname);
    }

    /**
     * True for the /find header line that IS about the local player ("Игрок <self> ...").
     */
    public static boolean isFindResponseAboutSelf(String text, String selfNickname) {
        return text.startsWith("Игрок " + selfNickname);
    }

    /**
     * Lines that /playtime emits and the player never needs to see in chat (cancelled by the mod).
     * Kept as an explicit list so a future format change only touches this constant.
     */
    public static boolean isPlaytimeNoise(String text) {
        return text.startsWith("Активность") || text.startsWith("Общее время")
                || text.startsWith(PLAYTIME_CURRENT_PREFIX) || text.startsWith("Время")
                || text.startsWith(PLAYTIME_LAST_PREFIX) || text.startsWith("Последний")
                || text.startsWith(PLAYTIME_SEPARATOR) || text.isEmpty();
    }

    /**
     * Extract the location from a "Текущая ...: <loc>" line, or detect offline.
     * Returns the raw bracket-stripped token (NOT formatted — caller decides), "Оффлайн" sentinel,
     * or null if the line isn't a "Текущая" line.
     */
    public static String extractPlaytimeLocation(String currentLine) {
        if (!currentLine.startsWith(PLAYTIME_CURRENT_PREFIX)) return null;
        String loc = currentLine.split(": ")[1];
        return loc.substring(1, loc.length() - 1);
    }

    /**
     * Extract the activity from a "Последняя ...: <activity>" line, or null if not such a line.
     */
    public static String extractPlaytimeActivity(String lastLine) {
        if (!lastLine.startsWith(PLAYTIME_LAST_PREFIX)) return null;
        return lastLine.split(": ")[1];
    }

    /**
     * Convert a raw server location code (e.g. "l2anarchy3") into the mod's internal token
     * (e.g. "lite120-3"). Mirrors the previous inline chain in ChatService.formatLocation.
     */
    public static String formatLocation(String location) {
        if (location.equals(LOC_LITE120_FULL)) return "lite120-1";
        if (location.equals(LOC_LITE_FULL)) return "lite-1";
        if (location.equals(LOC_CLASSIC_FULL)) return "classic-1";
        if (location.equals(LOC_LPVP)) return "lpvp";
        if (location.startsWith("l2")) return "lite120-%s".formatted(location.split(LOC_SUFFIX)[1]);
        if (location.startsWith("l")) return "lite-%s".formatted(location.split(LOC_SUFFIX)[1]);
        return "classic-%s".formatted(location.split(LOC_SUFFIX)[1]);
    }

    /**
     * True if the line aborts a /checkban lookup (target clean / no history).
     */
    public static boolean isCheckbanAbort(String text) {
        return text.equals(CHECKBAN_CLEAN_TARGET) || text.equals(CHECKBAN_CLEAN_HISTORY);
    }

    /**
     * Extract the ban reason from a "Причина: <reason> | ..." line.
     * Returns null if the line is not a reason line.
     */
    public static String extractBanReason(String reasonLine) {
        if (!reasonLine.startsWith(CHECKBAN_REASON_PREFIX)) return null;
        return reasonLine.split("Причина: ")[1].split(" \\| ")[0];
    }

    /**
     * True if a chat line belongs to a /history block and should be captured to the temp file.
     */
    public static boolean isHistoryMessage(String message) {
        return message.startsWith(HISTORY_MARKER) || message.startsWith(HISTORY_PLAYER_PREFIX)
                || message.startsWith(HISTORY_REASON_PREFIX) || message.startsWith(HISTORY_HEADER_PREFIX)
                || message.startsWith(HISTORY_ENDING_PREFIX + " через")
                || message.startsWith(HISTORY_UNBANNED_PREFIX) || message.startsWith(HISTORY_UNMUTED_PREFIX)
                || message.trim().isEmpty();
    }

    /**
     * True if the captured /history block represents "no history" — either the single
     * "История не найдена." line, or the two-line "История .../пусто" header form.
     */
    public static boolean isNoHistory(List<String> lines) {
        if (lines.size() == 1 && lines.get(0).equals(HISTORY_NOT_FOUND)) return true;
        return lines.size() == 2 && lines.get(0).startsWith(HISTORY_HEADER_PREFIX + " ") && lines.get(1).isEmpty();
    }

    /**
     * True if a line starts a new /history block entry or a section header (so collectEntryLines stops).
     */
    public static boolean isHistoryBlockBoundary(String line) {
        return line.startsWith(HISTORY_MARKER) || line.startsWith(HISTORY_HEADER_PREFIX)
                || line.startsWith(HISTORY_ENDING_PREFIX) || line.startsWith(HISTORY_UNBANNED_PREFIX)
                || line.startsWith(HISTORY_UNMUTED_PREFIX);
    }

    /**
     * True for the entry header line "Игрок <nick> был ...".
     */
    public static boolean isHistoryPlayerLine(String line) {
        return line.startsWith(HISTORY_PLAYER_PREFIX + " ") && line.contains(HISTORY_PLAYER_WAS);
    }

    /**
     * Classify a "Игрок ... был ..." line, or null if not a punishment line.
     */
    public static PunishmentKind classifyPunishment(String line) {
        if (!isHistoryPlayerLine(line)) return null;
        if (line.contains(HISTORY_BAN_MARK)) return PunishmentKind.BAN;
        if (line.contains(HISTORY_KICK_MARK)) return PunishmentKind.KICK;
        if (line.contains(HISTORY_MUTE_MARK)) return PunishmentKind.MUTE;
        return null;
    }

    /**
     * Extract the reason from a "по причина: '...'" line, falling back to the raw remainder.
     */
    public static String extractHistoryReason(String line) {
        if (!line.startsWith(HISTORY_REASON_PREFIX)) return StringUtils.EMPTY;
        int first = line.indexOf('\'');
        int last = line.lastIndexOf('\'');
        if (first >= 0 && last > first) {
            return line.substring(first + 1, last);
        }
        return line.replaceFirst("^по причина:\\s*", "").trim();
    }

    /**
     * True if an active "[Активный]" status tag is present in the line.
     */
    public static boolean isBanStatusActive(String line) {
        Matcher m = BAN_STATUS_PATTERN.matcher(line);
        return m.find() && "Активный".equals(m.group(1));
    }

    /**
     * Returns the matcher over the status tag (for two-line lookups).
     */
    public static Matcher banStatusMatcher(String line) {
        return BAN_STATUS_PATTERN.matcher(line);
    }

    public enum PunishmentKind {BAN, KICK, MUTE}
}
