package me.yuugao.holymoderation.client.util.service.config;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.service.LoggerService;
import me.yuugao.holymoderation.client.util.service.config.impl.ApiConfig;
import me.yuugao.holymoderation.client.util.service.config.impl.GuiConfig;
import me.yuugao.holymoderation.client.util.service.config.impl.KeyBindsConfig;
import me.yuugao.holymoderation.client.util.service.config.impl.SettingsConfig;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.stream.JsonReader;
import lombok.AccessLevel;
import lombok.Getter;

@Getter
@Singleton
public class ConfigManagerService {
    @Getter(AccessLevel.NONE)
    private final Path configsDir = Paths.get(System.getProperty("user.home"), "HolyModeration", "Config");

    private final ApiConfig apiConfig;
    private final GuiConfig guiConfig;
    private final KeyBindsConfig keyBindsConfig;
    private final SettingsConfig settingsConfig;
    @Getter(AccessLevel.NONE)
    private final LoggerService loggerService;

    @Inject
    public ConfigManagerService(LoggerService loggerService) {
        this.loggerService = loggerService;

        ensureConfigDirectory();
        apiConfig = loadOrCreate(new ApiConfig());
        guiConfig = loadOrCreate(new GuiConfig());
        keyBindsConfig = loadOrCreate(new KeyBindsConfig());
        settingsConfig = loadOrCreate(new SettingsConfig());
    }

    private void ensureConfigDirectory() {
        try {
            if (!Files.exists(configsDir)) {
                Files.createDirectories(configsDir);
            }
        } catch (IOException e) {
            loggerService.exception("Исключение в ConfigManager/ensureConfigDirectory: %s".formatted(e));
        }
    }

    private <T extends Config> T loadOrCreate(T defaultConfig) {
        Path configPath = configsDir.resolve("%sConfig.json".formatted(defaultConfig.getConfigName()));

        if (!Files.exists(configPath)) {
            saveConfig(defaultConfig);
            return defaultConfig;
        }

        try {
            String raw = Files.readString(configPath, StandardCharsets.UTF_8);
            try (JsonReader jr = new JsonReader(new StringReader(raw))) {
                jr.setLenient(true);
                T loaded = defaultConfig.getGson().fromJson(jr, defaultConfig.getClass());
                return loaded != null ? loaded : defaultConfig;
            }
        } catch (IOException e) {
            loggerService.exception("Исключение в ConfigManager/loadOrCreate: %s".formatted(e));
            saveConfig(defaultConfig);
            return defaultConfig;
        }
    }

    public void saveConfig(Config config) {
        Path configPath = configsDir.resolve("%sConfig.json".formatted(config.getConfigName()));

        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(configPath), StandardCharsets.UTF_8)) {
            String HEADER = """
                    /*
                    ********************************************************************************
                    **                              ВНИМАНИЕ!                                     **
                    **  Создатель мода не несёт ответственности за любые изменения конфигурации   **
                    **  вручную, за передачу конфигурационных файлов другим лицам, включая API    **
                    **  токен, и возможные последствия использования модифицированного файла.     **
                    ********************************************************************************
                    */
                    
                    """;
            writer.write(HEADER);

            config.getGson().toJson(config, writer);

            String FOOTER = """
                    \n
                    ⠢⠡⠂⠠⠀⠀⠀⣀⣤⣶⣶⣷⣾⣶⣷⣾⣶⣷⣾⣶⣷⣾⣶⣷⣾⣶⣷⣾⣶⣷⣾⣶⣷⣷⣾⣾⣶⡷⠷⠓⠓⠑⠁⠂⠀⠀⡀⠀⡀⢀⠀⢀⠀⠀⠀⠀⠀⠈⠘⠚⠚⠾⠾⡾⣾⣾⣾⣾⣾⣾⣾⣾⣾⣾⣾⣾⣾⣾⣾⣾⣾⣾⣾⣾⣾⣾⣾⣾⣾⣶⣶⣤⣄⠀⠀⠀⠀⡐⢈⠢
                    ⡁⠌⠀⠀⢀⣴⣾⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠿⠛⠉⠀⣀⡠⡤⡴⣴⡺⡮⣯⣻⡺⡯⡯⣯⣻⢽⢽⣫⢿⢽⣺⣺⣲⢴⢤⣄⢄⡀⡈⠉⠛⠿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⣦⡀⠀⠀⠐⠈
                    ⠀⠀⠀⣠⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠟⠁⢀⢤⢲⣳⡳⡩⡹⡘⣕⡯⣟⣞⡾⡽⡽⣝⢷⢽⢽⢽⣺⢽⢽⣺⣺⢽⣝⢷⢽⢽⣽⣺⣲⢤⡀⠈⠛⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣆⠀⠀⠐
                    ⠀⢀⣼⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡟⠁⢀⢔⢕⢱⠱⣸⣞⢮⡺⡪⡳⡫⡳⡱⡹⡸⡍⡎⡇⡗⡕⡇⡇⡏⣎⢮⢪⢣⢫⢝⢝⢗⢿⣿⣿⣿⣾⡢⡀⠀⢻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣷⠀⠀
                    ⠀⣼⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠏⠀⡠⡣⡱⡸⡰⡹⡸⡔⡇⡇⡏⡮⡪⡎⣎⢇⢧⢓⢝⢜⢜⢎⢮⢪⢣⢣⡣⡣⡳⡱⡕⡇⡗⣕⢝⠿⡿⣿⣳⢱⠄⠀⣻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣧⠀
                    ⠰⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣟⠀⢐⢵⢱⢕⢕⡕⡝⣜⢜⢜⢎⢞⢜⢜⡜⡜⣜⢜⢎⢇⢇⢗⢕⢕⢕⢕⢇⢇⢏⢎⢮⢪⡪⡎⡮⡪⡣⡫⣪⢪⢪⣓⠀⢘⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡇
                    ⢸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡇⠀⡪⡪⡎⡎⡇⡇⡏⡎⡎⡇⡗⡕⡵⡱⡱⡭⣒⢕⢕⢕⢕⢕⢕⢕⢕⢕⡵⡱⡱⡱⡱⡱⡱⡱⡕⡵⡹⡸⡜⡜⡎⡎⡄⠀⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡧
                    ⢸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠂⠀⡇⡗⡕⣝⢜⢎⢇⢏⢮⢺⢸⡸⡸⡜⡎⡮⣳⡪⡘⠌⡎⡪⡊⠊⡴⣝⢎⢇⢏⢎⢞⢜⢎⢇⢇⢧⢳⢱⢕⢇⢗⢕⠅⠀⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡯
                    ⢹⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠀⢐⢝⢜⢎⢎⢮⢪⢎⢇⢇⢇⢃⢎⢮⢪⠊⠊⡎⡯⣖⠄⠑⠌⢀⡾⡝⡎⡊⠪⡣⡳⡱⡑⢕⢕⢝⢜⡜⣜⢜⡜⡎⡎⡇⠀⢽⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡯
                    ⢼⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠀⠰⡱⡣⡳⡹⡸⡸⡜⡜⡕⡁⡖⡕⠕⠁⡠⢂⠈⢪⢎⢇⠀⡀⢸⢸⠱⠁⠠⡀⠈⠪⡪⡲⡀⢳⢹⢸⡸⡜⣜⢜⢜⢎⠆⠀⢽⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡯
                    ⢸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣗⠀⢘⢜⢎⢞⢜⢎⢇⢗⢝⢔⡕⠕⠁⡠⡑⢌⠢⠢⠀⠱⡱⢐⠠⢪⠃⠁⡐⡅⢕⢡⠠⠈⠪⡹⡔⡕⡇⣇⢇⡇⡗⡝⡜⡅⠀⣹⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡯
                    ⢹⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡧⠀⢸⢸⢱⢱⢣⢳⢱⠣⠃⠃⠠⡐⢌⠢⡊⡢⡑⡑⢅⠠⠨⢐⠨⢐⠀⢘⠔⢌⠢⢢⢑⠅⡄⡀⠁⠋⠎⡎⡎⣎⢮⢺⢸⢊⠀⢼⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡯
                    ⢼⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠟⠿⢿⣿⣿⣿⣿⣿⣿⣇⠀⠘⠜⠑⡑⢁⠁⢄⠄⡢⢡⢑⢌⠢⡑⢔⢂⠪⠨⠀⢄⠕⡰⢑⠔⡐⡀⠡⢅⠕⡡⠢⡑⢔⢘⢌⠢⡠⠠⡈⠐⠑⠑⠕⠕⠀⢼⣿⣿⣿⣿⣿⣿⠿⠻⠻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡯
                    ⢸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠁⢠⢠⠀⠉⠻⢿⣿⣿⣿⣗⠀⠠⢡⠢⠢⡡⡑⢅⠪⡨⠢⡑⣐⢑⢌⠢⡡⠑⢠⢄⠐⡑⡌⡢⡑⠅⢀⠄⠂⠕⢌⠪⠨⡂⢕⢐⢑⢌⠪⡨⢊⠜⡐⠔⠄⠀⢸⣿⣿⣿⡿⠋⠁⡠⣠⠀⠸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣯
                    ⣸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠀⠸⡸⣩⢲⢠⠈⠙⢿⣿⣯⠀⠀⠑⢌⠪⡐⢌⠢⡑⢌⢌⠢⠢⡑⡐⠅⠂⠀⢻⡷⡄⠨⡂⢎⢌⠀⣾⡟⠀⠈⠂⠕⡑⢌⠢⡑⢌⠢⡑⢌⠢⡑⢜⠈⡀⠀⣸⣿⠟⢁⠠⡰⡱⡕⡕⠅⠨⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣗
                    ⣸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡀⠈⣎⢎⢮⢪⢎⢆⡀⠙⢿⠀⠐⡄⠁⡑⠌⠢⠑⠌⠂⠂⠁⠁⠀⠀⠀⠀⠀⠐⣿⡇⢀⢣⠱⡐⢐⣯⠇⠀⠀⠀⠀⠀⠀⠁⠊⠂⠑⠌⠂⠕⠈⠂⢠⠢⠀⠚⢁⢠⢢⡫⡪⡎⡮⡚⠀⢸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣗
                    ⢸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣇⠀⠸⡸⡸⡸⡸⡜⣜⢤⢀⠀⢈⢎⠀⣖⣶⡆⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⢨⣿⡝⠀⡎⡜⢄⠨⣿⡅⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⠀⣰⣶⣵⠂⢸⡑⠀⡐⡴⡱⡕⡕⡕⡕⣕⠁⢀⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣗
                    ⢸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡂⠀⢣⢣⢣⢪⠪⡪⡪⣪⠀⠀⡧⡁⠺⣯⣿⣤⣶⣾⣶⠄⠀⠀⠀⠀⠀⢠⣾⢷⠇⢸⢨⢪⢢⠈⣿⣽⡄⠀⠀⠀⠀⠀⠠⣾⣾⣶⣤⡾⣷⡟⢀⢕⡊⠀⢜⢜⢜⢜⢜⢌⢎⠆⠀⣼⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡯
                    ⢸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡀⠈⠪⠊⠆⠣⠃⠇⢇⠂⠀⡇⣇⠂⠻⣷⣿⣿⣿⡟⠁⢀⠀⡀⣄⣮⡿⣾⠟⢀⢇⢇⢇⢇⢆⠘⡷⣿⣧⣄⡀⡀⢀⠈⢻⣿⣿⡿⣿⡽⠁⡔⣕⠅⠀⠪⠪⠊⠢⠃⠕⠕⠀⢰⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣟
                    ⢸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⠟⠋⠃⠀⣀⡄⣄⡤⡤⡤⣄⣄⣀⠀⠀⠱⠱⡕⡄⠑⠿⣽⡷⣿⢿⡷⣿⣻⣯⠷⠛⢃⢠⠬⣸⢸⢜⢜⡔⣄⠌⠛⠺⣯⣿⣻⣽⣟⣯⣷⣿⡻⠋⡀⣆⠇⠇⠁⢀⢠⡠⡤⡤⣤⢤⣠⣠⣀⠀⠈⠙⠻⠿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡷
                    ⢸⣿⣿⣿⣿⣿⣿⣿⣿⡿⠟⠋⠁⠀⣀⡴⣪⡯⣗⡯⣗⣯⢯⢯⣗⣗⡯⡷⣕⡄⡀⠑⢍⢇⢆⡈⡙⠛⠻⠻⠫⠛⠈⡠⡪⣪⢪⢺⢸⡸⡸⡱⡱⡱⡍⡧⣅⡈⠚⠫⠻⠝⠏⠓⡁⡄⣕⢕⠡⠀⣄⡮⡾⡽⡽⡽⣽⣳⣻⣺⣺⣺⢽⣳⣢⢄⠀⠈⠙⠻⢿⣿⣿⣿⣿⣿⣿⣿⣿⣯
                    ⣸⣿⣿⣿⣿⣿⡿⠟⠉⠀⣀⢆⢐⢼⡺⡝⡗⡝⣕⢝⢼⢸⢩⡣⡳⡳⣫⢯⣗⣟⢦⡀⠀⠣⡣⣣⢣⡣⣣⠲⠁⢠⢪⢎⣗⠵⣝⢮⡣⡧⣫⡪⡎⣇⠧⣓⣦⡳⡄⡀⢣⢪⡪⡪⡎⡮⡊⠀⢀⡜⣞⡾⡽⡽⢝⢝⢎⢖⢕⢎⢎⢎⢏⢞⢞⢽⢳⣀⢢⢀⠀⠈⠻⢻⣿⣿⣿⣿⣿⣗
                    ⢸⣿⣿⣿⠟⠉⠀⡠⡰⣱⢱⡱⡕⡇⡇⡗⡝⣜⢜⡜⡎⡎⡇⣇⢗⢕⡕⣕⢕⢕⡕⣎⡂⠀⠱⡱⣱⢱⡱⡡⠐⡕⢅⢗⠅⣕⢵⢳⡹⡺⡪⡞⣞⡜⣎⢞⢞⡾⣕⢆⠀⢣⢣⢳⢱⠑⠀⢠⢣⡣⡇⡗⣕⢵⢹⢸⢪⢺⢸⢪⢣⢫⢪⢣⢫⡪⣪⢲⢱⢍⢇⢇⡄⡀⠉⠻⢿⣿⣿⣗
                    ⢸⡿⠋⢀⢀⢆⢧⢳⢹⢸⢸⡸⡸⡜⡎⡞⣜⢜⡜⣜⢜⢎⢇⢇⢧⢣⡣⡣⡳⡱⣱⢱⡱⡀⠀⢝⢜⢜⢜⠄⢘⢌⢎⠇⡰⣳⠑⣕⣏⢯⢝⡞⡮⡺⣕⢧⢫⢪⢣⠣⠀⡕⡕⡇⡗⠁⢀⢇⢧⢣⡣⡳⡱⡱⡕⡇⡗⡕⡇⡗⡕⡇⡏⡎⡇⣇⢇⢧⢣⢳⢹⢸⡸⡜⣔⡀⠈⠙⠿⡯
                    ⠈⠀⡠⡲⡱⡕⡵⡱⡕⡇⡇⡣⡳⡱⡕⡵⡱⣱⢱⡱⡕⡵⡹⡸⡪⡪⡎⡮⢣⢫⢪⡪⡪⡆⠀⠸⡸⡱⡕⡕⡀⠱⢍⢇⢮⡳⢨⡪⡞⡮⡳⣝⢮⡫⡞⡎⣎⢇⠇⢁⢰⢱⡹⡸⡂⠀⢰⢣⡣⡇⡧⡓⡝⣜⢼⢸⢪⢺⢸⢪⡪⡺⡸⡱⡹⡸⡸⡪⡪⢱⢣⡣⡇⣇⢧⢪⡣⣂⠀⠉
                    ⢠⢪⡪⡺⡸⡪⡪⡎⡎⡎⢎⠄⠑⠕⡵⡱⡉⢎⢎⡎⡮⣪⠪⡣⡫⡪⡎⡞⡔⠈⡇⣇⢧⢃⠀⢨⢺⢸⢪⡪⡲⡠⡀⠑⠑⠹⠸⡸⡹⡸⡹⡸⠜⠜⠜⠘⠀⡄⢔⢜⢜⢜⡜⡜⡆⠀⠸⡜⣜⢜⠌⢰⢱⡱⡱⡕⡇⡏⢮⢪⡪⡺⡸⢁⢏⡎⠇⠃⡠⡣⡱⢪⢪⡪⣪⢪⡪⣪⢲⡀
                    ⢜⢜⢜⢎⢮⢪⢣⠣⡣⡱⡱⡩⡢⡀⠀⠁⡁⢨⢪⡪⣪⢪⡂⢑⢝⡜⡜⡎⡎⠀⡣⡣⠣⠁⠀⡎⡎⡞⡜⣜⢜⢜⢜⢜⢔⢅⠤⡠⡀⡄⡠⡠⡐⡔⢔⢱⢱⢱⢱⢱⡱⡕⣕⢝⢔⠄⠈⠪⡪⡪⠀⢪⢪⡪⡎⡮⡊⢐⢵⢱⢕⢝⠔⡀⡁⠀⠠⡑⡌⡎⡜⢜⢌⢎⢎⢮⢪⡪⣪⢪
                    ⠘⡜⢕⢕⢕⢱⢡⠣⡣⢪⠢⡃⠊⠀⠀⠐⡝⣜⢜⡜⣜⢜⠔⠀⣇⢗⢝⠜⠀⠀⠀⠀⠠⡠⡣⣣⢫⡪⡺⡸⡸⡱⡱⡕⡕⡕⡕⡕⡕⠅⡇⡣⡱⡱⡱⡱⡕⣕⢵⢱⡱⡱⡱⡕⡇⡗⡤⡀⠀⠀⠀⠀⠣⡣⣣⢳⢀⠘⡜⡜⡎⣎⢇⢗⠄⠀⠀⠈⠊⢎⢜⠜⡌⡆⡇⡕⢕⠕⡕⡑
                    ⠀⠈⠘⠰⢑⠅⢇⠣⠃⠅⠁⠀⠀⠀⠀⠀⠹⢸⢸⢸⠜⠈⠀⠀⠀⠁⠁⠀⠀⠀⠀⠄⠀⢑⢍⢎⢎⢮⢪⢎⢇⢏⢮⢪⢺⢸⢱⢱⢑⠁⡎⢎⢎⢎⢞⢜⡜⣜⢜⢜⢜⢎⢇⢇⢧⢓⠕⠁⢀⠀⡀⠀⠀⠈⠀⠁⠀⠀⠈⠪⡣⡣⡣⠣⠁⠀⠀⠀⠀⠀⠁⠃⠣⠱⠸⠘⠜⠈⠀⠀
                    ⢸⣤⣄⣀⡀⡀⢀⢀⣀⣄⣤⣤⣶⣾⣾⣦⣄⡀⡀⣀⣠⣰⣧⣀⠀⡀⣀⣠⡴⠀⠈⡌⡂⠀⠈⢪⢪⢪⢪⢎⢎⢇⢧⢳⢱⠣⠣⡣⠁⡐⡕⢕⢱⢸⢸⢱⢱⡱⡕⡝⡜⡎⡎⡇⡇⡃⠁⢀⠢⡡⠀⢠⣄⡄⡀⢀⢠⣼⣦⣠⡀⡀⣀⣄⣶⣿⣾⣶⣦⣦⣤⣠⣀⡀⣀⣀⣄⣄⣦⡷
                    ⢸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡇⠀⡐⢅⢊⠆⡐⠀⠈⠪⢪⢪⢪⠣⡃⡧⡣⡣⡢⡀⠐⠡⠊⠌⢂⢡⣬⣮⣇⢮⢪⢪⢣⢣⠣⠃⠂⠀⡠⢡⢑⠔⠀⠐⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣟
                    ⠸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠅⠀⡐⡑⢔⠡⡂⠅⢄⠀⠐⠈⠂⢂⢇⢧⢫⢪⢎⢖⠄⠁⡠⡮⣞⡿⣿⣟⡯⡇⠱⠑⡁⠁⢀⠠⠠⡑⡐⢅⠢⡑⡁⠀⢹⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡗
                    ⠈⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣟⠀⠀⡪⠨⠢⡑⡐⡡⠡⠊⢔⠠⠀⠰⡹⡸⡸⡱⡱⡕⡇⣕⢽⣝⣗⡯⣗⣗⢏⠇⢀⠠⡀⡂⡂⡪⠐⢔⠨⠢⡑⢌⠄⠀⢸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠂
                    ⠀⠸⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡗⠀⢀⠪⡘⢌⠢⢊⠄⠅⠕⡁⡪⢂⠀⠘⢕⢝⢜⢎⢮⢺⢸⢸⢚⢮⢫⢳⠱⠃⠁⠠⡊⡢⢑⢐⠌⢌⠢⢘⢌⢌⡆⡇⠀⠘⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠏⠀
                    ⠀⠀⠙⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡇⠀⠠⡑⢌⠢⡑⡡⠨⡨⢊⢐⠌⡢⢂⢀⠀⠈⠊⠣⢣⢣⢳⢱⠝⢜⠊⠅⠁⢀⢐⠕⡨⡂⢕⢐⢌⢢⢊⢎⢎⢮⢪⠮⠀⠈⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠋⠀⠀
                    ⠀⠀⠀⠈⢻⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠅⠀⢌⢎⡢⡕⣌⢢⢑⢐⢔⢐⢅⢊⡢⡑⡔⡠⡠⡀⡀⠀⠀⠀⠀⡀⡀⡄⡆⡎⡖⡕⣕⢕⢕⢅⢣⢑⢅⢇⢧⢳⢱⢹⠀⠀⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠟⠁⠀⠀⠀
                    ⠀⠀⠀⠀⠀⠈⠻⢿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠅⠀⢜⢎⢎⡎⣎⢆⢇⢕⢜⠰⡱⡱⡱⡕⡵⡱⡕⣕⢕⢵⢱⢕⢇⢧⢳⢱⡹⡜⡎⡞⡜⡜⡔⢅⢣⢑⢅⢇⢧⢣⢳⢱⠁⠀⣺⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⡛⠁⠀⠀⠀⠀⠀
                    ⢁⠀⠀⠀⠀⠀⠀⠀⠉⠛⠻⠿⡿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⠂⠀⢪⡪⡎⣎⢎⢎⢆⢕⠜⡌⢎⢎⢮⢪⢎⢮⢪⡪⡎⡮⡪⡎⡮⡪⣪⢣⢣⡣⣣⢳⢹⢸⠨⡪⡘⡌⢎⢎⢮⢪⡣⣓⠅⠀⣺⣿⣿⣿⣿⣿⣿⣿⣿⣿⣿⡿⣿⢿⢿⠿⠻⠋⠁⠀⠀⠀⠀⠀⠀⠀⢂
                    """;
            writer.write(FOOTER);
        } catch (IOException e) {
            loggerService.exception("Исключение в ConfigManager/saveConfig: %s".formatted(e));
        }
    }
}