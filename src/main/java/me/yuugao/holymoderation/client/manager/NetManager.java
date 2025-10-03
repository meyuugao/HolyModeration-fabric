package me.yuugao.holymoderation.client.manager;

import static me.yuugao.holymoderation.client.util.Colors.*;
import static me.yuugao.holymoderation.client.manager.ChatManager.*;
import static me.yuugao.holymoderation.client.HolyModerationClient.CONFIG;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class NetManager {
    private final static String journalApiPath = "https://journal.holyworld.me/srv/api/v1/";

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
        HttpsURLConnection connection = openHttpsConnection(journalApiPath + "me", "GET", null);
        connection.setRequestProperty("x-token", CONFIG.apiToken);
        connection.setRequestProperty("Content-Type", "application/json");
        return parseJsonResponse(getResponse(connection));
    }

    public static Map<String, Object> getJournalStats() {
        HttpsURLConnection connection = openHttpsConnection(journalApiPath + "stats", "GET", null);
        connection.setRequestProperty("x-token", CONFIG.apiToken);
        connection.setRequestProperty("Content-Type", "application/json");
        return parseJsonResponse(getResponse(connection));
    }

    public static void startCheckout(String name, String reason, String mode, int number, boolean pvp) {
        try {
            if (currectCheckout()) {
                printError("У вас уже есть активная проверка.");
                return;
            }
            HttpsURLConnection connection = openHttpsConnection(journalApiPath + "checkout/start", "POST", null);
            connection.setRequestProperty("x-token", CONFIG.apiToken);
            connection.setRequestProperty("Content-Type", "application/json");
            JsonObject jsonBody = new JsonObject();
            jsonBody.addProperty("username", name);
            jsonBody.addProperty("reason", reason);
            jsonBody.addProperty("mode", mode);
            jsonBody.addProperty("anarchyNumber", number);
            jsonBody.addProperty("isPvpAnarchy", pvp);
            writeJson(connection, jsonBody);
            if (connection.getResponseMessage().equals("Created")) {
                printSuccess("Вы успешно внесли проверку.");
            } else {
                throw new Exception("Ошибка при внесении проверки. Message: " + connection.getResponseMessage());
            }
        } catch (Exception e) {
            printException("Исключение в NetManager/startCheckout: " + e);
        }
    }

    public static void endCheckout(String result, String reason, boolean destroyStash) {
        try {
            if (!currectCheckout()) {
                printError("У вас нет активной проверки.");
                return;
            }
            HttpsURLConnection connection = openHttpsConnection(journalApiPath + "checkout/end", "POST", null);
            connection.setRequestProperty("x-token", CONFIG.apiToken);
            connection.setRequestProperty("Content-Type", "application/json");
            JsonObject jsonBody = new JsonObject();
            jsonBody.addProperty("result", result);
            jsonBody.addProperty("banReason", reason);
            jsonBody.addProperty("destroyStash", destroyStash);
            writeJson(connection, jsonBody);
            if (connection.getResponseCode() == 201) {
                printSuccess("Вы успешно закончили проверку.");
            } else {
                throw new Exception("Ошибка при завершении проверки. Message: " + connection.getResponseMessage());
            }
        } catch (Exception e) {
            printException("Исключение в NetManager/endCheckout: " + e);
        }
    }

    private static boolean currectCheckout() {
        HttpsURLConnection connection = openHttpsConnection(journalApiPath + "checkout/status", "GET", null);
        connection.setRequestProperty("x-token", CONFIG.apiToken);
        connection.setRequestProperty("Content-Type", "application/json");
        return (boolean) parseJsonResponse(getResponse(connection)).get("status");
    }

    private static void writeJson(HttpsURLConnection connection, JsonObject jsonBody) {
        try {
            connection.setDoOutput(true);
            try (OutputStreamWriter out = new OutputStreamWriter(
                    connection.getOutputStream(), StandardCharsets.UTF_8)) {
                out.write(jsonBody.toString());
                out.flush();
            }
        } catch (Exception e) {
            printException("Исключение в NetManager/writeJson: " + e);
        }
    }

    public static StringBuilder getResponse(HttpsURLConnection connection) {
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
        }
        return null;
    }

    private static Map<String, Object> parseJsonResponse(StringBuilder response) {
        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, Object>>() {
        }.getType();
        return gson.fromJson(response.toString(), type);
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
        }
        return null;
    }
}