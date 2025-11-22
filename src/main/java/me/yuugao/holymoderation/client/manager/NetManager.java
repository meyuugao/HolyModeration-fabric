package me.yuugao.holymoderation.client.manager;

import static me.yuugao.holymoderation.client.manager.ChatManager.*;
import static me.yuugao.holymoderation.client.util.Colors.BOLD;
import static me.yuugao.holymoderation.client.util.Colors.RED;


import me.yuugao.holymoderation.client.util.service.ServiceLocator;

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

public class NetManager {
    private final static String journalApiPath = "https://journal.holyworld.me/srv/api/v1/";
    private final static Gson gson = new Gson();

    public static void downloadSound(String sound) {
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
            clientMessage(RED + BOLD + "Исключение в NetManager/downloadSound: " + e);
        }
    }

    public static Map<String, Object> getJournalProfile() {
        return executeGetRequest("me");
    }

    public static Map<String, Object> getJournalStats() {
        return executeGetRequest("stats");
    }

    public static void startCheckout(String name, String reason, String mode, int number, boolean pvp) {
        try {
            if (hasActiveCheckout()) {
                printError("У вас уже есть активная проверка.");
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
                        printSuccess("Вы успешно внесли проверку.");
                    } else {
                        printError("Ошибка при внесении проверки. Код: " + connection.getResponseCode());
                    }
                }
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            printException("Исключение в NetManager/startCheckout: " + e);
        }
    }

    public static void endCheckout(String result, String reason, boolean destroyStash) {
        try {
            if (!hasActiveCheckout()) {
                printError("У вас нет активной проверки.");
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
                        printSuccess("Вы успешно закончили проверку.");
                    } else {
                        printError("Ошибка при завершении проверки. Код: " + connection.getResponseCode());
                    }
                }
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            printException("Исключение в NetManager/endCheckout: " + e);
        }
    }

    private static boolean hasActiveCheckout() {
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
            printException("Исключение в NetManager/hasActiveCheckout: " + e.getMessage());
            return false;
        }
    }

    private static Map<String, Object> executeGetRequest(String endpoint) {
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
            printException("Исключение в NetManager/executeGetRequest: " + e);
            return Collections.emptyMap();
        }
    }

    private static void setAuthHeaders(@NotNull HttpsURLConnection connection) {
        connection.setRequestProperty("x-token", ServiceLocator.getConfigManager().getConfig().apiToken);
        connection.setRequestProperty("Content-Type", "application/json");
    }

    private static boolean writeJson(@NotNull HttpsURLConnection connection, @NotNull JsonObject jsonBody) {
        try {
            connection.setDoOutput(true);
            try (OutputStreamWriter out = new OutputStreamWriter(
                    connection.getOutputStream(), StandardCharsets.UTF_8)) {
                out.write(jsonBody.toString());
                out.flush();
            }

            return true;
        } catch (Exception e) {
            printException("Исключение в NetManager/writeJson: " + e);
            return false;
        }
    }

    public static StringBuilder getResponse(@NotNull HttpsURLConnection connection) {
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
            printException("Исключение в NetManager/getResponse: " + e);
            return null;
        }
    }

    private static Map<String, Object> parseJsonResponse(@NotNull StringBuilder response) {
        try {
            Type type = new TypeToken<Map<String, Object>>() {
            }.getType();

            return gson.fromJson(response.toString(), type);
        } catch (Exception e) {
            printException("Исключение в NetManager/parseJsonResponse: " + e);
            return Collections.emptyMap();
        }
    }

    public static HttpsURLConnection openHttpsConnection(String url, String method, String cookie) {
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
            printException("Исключение в NetManager/openHttpsConnection: " + e);
            return null;
        }
    }
}