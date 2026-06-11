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
    private final NotificationsService notificationsService;

    static int parseColumnToIndex(String col) {
        int result = 0;
        for (char c : col.toUpperCase().toCharArray()) {
            result = result * 26 + (c - 'A' + 1);
        }
        return result - 1;
    }

    static int @Nullable [] parseCellReference(String ref) {
        Pattern columnPattern = Pattern.compile("^([A-Z]+)(\\d*)$");
        Matcher matcher = columnPattern.matcher(ref.toUpperCase());
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

            HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return new Spreadsheet(spreadsheetId, gid, parseCsv(response.body()));
            }
        } catch (IOException e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в NetService/executeGetRequest: %s%s".formatted(DARK_RED, e), 5f);
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в NetService/executeGetRequest: %s%s".formatted(DARK_RED, e), 5f);
            return null;
        }

        return null;
    }

    private String extractSpreadsheetId(String url) {
        Pattern sheetIdPattern = Pattern.compile("/d/([a-zA-Z0-9-_]+)");
        Matcher matcher = sheetIdPattern.matcher(url);
        return matcher.find() ? matcher.group(1) : null;
    }

    private String extractGid(String url) {
        Pattern gidPattern = Pattern.compile("[#&]gid=(\\d+)");
        Matcher matcher = gidPattern.matcher(url);
        return matcher.find() ? matcher.group(1) : "0";
    }

    private String buildCsvUrl(String spreadsheetId, String gid) {
        return "https://docs.google.com/spreadsheets/d/" + spreadsheetId + "/export?format=csv&gid=" + gid;
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
        Pattern hyperlinkPattern = Pattern.compile("=HYPERLINK\\(\"([^\"]+)\"\\s*;?\\s*\"([^\"]+)\"\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = hyperlinkPattern.matcher(raw);
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

        @Nullable
        public CellData getCell(String cellRef) {
            int[] coords = parseCellReference(cellRef);
            if (coords == null) return null;
            return getCell(coords[0], coords[1]);
        }

        @Nullable
        public CellData getCell(int col, int row) {
            if (row < 0 || row >= data.size()) return null;
            List<CellData> rowData = data.get(row);
            if (col < 0 || col >= rowData.size()) return null;
            return rowData.get(col);
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

        public List<CellData> getRow(int row) {
            if (row < 0 || row >= data.size()) return new ArrayList<>();
            return new ArrayList<>(data.get(row));
        }

        public List<List<CellData>> getRange(String range) {
            String[] parts = range.split("-");
            if (parts.length != 2) return new ArrayList<>();

            int[] start = parseCellReference(parts[0].trim());
            int[] end = parseCellReference(parts[1].trim());

            if (start == null || end == null) return new ArrayList<>();

            return getRange(start[0], start[1], end[0], end[1]);
        }

        public List<List<CellData>> getRange(int startCol, int startRow, int endCol, int endRow) {
            List<List<CellData>> result = new ArrayList<>();

            int minRow = Math.min(startRow, endRow);
            int maxRow = Math.min(Math.max(startRow, endRow), data.size() - 1);
            int minCol = Math.min(startCol, endCol);
            int maxCol = Math.max(startCol, endCol);

            for (int r = minRow; r <= maxRow; r++) {
                List<CellData> rowResult = new ArrayList<>();
                List<CellData> rowData = data.get(r);

                for (int c = minCol; c <= maxCol; c++) {
                    rowResult.add(c < rowData.size() ? rowData.get(c) : new CellData("", null));
                }
                result.add(rowResult);
            }

            return result;
        }

        public List<CellData> getFlatRange(String range) {
            List<List<CellData>> matrix = getRange(range);
            List<CellData> flat = new ArrayList<>();
            for (List<CellData> row : matrix) {
                flat.addAll(row);
            }
            return flat;
        }

        public int getRowCount() {
            return data.size();
        }

        public int getColumnCount(int row) {
            if (row < 0 || row >= data.size()) return 0;
            return data.get(row).size();
        }

        public boolean isEmpty() {
            return data.isEmpty();
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