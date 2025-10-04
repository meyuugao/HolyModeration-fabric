package me.yuugao.holymoderation.client.config;

import static me.yuugao.holymoderation.client.manager.ChatManager.printException;


import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ConfigManager {
    private static final String CONFIG_DIRECTORY = "C:\\HolyModeration\\Config";
    private static final String CONFIG_FILE_PATH = CONFIG_DIRECTORY + "\\config_labymod3.json";

    private final Gson gson;
    private Config config;

    public ConfigManager() {
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        config = new Config();
        createConfigDirectory();
        loadConfig();
    }

    private void createConfigDirectory() {
        File directory = new File(CONFIG_DIRECTORY);
        if (!directory.exists()) {
            directory.mkdirs();
        }
    }

    public void loadConfig() {
        File configFile = new File(CONFIG_FILE_PATH);
        if (configFile.exists()) {
            try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(configFile.toPath()), StandardCharsets.UTF_8)) {
                config = gson.fromJson(reader, Config.class);
            } catch (Exception e) {
                printException("Исключение в ConfigManager/loadConfig: " + e);
            }
        } else {
            saveCfg(config);
        }
    }

    public void saveCfg(Config config) {
        this.config = config;
        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(Paths.get(CONFIG_FILE_PATH)), StandardCharsets.UTF_8)) {
            gson.toJson(this.config, writer);
        } catch (Exception e) {
            printException("Исключение в ConfigManager/saveCfg: " + e);
        }
    }

    public Config getConfig() {
        return this.config;
    }
}
