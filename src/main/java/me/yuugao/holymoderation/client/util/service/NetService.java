package me.yuugao.holymoderation.client.util.service;

import static me.yuugao.holymoderation.client.util.Colors.BOLD;
import static me.yuugao.holymoderation.client.util.Colors.RED;


import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

public class NetService extends Service {
    private final String journalApiPath = "https://journal.holyworld.me/srv/api/v1/";
    private final Gson gson = new Gson();

    public void downloadSound(String sound) {
        String targetDirectory = "C:\\HolyModeration\\Sounds";
        try {
            String fileUrl = "https://raw.githubusercontent.com/meyuugao/HolyModeration-Releases/main/Sounds/" + sound;
            Path filePath = Paths.get(targetDirectory + File.separator + sound);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            Path directoryPath = Paths.get(targetDirectory);
            if (!Files.exists(directoryPath)) {
                Files.createDirectories(directoryPath);
            }

            URL url = new URL(fileUrl);
            try (InputStream in = url.openStream()) {
                Files.copy(in, filePath, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            logger.printException(RED + BOLD + "Исключение в NetService/downloadSound: " + e);
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
                logger.printError("У вас уже есть активная проверка.");
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
                    if (connection.getResponseCode() == 201) {
                        logger.printSuccess("Вы успешно внесли проверку.");
                    } else {
                        logger.printError("Ошибка при внесении проверки. Код: " + connection.getResponseCode());
                    }
                }
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            logger.printException("Исключение в NetService/startCheckout: " + e);
        }
    }

    public void endCheckout(String result, String reason, boolean destroyStash) {
        try {
            if (!hasActiveCheckout()) {
                logger.printError("У вас нет активной проверки.");
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
                    if (connection.getResponseCode() == 201) {
                        logger.printSuccess("Вы успешно закончили проверку.");
                    } else {
                        logger.printError("Ошибка при завершении проверки. Код: " + connection.getResponseCode());
                    }
                }
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            logger.printException("Исключение в NetService/endCheckout: " + e);
        }
    }

    private boolean hasActiveCheckout() {
        try {
            HttpsURLConnection connection = openHttpsConnection(journalApiPath + "checkout/status", "GET", null);
            if (connection == null) throw new IOException("connection is null");

            try {
                setAuthHeaders(connection);
                StringBuilder response = getResponse(connection);
                if (response == null) throw new IOException("response is null");

                Map<String, Object> jsonResponse = parseJsonResponse(response);
                Object status = jsonResponse.get("status");
                return status instanceof Boolean && (Boolean) status;
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            logger.printException("Исключение в NetService/hasActiveCheckout: " + e.getMessage());
            return false;
        }
    }

    private Map<String, Object> executeGetRequest(String endpoint) {
        try {
            HttpsURLConnection connection = openHttpsConnection(journalApiPath + endpoint, "GET", null);
            if (connection == null) throw new IOException("connection is null");

            try {
                setAuthHeaders(connection);
                StringBuilder response = getResponse(connection);
                if (response == null) throw new IOException("response is null");

                return parseJsonResponse(response);
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            logger.printException("Исключение в NetService/executeGetRequest: " + e);
            return Collections.emptyMap();
        }
    }

    private void setAuthHeaders(@NotNull HttpsURLConnection connection) {
        connection.setRequestProperty("x-token", ServiceLocator.getConfigManager().getConfig().apiToken);
        connection.setRequestProperty("Content-Type", "application/json");
    }

    private boolean writeJson(@NotNull HttpsURLConnection connection, @NotNull JsonObject jsonBody) {
        try {
            connection.setDoOutput(true);
            try (OutputStreamWriter out = new OutputStreamWriter(
                    connection.getOutputStream(), StandardCharsets.UTF_8)) {
                out.write(jsonBody.toString());
                out.flush();
            }

            return true;
        } catch (Exception e) {
            logger.printException("Исключение в NetService/writeJson: " + e);
            return false;
        }
    }

    public StringBuilder getResponse(@NotNull HttpsURLConnection connection) {
        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                response.append(line);
            }
            bufferedReader.close();

            return response;
        } catch (Exception e) {
            logger.printException("Исключение в NetService/getResponse: " + e);
            return null;
        }
    }

    private Map<String, Object> parseJsonResponse(@NotNull StringBuilder response) {
        try {
            Type type = new TypeToken<Map<String, Object>>() {
            }.getType();

            return gson.fromJson(response.toString(), type);
        } catch (Exception e) {
            logger.printException("Исключение в NetService/parseJsonResponse: " + e);
            return Collections.emptyMap();
        }
    }

    public HttpsURLConnection openHttpsConnection(String url, String method, String cookie) {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();

            connection.setRequestMethod(method);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.6613.120 Safari/537.36");
            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7");
            if (cookie != null) {
                connection.setRequestProperty("Cookie", cookie);
            }

            return connection;
        } catch (Exception e) {
            logger.printException("Исключение в NetService/openHttpsConnection: " + e);
            return null;
        }
    }
}