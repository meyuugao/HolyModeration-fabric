package me.yuugao.holymoderation.client.util.service;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class NetService {
    private final ConfigManagerService configManagerService;
    private final NotificationsService notificationsService;
    private final SoundService soundService;
    private final String journalApiUrl = "https://journal.holyworld.me/srv/api/v1/";
    private final Gson gson = new Gson();

    public List<AbstractMap.SimpleEntry<String, String>> getBanLists() {
        try {
            String url = "https://raw.githubusercontent.com/Gr0wMan/HolyModeration-Releases/main/BANLIST.json";

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
            notificationsService.addNotification(
                    NotificationType.EXCEPTION,
                    "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "NetService/getBanList: %s".formatted(e),
                    5f
            );

            return Collections.emptyList();
        }
    }

    public AbstractMap.SimpleEntry<String, String> getLastUpdates() {
        try {
            String lastUpdatesUrl = "https://raw.githubusercontent.com/Gr0wMan/HolyModeration-Releases/main/LATEST.txt";
            HttpsURLConnection connection = openHttpsConnection(lastUpdatesUrl, "GET", null);
            String response = getResponse(connection).toString();
            String[] responseSplit = response.split("%%%");
            return new AbstractMap.SimpleEntry<>(responseSplit[0], responseSplit[1]);
        } catch (IOException e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в NetService/getLastUpdates: %s%s".formatted(DARK_RED, e), 5f);
            return null;
        }
    }

    public List<String> getSoundsList() {
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
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в NetService/getSoundsList: %s%s".formatted(DARK_RED, e), 5f);
        }
        return soundFiles;
    }

    public void downloadSounds() {
        try {
            Path soundsDir = soundService.getSoundsDir();
            if (Files.exists(soundsDir)) {
                try (var stream = Files.walk(soundsDir)) {
                    stream.filter(p -> !p.equals(soundsDir)).sorted(Comparator.reverseOrder()).forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException ignored) {
                        }
                    });
                }
            } else {
                Files.createDirectories(soundsDir);
            }
            for (String sound : getSoundsList()) {
                String fileUrl = "https://raw.githubusercontent.com/meyuugao/HolyModeration-Releases/main/Sounds/%s".formatted(sound);
                Path filePath = soundsDir.resolve(sound);
                URL url = new URL(fileUrl);
                try (InputStream in = url.openStream()) {
                    Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (IOException e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в NetService/downloadSounds: %s%s".formatted(DARK_RED, e), 5f);
        }
    }

    public Map<String, Object> getJournalProfile() {
        return executeGetRequest("me");
    }

    public Map<String, Object> getJournalStats() {
        return executeGetRequest("stats");
    }

    public void startCheckout(String name, String reason, String mode, int number, boolean pvp) {
        try {
            if (hasActiveCheckout()) {
                notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                        "У вас уже есть активная проверка.", 5f);
                return;
            }
            HttpsURLConnection connection = openHttpsConnection("%scheckout/start".formatted(journalApiUrl), "POST", null);
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
                    if (connection.getResponseCode() == 201)
                        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                                "Вы успешно внесли проверку в журнал.", 5f);
                    else
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                                "Ошибка при внесении проверки. Код: %s%s".formatted(RED, connection.getResponseCode()), 5f);
                }
            } finally {
                connection.disconnect();
            }
        } catch (IOException e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в NetService/startCheckout: %s%s".formatted(DARK_RED, e), 5f);
        }
    }

    public void endCheckout(String result, String reason, boolean destroyStash) {
        try {
            if (!hasActiveCheckout()) {
                notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                        "У вас нет активной проверки.", 5f);
                return;
            }
            HttpsURLConnection connection = openHttpsConnection("%scheckout/end".formatted(journalApiUrl), "POST", null);
            if (connection == null) throw new IOException("connection is null");
            try {
                setAuthHeaders(connection);
                JsonObject jsonBody = new JsonObject();
                jsonBody.addProperty("result", result);
                jsonBody.addProperty("banReason", reason);
                jsonBody.addProperty("destroyStash", destroyStash);
                if (writeJson(connection, jsonBody)) {
                    if (connection.getResponseCode() == 201)
                        notificationsService.addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(GREEN, BOLD),
                                "Вы успешно закончили проверку в журнале.", 5f);
                    else
                        notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                                "Ошибка при завершении проверки. Код: %s%s".formatted(RED, connection.getResponseCode()), 5f);
                }
            } finally {
                connection.disconnect();
            }
        } catch (IOException e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в NetService/endCheckout: %s%s".formatted(DARK_RED, e), 5f);
        }
    }

    private boolean hasActiveCheckout() {
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
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в NetService/hasActiveCheckout: %s%s".formatted(DARK_RED, e), 5f);
            return false;
        }
    }

    private Map<String, Object> executeGetRequest(String endpoint) {
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
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в NetService/executeGetRequest: %s%s".formatted(DARK_RED, e), 5f);
            return Collections.emptyMap();
        }
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
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в NetService/writeJson: %s%s".formatted(DARK_RED, e), 5f);
            return false;
        }
    }

    public StringBuilder getResponse(@NotNull HttpsURLConnection connection) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();
            return response;
        } catch (IOException e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в NetService/getResponse: %s%s".formatted(DARK_RED, e), 5f);
            return new StringBuilder();
        }
    }

    private Map<String, Object> parseJsonResponse(@NotNull StringBuilder response) {
        if (response.isEmpty()) {
            return Collections.emptyMap();
        }
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        return gson.fromJson(response.toString(), type);
    }

    public HttpsURLConnection openHttpsConnection(String url, String method, String cookie) throws MalformedURLException {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            if (cookie != null) connection.setRequestProperty("Cookie", cookie);
            return connection;
        } catch (IOException e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в NetService/openHttpsConnection: %s%s".formatted(DARK_RED, e), 5f);
            return null;
        }
    }

    //tip: ёбнуть

    public void sendLaunchData(String hwid, String username) {
        String endpoint = "https://holymoderation.alwaysdata.net/api/launch";

        try {
            HttpsURLConnection connection = openHttpsConnection(endpoint, "POST", null);
            if (connection == null) throw new IOException("Connection is null");

            connection.setRequestMethod("POST");
            connection.setRequestProperty("hwid", hwid);
            connection.setRequestProperty("username", username);
            connection.setDoOutput(false);

            connection.getResponseMessage();
            connection.disconnect();
        } catch (IOException e) {
            notificationsService.addNotification(NotificationType.EXCEPTION,
                    "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Системная ошибка: %s%s".formatted(DARK_RED, e.getMessage()), 5f);
        }
    }
}