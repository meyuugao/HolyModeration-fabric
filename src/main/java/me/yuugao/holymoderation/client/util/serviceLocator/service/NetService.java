package me.yuugao.holymoderation.client.util.serviceLocator.service;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

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
            ServiceLocator.getNotificationService().addNotification(NotificationType.EXCEPTION, DARK_RED + BOLD + "Исключение", "Исключение в NetService/getSoundsList: " + DARK_RED + e, 5f);
        }
        return soundFiles;
    }

    public void downloadSounds() {
        try {
            Path soundsDir = Paths.get("C:\\HolyModeration\\Sounds");

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
                String fileUrl = "https://raw.githubusercontent.com/meyuugao/HolyModeration-Releases/main/Sounds/" + sound;
                Path filePath = soundsDir.resolve(sound);
                URL url = new URL(fileUrl);
                try (InputStream in = url.openStream()) {
                    Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (Exception e) {
            ServiceLocator.getNotificationService().addNotification(
                    NotificationType.EXCEPTION,
                    DARK_RED + BOLD + "Исключение",
                    "Исключение в NetService/downloadSounds: " + DARK_RED + e,
                    5f,
                    StringUtils.EMPTY
            );
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
                ServiceLocator.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "У вас уже есть активная проверка.", 5f);
                return;
            }

            HttpsURLConnection connection = openHttpsConnection(journalApiPath + "checkout/start", "POST", null);
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
                        ServiceLocator.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Вы успешно внесли проверку.", 5f);
                    else
                        ServiceLocator.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Ошибка при внесении проверки. Код: " + RED + connection.getResponseCode(), 5f);
                }
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            ServiceLocator.getNotificationService().addNotification(NotificationType.EXCEPTION, DARK_RED + BOLD + "Исключение", "Исключение в NetService/startCheckout: " + DARK_RED + e, 5f);
        }
    }

    public void endCheckout(String result, String reason, boolean destroyStash) {
        try {
            if (!hasActiveCheckout()) {
                ServiceLocator.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "У вас нет активной проверки.", 5f);
                return;
            }

            HttpsURLConnection connection = openHttpsConnection(journalApiPath + "checkout/end", "POST", null);
            if (connection == null) throw new IOException("connection is null");

            try {
                setAuthHeaders(connection);
                JsonObject jsonBody = new JsonObject();
                jsonBody.addProperty("result", result);
                jsonBody.addProperty("banReason", reason);
                jsonBody.addProperty("destroyStash", destroyStash);

                if (writeJson(connection, jsonBody)) {
                    if (connection.getResponseCode() == 201)
                        ServiceLocator.getNotificationService().addNotification(NotificationType.SUCCESS, GREEN + BOLD + "Успех", "Вы успешно закончили проверку.", 5f);
                    else
                        ServiceLocator.getNotificationService().addNotification(NotificationType.ERROR, RED + BOLD + "Ошибка", "Ошибка при завершении проверки. Код: " + RED + connection.getResponseCode(), 5f);
                }
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            ServiceLocator.getNotificationService().addNotification(NotificationType.EXCEPTION, DARK_RED + BOLD + "Исключение", "Исключение в NetService/endCheckout: " + DARK_RED + e, 5f);
        }
    }

    private boolean hasActiveCheckout() {
        try {
            HttpsURLConnection connection = openHttpsConnection(journalApiPath + "checkout/status", "GET", null);
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
            ServiceLocator.getNotificationService().addNotification(NotificationType.EXCEPTION, DARK_RED + BOLD + "Исключение", "Исключение в NetService/hasActiveCheckout: " + DARK_RED + e, 5f);
            return false;
        }
    }

    private Map<String, Object> executeGetRequest(String endpoint) {
        try {
            HttpsURLConnection connection = openHttpsConnection(journalApiPath + endpoint, "GET", null);
            if (connection == null) throw new IOException("connection is null");
            try {
                setAuthHeaders(connection);
                return parseJsonResponse(getResponse(connection));
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            ServiceLocator.getNotificationService().addNotification(NotificationType.EXCEPTION, DARK_RED + BOLD + "Исключение", "Исключение в NetService/executeGetRequest: " + DARK_RED + e, 5f);
            return Collections.emptyMap();
        }
    }

    private void setAuthHeaders(@NotNull HttpsURLConnection connection) {
        connection.setRequestProperty("x-token", ServiceLocator.getConfigManager().getConfig().getApiToken());
        connection.setRequestProperty("Content-Type", "application/json");
    }

    private boolean writeJson(@NotNull HttpsURLConnection connection, @NotNull JsonObject jsonBody) {
        try {
            connection.setDoOutput(true);
            try (OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8)) {
                out.write(jsonBody.toString());
            }
            return true;
        } catch (Exception e) {
            ServiceLocator.getNotificationService().addNotification(NotificationType.EXCEPTION, DARK_RED + BOLD + "Исключение", "Исключение в NetService/writeJson: " + DARK_RED + e, 5f);
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
        } catch (Exception e) {
            ServiceLocator.getNotificationService().addNotification(NotificationType.EXCEPTION, DARK_RED + BOLD + "Исключение", "Исключение в NetService/getResponse: " + DARK_RED + e, 5f);
            return null;
        }
    }

    private Map<String, Object> parseJsonResponse(@NotNull StringBuilder response) {
        try {
            Type type = new TypeToken<Map<String, Object>>() {
            }.getType();
            return gson.fromJson(response.toString(), type);
        } catch (Exception e) {
            ServiceLocator.getNotificationService().addNotification(NotificationType.EXCEPTION, DARK_RED + BOLD + "Исключение", "Исключение в NetService/parseJsonResponse: " + DARK_RED + e, 5f);
            return Collections.emptyMap();
        }
    }

    public HttpsURLConnection openHttpsConnection(String url, String method, String cookie) {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setRequestMethod(method);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            if (cookie != null) connection.setRequestProperty("Cookie", cookie);
            return connection;
        } catch (Exception e) {
            ServiceLocator.getNotificationService().addNotification(NotificationType.EXCEPTION, DARK_RED + BOLD + "Исключение", "Исключение в NetService/openHttpsConnection: " + DARK_RED + e, 5f);
            return null;
        }
    }
}