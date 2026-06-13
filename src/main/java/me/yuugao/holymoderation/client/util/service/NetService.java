package me.yuugao.holymoderation.client.util.service;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class NetService {
    private final LoggerService loggerService;
    private final ConfigManagerService configManagerService;
    private final NotificationsService notificationsService;
    private final SoundService soundService;
    private final AsyncExecutor asyncExecutor;
    private final String journalApiUrl = "https://journal.holyworld.me/srv/api/v1/";
    private final Gson gson = new Gson();

    public CompletableFuture<List<AbstractMap.SimpleEntry<String, String>>> getWhiteList() {
        return getListAsync("https://holymoderation.alwaysdata.net/whitelist");
    }

    public CompletableFuture<List<AbstractMap.SimpleEntry<String, String>>> getBlackList() {
        return getListAsync("https://holymoderation.alwaysdata.net/blacklist");
    }

    private CompletableFuture<List<AbstractMap.SimpleEntry<String, String>>> getListAsync(String url) {
        return asyncExecutor.supplyAsync("NetService/getList " + url, () -> {
            try {
                HttpsURLConnection connection = openHttpsConnection(url, "GET", null);
                String response = getResponse(connection).toString();

                Gson gson = new Gson();
                Type type = new TypeToken<Map<String, List<String>>>() {
                }.getType();
                Map<String, List<String>> data = gson.fromJson(response, type);

                if (data == null) {
                    return Collections.emptyList();
                }

                List<AbstractMap.SimpleEntry<String, String>> result = new ArrayList<>();
                for (Map.Entry<String, List<String>> entry : data.entrySet()) {
                    String player = entry.getKey();
                    List<String> hwids = entry.getValue();
                    if (player == null || hwids == null) continue;
                    for (String hwid : hwids) {
                        if (hwid == null || hwid.isEmpty()) continue;
                        result.add(new AbstractMap.SimpleEntry<>(player, hwid));
                    }
                }
                return result;
            } catch (Exception e) {
                notificationsService.addNotification(NotificationType.EXCEPTION,
                        "%s%sИсключение".formatted(DARK_RED, BOLD),
                        "NetService/getList: %s".formatted(e), 5f);
                return Collections.emptyList();
            }
        });
    }

    public CompletableFuture<AbstractMap.SimpleEntry<String, String>> getLastUpdates() {
        return asyncExecutor.supplyAsync("NetService/getLastUpdates", () -> {
            try {
                String lastUpdatesUrl = "https://raw.githubusercontent.com/Gr0wMan/HolyModeration-Releases/main/LATEST.txt";
                HttpsURLConnection connection = openHttpsConnection(lastUpdatesUrl, "GET", null);
                String response = getResponse(connection).toString();
                String[] responseSplit = response.split("%%%");
                return new AbstractMap.SimpleEntry<>(responseSplit[0], responseSplit[1]);
            } catch (IOException e) {
                notificationsService.addNotification(NotificationType.EXCEPTION,
                        "%s%sИсключение".formatted(DARK_RED, BOLD),
                        "Исключение в NetService/getLastUpdates: %s%s".formatted(DARK_RED, e), 5f);
                return null;
            }
        });
    }

    public CompletableFuture<List<String>> getSoundsList() {
        return asyncExecutor.supplyAsync("NetService/getSoundsList", () -> {
            List<String> soundFiles = new ArrayList<>();
            try {
                String soundsListUrl = "https://github.com/meyuugao/HolyModeration-Releases/tree/main/Sounds";
                HttpsURLConnection connection = openHttpsConnection(soundsListUrl, "GET", null);
                StringBuilder response = getResponse(connection);
                String html = response.toString();
                int index = 0;
                while ((index = html.indexOf("Sounds/", index)) != -1) {
                    int start = index + 7;
                    int end = html.indexOf("\"", start);
                    if (end == -1) break;
                    String fileName = html.substring(start, end);
                    if (fileName.endsWith(".wav")) soundFiles.add(fileName);
                    index = end + 1;
                }
                connection.disconnect();
            } catch (IOException e) {
                notificationsService.addNotification(NotificationType.EXCEPTION,
                        "%s%sИсключение".formatted(DARK_RED, BOLD),
                        "Исключение в NetService/getSoundsList: %s%s".formatted(DARK_RED, e), 5f);
            }
            return soundFiles;
        });
    }

    public CompletableFuture<Void> downloadSounds() {
        return getSoundsList().thenCompose(sounds ->
                asyncExecutor.runAsync("NetService/downloadSounds", () -> {
                    try {
                        Path soundsDir = soundService.getSoundsDir();

                        if (Files.exists(soundsDir)) {
                            try (var stream = Files.walk(soundsDir)) {
                                stream.filter(p -> !p.equals(soundsDir))
                                        .sorted(Comparator.reverseOrder())
                                        .forEach(p -> {
                                            try { Files.delete(p); } catch (IOException ignored) {}
                                        });
                            }
                        } else {
                            Files.createDirectories(soundsDir);
                        }

                        for (String sound : sounds) {
                            String fileUrl =
                                    "https://raw.githubusercontent.com/meyuugao/HolyModeration-Releases/main/Sounds/%s"
                                            .formatted(sound);

                            Path filePath = soundsDir.resolve(sound);

                            try (InputStream in = new URL(fileUrl).openStream()) {
                                Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
                            }
                        }

                    } catch (IOException e) {
                        notificationsService.addNotification(
                                NotificationType.EXCEPTION,
                                "%s%sИсключение".formatted(DARK_RED, BOLD),
                                "downloadSounds: " + e,
                                5f
                        );
                    }
                })
        );
    }

    public CompletableFuture<Map<String, Object>> getJournalProfile() {
        return executeGetRequestAsync("me");
    }

    public CompletableFuture<Map<String, Object>> getJournalStats() {
        return executeGetRequestAsync("stats");
    }

    private CompletableFuture<Map<String, Object>> executeGetRequestAsync(String endpoint) {
        return asyncExecutor.supplyAsync("NetService/executeGetRequest " + endpoint, () -> {
            try {
                HttpsURLConnection connection = openHttpsConnection("%s%s".formatted(journalApiUrl, endpoint), "GET", null);
                if (connection == null) throw new IOException("connection is null");
                try {
                    setAuthHeaders(connection);
                    return parseJsonResponse(getResponse(connection));
                } finally {
                    connection.disconnect();
                }
            } catch (IOException e) {
                notificationsService.addNotification(NotificationType.EXCEPTION,
                        "%s%sИсключение".formatted(DARK_RED, BOLD),
                        "Исключение в NetService/executeGetRequest: %s%s".formatted(DARK_RED, e), 5f);
                return Collections.emptyMap();
            }
        });
    }

    public CompletableFuture<Void> startCheckout(String name, String reason, String mode, int number, boolean pvp) {
        return hasActiveCheckout().thenCompose(active -> {
            if (active) {
                notificationsService.addNotification(
                        NotificationType.ERROR,
                        "%s%sОшибка".formatted(RED, BOLD),
                        "У вас уже есть активная проверка.",
                        5f
                );
                return CompletableFuture.completedFuture(null);
            }

            return asyncExecutor.runAsync("NetService/startCheckout", () -> {
                try {
                    HttpsURLConnection connection =
                            openHttpsConnection("%scheckout/start".formatted(journalApiUrl), "POST", null);

                    if (connection == null) throw new IOException("connection is null");

                    try {
                        setAuthHeaders(connection);

                        JsonObject jsonBody = new JsonObject();
                        jsonBody.addProperty("username", name);
                        jsonBody.addProperty("reason", reason);
                        jsonBody.addProperty("mode", mode);
                        jsonBody.addProperty("anarchyNumber", number);
                        jsonBody.addProperty("isPvpAnarchy", pvp);

                        if (writeJson(connection, jsonBody)) {
                            int code = connection.getResponseCode();

                            notificationsService.addNotification(
                                    code == 201 ? NotificationType.SUCCESS : NotificationType.ERROR,
                                    "%s%sРезультат".formatted(code == 201 ? GREEN : RED, BOLD),
                                    code == 201
                                            ? "Проверка успешно создана"
                                            : "Ошибка. Код: " + code,
                                    5f
                            );
                        }
                    } finally {
                        connection.disconnect();
                    }

                } catch (IOException e) {
                    notificationsService.addNotification(
                            NotificationType.EXCEPTION,
                            "%s%sИсключение".formatted(DARK_RED, BOLD),
                            e.toString(),
                            5f
                    );
                }
            });
        });
    }

    public CompletableFuture<Void> endCheckout(String result, String reason, boolean destroyStash) {
        return hasActiveCheckout().thenCompose(active -> {

            if (!active) {
                notificationsService.addNotification(
                        NotificationType.ERROR,
                        "%s%sОшибка".formatted(RED, BOLD),
                        "У вас нет активной проверки.",
                        5f
                );
                return CompletableFuture.completedFuture(null);
            }

            return asyncExecutor.runAsync("NetService/endCheckout", () -> {
                try {
                    HttpsURLConnection connection =
                            openHttpsConnection("%scheckout/end".formatted(journalApiUrl), "POST", null);

                    if (connection == null) throw new IOException("connection is null");

                    try {
                        setAuthHeaders(connection);

                        JsonObject jsonBody = new JsonObject();
                        jsonBody.addProperty("result", result);
                        jsonBody.addProperty("banReason", reason);
                        jsonBody.addProperty("destroyStash", destroyStash);

                        if (writeJson(connection, jsonBody)) {
                            int code = connection.getResponseCode();

                            notificationsService.addNotification(
                                    code == 201 ? NotificationType.SUCCESS : NotificationType.ERROR,
                                    "%s%sРезультат".formatted(code == 201 ? GREEN : RED, BOLD),
                                    code == 201
                                            ? "Проверка завершена"
                                            : "Ошибка. Код: " + code,
                                    5f
                            );
                        }

                    } finally {
                        connection.disconnect();
                    }

                } catch (IOException e) {
                    notificationsService.addNotification(
                            NotificationType.EXCEPTION,
                            "%s%sИсключение".formatted(DARK_RED, BOLD),
                            e.toString(),
                            5f
                    );
                }
            });
        });
    }

    private CompletableFuture<Boolean> hasActiveCheckout() {
        return asyncExecutor.supplyAsync("NetService/hasActiveCheckout", () -> {
            try {
                HttpsURLConnection connection = openHttpsConnection("%scheckout/status".formatted(journalApiUrl), "GET", null);
                if (connection == null) throw new IOException("connection is null");
                try {
                    setAuthHeaders(connection);
                    StringBuilder response = getResponse(connection);
                    Map<String, Object> jsonResponse = parseJsonResponse(response);
                    Object status = jsonResponse.get("status");
                    return status instanceof Boolean && (Boolean) status;
                } finally {
                    connection.disconnect();
                }
            } catch (IOException e) {
                notificationsService.addNotification(NotificationType.EXCEPTION,
                        "%s%sИсключение".formatted(DARK_RED, BOLD),
                        "Исключение в NetService/hasActiveCheckout: %s%s".formatted(DARK_RED, e), 5f);
                return false;
            }
        });
    }

    public CompletableFuture<Void> sendLaunchData(String hwid, String username) {
        return asyncExecutor.runAsync("NetService/sendLaunchData", () -> {
            String endpoint = "https://holymoderation.alwaysdata.net/api/launch";

            try {
                HttpsURLConnection connection = openHttpsConnection(endpoint, "POST", null);
                if (connection == null) throw new IOException("Connection is null");
                try {
                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("hwid", hwid);
                    connection.setRequestProperty("username", username);
                    connection.setDoOutput(false);

                    connection.getResponseCode();
                } finally {
                    connection.disconnect();
                }
            } catch (IOException e) {
                notificationsService.addNotification(NotificationType.EXCEPTION,
                        "%s%sИсключение".formatted(DARK_RED, BOLD),
                        "Системная ошибка: %s%s".formatted(DARK_RED, e.getMessage()), 5f);
            }
        });
    }

    public CompletableFuture<Boolean> downloadViewerJar(Path target) {
        return asyncExecutor.supplyAsync("NetService/downloadViewerJar", () -> {
            try {
                String url = "https://holymoderation.alwaysdata.net/viewer";
                loggerService.info("Downloading Viewer.jar from: " + url);
                HttpsURLConnection connection = openHttpsConnection(url, "GET", null);
                if (connection == null) return false;
                try (InputStream in = connection.getInputStream()) {
                    Files.copy(in, target, StandardCopyOption.REPLACE_EXISTING);
                } finally {
                    connection.disconnect();
                }
                return true;
            } catch (IOException e) {
                loggerService.exception("NetService/downloadViewerJar: " + e);
                return false;
            }
        });
    }

    private void setAuthHeaders(@NotNull HttpsURLConnection connection) {
        connection.setRequestProperty("x-token", configManagerService.getApiConfig().getApiToken());
        connection.setRequestProperty("Content-Type", "application/json");
    }

    private boolean writeJson(@NotNull HttpsURLConnection connection, @NotNull JsonObject jsonBody) {
        try {
            connection.setDoOutput(true);
            try (OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
                out.write(jsonBody.toString());
            }
            return true;
        } catch (IOException e) {
            notificationsService.addNotification(NotificationType.EXCEPTION,
                    "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "NetService/writeJson: %s".formatted(e), 5f);
            return false;
        }
    }

    public StringBuilder getResponse(@NotNull HttpsURLConnection connection) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) response.append(line);
        reader.close();
        return response;
    }

    private Map<String, Object> parseJsonResponse(@NotNull StringBuilder response) {
        if (response.isEmpty()) return Collections.emptyMap();
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        return gson.fromJson(response.toString(), type);
    }

    public HttpsURLConnection openHttpsConnection(String url, String method, String cookie) throws IOException {
        HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("User-Agent", "Mozilla/5.0");
        connection.setConnectTimeout(10_000);
        connection.setReadTimeout(15_000);
        if (cookie != null) connection.setRequestProperty("Cookie", cookie);
        return connection;
    }
}