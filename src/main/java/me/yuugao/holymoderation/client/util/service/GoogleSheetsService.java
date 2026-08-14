package me.yuugao.holymoderation.client.util.service;

import static me.yuugao.holymoderation.client.util.Colors.BOLD;
import static me.yuugao.holymoderation.client.util.Colors.DARK_RED;


import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class GoogleSheetsService {
    private static final Pattern CELL_REF_PATTERN = Pattern.compile("^([A-Z]+)(\\d*)$");
    private static final Pattern SPREADSHEET_ID_PATTERN = Pattern.compile("/d/([a-zA-Z0-9-_]+)");
    private static final Pattern GID_PATTERN = Pattern.compile("[#&]gid=(\\d+)");
    private static final Pattern HYPERLINK_PATTERN = Pattern.compile("=HYPERLINK\\(\"([^\"]+)\"\\s*;?\\s*\"([^\"]+)\"\\)", Pattern.CASE_INSENSITIVE);
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();

    private final NotificationsService notificationsService;

    static int parseColumnToIndex(String col) {
        int result = 0;
        for (char c : col.toUpperCase().toCharArray()) {
            result = result * 26 + (c - 'A' + 1);
        }
        return result - 1;
    }

    static int @Nullable [] parseCellReference(String ref) {
        Matcher matcher = CELL_REF_PATTERN.matcher(ref.toUpperCase());
        if (!matcher.find()) return null;

        String colPart = matcher.group(1);
        String rowPart = matcher.group(2);

        int col = parseColumnToIndex(colPart);
        int row = rowPart.isEmpty() ? 0 : Integer.parseInt(rowPart) - 1;

        return new int[]{col, row};
    }

    @Nullable
    public Spreadsheet getPublicSpreadsheet(String url) {
        String spreadsheetId = extractSpreadsheetId(url);
        if (spreadsheetId == null) return null;

        String gid = extractGid(url);
        String csvUrl = buildCsvUrl(spreadsheetId, gid);

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(csvUrl))
                    .GET()
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return new Spreadsheet(spreadsheetId, gid, parseCsv(response.body()));
            }
        } catch (IOException e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в GoogleSheetsService/getPublicSpreadsheet: %s%s".formatted(DARK_RED, e), 5f);
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в GoogleSheetsService/getPublicSpreadsheet: %s%s".formatted(DARK_RED, e), 5f);
            return null;
        }

        return null;
    }

    private String extractSpreadsheetId(String url) {
        Matcher matcher = SPREADSHEET_ID_PATTERN.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractGid(String url) {
        Matcher matcher = GID_PATTERN.matcher(url);
        return matcher.find() ? matcher.group(1) : "0";
    }

    private String buildCsvUrl(String spreadsheetId, String gid) {
        return "https://docs.google.com/spreadsheets/d/%s/export?format=csv&gid=%s".formatted(spreadsheetId, gid);
    }

    private List<List<CellData>> parseCsv(String csv) {
        List<List<CellData>> result = new ArrayList<>();
        List<String> currentRow = new ArrayList<>();
        StringBuilder currentCell = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < csv.length(); i++) {
            char c = csv.charAt(i);

            if (inQuotes) {
                if (c == '"') {
                    if (i + 1 < csv.length() && csv.charAt(i + 1) == '"') {
                        currentCell.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    currentCell.append(c);
                }
            } else {
                if (c == '"') {
                    inQuotes = true;
                } else if (c == ',') {
                    currentRow.add(currentCell.toString());
                    currentCell.setLength(0);
                } else if (c == '\n' || (c == '\r' && i + 1 < csv.length() && csv.charAt(i + 1) == '\n')) {
                    currentRow.add(currentCell.toString());
                    currentCell.setLength(0);
                    result.add(parseRowToCellData(currentRow));
                    currentRow = new ArrayList<>();
                    if (c == '\r') i++;
                } else if (c != '\r') {
                    currentCell.append(c);
                }
            }
        }

        if (!currentCell.isEmpty() || !currentRow.isEmpty()) {
            currentRow.add(currentCell.toString());
            result.add(parseRowToCellData(currentRow));
        }

        return result;
    }

    private List<CellData> parseRowToCellData(List<String> rawCells) {
        List<CellData> cells = new ArrayList<>();
        for (String raw : rawCells) {
            cells.add(parseCellData(raw));
        }
        return cells;
    }

    private CellData parseCellData(String raw) {
        Matcher matcher = HYPERLINK_PATTERN.matcher(raw);
        if (matcher.find()) {
            return new CellData(matcher.group(2), matcher.group(1));
        }
        return new CellData(raw, null);
    }

    public static class Spreadsheet {
        public final String spreadsheetId;
        public final String gid;
        private final List<List<CellData>> data;

        public Spreadsheet(String spreadsheetId, String gid, List<List<CellData>> data) {
            this.spreadsheetId = spreadsheetId;
            this.gid = gid;
            this.data = data;
        }

        public List<CellData> getColumn(String colRef) {
            int col = parseColumnToIndex(colRef);
            if (col < 0) return new ArrayList<>();

            List<CellData> result = new ArrayList<>();
            for (List<CellData> row : data) {
                result.add(col < row.size() ? row.get(col) : new CellData("", null));
            }
            return result;
        }
    }

    public record CellData(String text, @Nullable String hyperlink) {
        public boolean hasHyperlink() {
            return hyperlink != null && !hyperlink.isEmpty();
        }

        @Override
        public @NotNull String toString() {
            return hasHyperlink() ? text + " [" + hyperlink + "]" : text;
        }
    }
}