package me.yuugao.holymoderation.client.config;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Getter;

public class ConfigManager {
    private static final String CONFIG_DIRECTORY = "C:\\HolyModeration\\Config";
    private static final String CONFIG_FILE_PATH = CONFIG_DIRECTORY + "\\config.json";

    private final Gson gson;
    @Getter
    private Config config;

    public ConfigManager() {
        gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        config = new Config();
        ensureConfigDirectory();
        loadConfig();
    }

    private void ensureConfigDirectory() {
        try {
            Files.createDirectories(Paths.get(CONFIG_DIRECTORY));
        } catch (Exception e) {
            ServiceLocator.getLoggerService().printException("Исключение в ConfigManager/ensureConfigDirectory: " + e);
        }
    }

    public void loadConfig() {
        ensureConfigDirectory();
        Path configPath = Paths.get(CONFIG_FILE_PATH);

        if (Files.exists(configPath)) {
            try (InputStreamReader reader = new InputStreamReader(Files.newInputStream(configPath), StandardCharsets.UTF_8)) {
                Config loaded = gson.fromJson(reader, Config.class);
                if (loaded != null) {
                    config = loaded;
                } else {
                    saveCfg(config);
                }
            } catch (Exception e) {
                ServiceLocator.getLoggerService().printException("Исключение в ConfigManager/loadConfig: " + e);
                saveCfg(config);
            }
        } else {
            saveCfg(config);
        }
    }

    public void saveCfg(Config config) {
        this.config = config;
        ensureConfigDirectory();

        try (OutputStreamWriter writer = new OutputStreamWriter(
                Files.newOutputStream(Paths.get(CONFIG_FILE_PATH)),
                StandardCharsets.UTF_8
        )) {
            gson.toJson(this.config, writer);
        } catch (Exception e) {
            ServiceLocator.getLoggerService().printException("Исключение в ConfigManager/saveCfg: " + e);
        }
    }
}