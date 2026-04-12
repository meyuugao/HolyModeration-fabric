package me.yuugao.holymoderation.client.util.serviceLocator.service.impl;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.config.manager.ConfigManager;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;
import me.yuugao.holymoderation.client.util.serviceLocator.service.Service;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class NetService extends Service {
    private final String journalApiPath = "https://journal.holyworld.me/srv/api/v1/";
    private final Gson gson = new Gson();

    public AbstractMap.SimpleEntry<String, String> getLastUpdates() {
        HttpsURLConnection connection = openHttpsConnection("https://raw.githubusercontent.com/Gr0wMan/HolyModeration-Releases/main/LATEST.txt", "GET", null);
        String response = getResponse(connection).toString();
        return new AbstractMap.SimpleEntry<>(response.split("%%%")[0], response.split("%%%")[1]);
    }

    public List<String> getSoundsList() {
        NotificationsService notificationsService = ServiceLocator.getNotificationsService();

        List<String> soundFiles = new ArrayList<>();
        try {
            String url = "https://github.com/meyuugao/HolyModeration-Releases/tree/main/Sounds";
            HttpsURLConnection connection = openHttpsConnection(url, "GET", null);
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
        } catch (Exception e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в NetService/getSoundsList: %s%s".formatted(DARK_RED, e), 5f);
        }
        return soundFiles;
    }

    public void downloadSounds() {
        SoundService soundService = ServiceLocator.getSoundService();
        NotificationsService notificationsService = ServiceLocator.getNotificationsService();

        try {
            Path soundsDir = soundService.getSoundsDir();

            if (Files.exists(soundsDir)) {
                try (var stream = Files.walk(soundsDir)) {
                    stream
                            .filter(p -> !p.equals(soundsDir))
                            .sorted(Comparator.reverseOrder())
                            .forEach(p -> {
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
        } catch (Exception e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в NetService/downloadSounds: %s%s".formatted(DARK_RED, e), 5f, StringUtils.EMPTY);
        }
    }

    public Map<String, Object> getJournalProfile() {
        return executeGetRequest("me");
    }

    public Map<String, Object> getJournalStats() {
        return executeGetRequest("stats");
    }

    public void startCheckout(String name, String reason, String mode, int number, boolean pvp) {
        NotificationsService notificationsService = ServiceLocator.getNotificationsService();

        try {
            if (hasActiveCheckout()) {
                notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                        "У вас уже есть активная проверка.", 5f);
                return;
            }

            HttpsURLConnection connection = openHttpsConnection(
                    "%scheckout/start".formatted(journalApiPath), "POST", null);
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
        } catch (Exception e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в NetService/startCheckout: %s%s".formatted(DARK_RED, e), 5f);
        }
    }

    public void endCheckout(String result, String reason, boolean destroyStash) {
        NotificationsService notificationsService = ServiceLocator.getNotificationsService();

        try {
            if (!hasActiveCheckout()) {
                notificationsService.addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(RED, BOLD),
                        "У вас нет активной проверки.", 5f);
                return;
            }

            HttpsURLConnection connection = openHttpsConnection(
                    "%scheckout/end".formatted(journalApiPath), "POST", null);
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
        } catch (Exception e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в NetService/endCheckout: %s%s".formatted(DARK_RED, e), 5f);
        }
    }

    private boolean hasActiveCheckout() {
        NotificationsService notificationsService = ServiceLocator.getNotificationsService();

        try {
            HttpsURLConnection connection = openHttpsConnection(
                    "%scheckout/status".formatted(journalApiPath), "GET", null);
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
        } catch (Exception e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в NetService/hasActiveCheckout: %s%s".formatted(DARK_RED, e), 5f);
            return false;
        }
    }

    private Map<String, Object> executeGetRequest(String endpoint) {
        NotificationsService notificationsService = ServiceLocator.getNotificationsService();

        try {
            HttpsURLConnection connection = openHttpsConnection(
                    "%s%s".formatted(journalApiPath, endpoint), "GET", null);
            if (connection == null) throw new IOException("connection is null");
            try {
                setAuthHeaders(connection);
                return parseJsonResponse(getResponse(connection));
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в NetService/executeGetRequest: %s%s".formatted(DARK_RED, e), 5f);
            return Collections.emptyMap();
        }
    }

    private void setAuthHeaders(@NotNull HttpsURLConnection connection) {
        ConfigManager configManager = ServiceLocator.getConfigManager();

        connection.setRequestProperty("x-token", configManager.getApiConfig().getApiToken());
        connection.setRequestProperty("Content-Type", "application/json");
    }

    private boolean writeJson(@NotNull HttpsURLConnection connection, @NotNull JsonObject jsonBody) {
        NotificationsService notificationsService = ServiceLocator.getNotificationsService();

        try {
            connection.setDoOutput(true);
            try (OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
                out.write(jsonBody.toString());
            }
            return true;
        } catch (Exception e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в NetService/writeJson: %s%s".formatted(DARK_RED, e), 5f);
            return false;
        }
    }

    public StringBuilder getResponse(@NotNull HttpsURLConnection connection) {
        NotificationsService notificationsService = ServiceLocator.getNotificationsService();

        try {
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) response.append(line);
            reader.close();
            return response;
        } catch (Exception e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в NetService/getResponse: %s%s".formatted(DARK_RED, e), 5f);
            return null;
        }
    }

    private Map<String, Object> parseJsonResponse(@NotNull StringBuilder response) {
        NotificationsService notificationsService = ServiceLocator.getNotificationsService();

        try {
            Type type = new TypeToken<Map<String, Object>>() {
            }.getType();
            return gson.fromJson(response.toString(), type);
        } catch (Exception e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в NetService/parseJsonResponse: %s%s".formatted(DARK_RED, e), 5f);
            return Collections.emptyMap();
        }
    }

    public HttpsURLConnection openHttpsConnection(String url, String method, String cookie) {
        NotificationsService notificationsService = ServiceLocator.getNotificationsService();

        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            if (cookie != null) connection.setRequestProperty("Cookie", cookie);
            return connection;
        } catch (Exception e) {
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в NetService/openHttpsConnection: %s%s".formatted(DARK_RED, e), 5f);
            return null;
        }
    }
}