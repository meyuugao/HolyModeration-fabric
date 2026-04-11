package me.yuugao.holymoderation.client.config.manager;

import me.yuugao.holymoderation.client.config.Config;
import me.yuugao.holymoderation.client.config.impl.ApiConfig;
import me.yuugao.holymoderation.client.config.impl.GuiConfig;
import me.yuugao.holymoderation.client.config.impl.KeyBindsConfig;
import me.yuugao.holymoderation.client.config.impl.SettingsConfig;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.LoggerService;

import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import lombok.Getter;
import lombok.Setter;

public class ConfigManager {
    private static final Path configsDir = Paths.get(System.getProperty("user.home"), "HolyModeration", "Config");

    private static final String HEADER = """
            /*
            ********************************************************************************
            **                              ВНИМАНИЕ!                                     **
            **  Создатель мода не несёт ответственности за любые изменения конфигурации   **
            **  вручную, за передачу конфигурационных файлов другим лицам, включая API    **
            **  токен, и возможные последствия использования модифицированного файла.     **
            ********************************************************************************
            */
            
            """;
    private static final String FOOTER = """
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

    private final Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    @Getter
    private final ApiConfig apiConfig;
    @Getter
    private final GuiConfig guiConfig;
    @Getter
    private final KeyBindsConfig keyBindsConfig;
    @Getter
    private final SettingsConfig settingsConfig;
    @Setter
    private LoggerService loggerService;

    public ConfigManager() {
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
        } catch (Exception e) {
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
                T loaded = gson.fromJson(jr, defaultConfig.getClass());
                return loaded != null ? loaded : defaultConfig;
            }
        } catch (Exception e) {
            loggerService.exception("Исключение в ConfigManager/loadOrCreate: %s".formatted(e));
            saveConfig(defaultConfig);
            return defaultConfig;
        }
    }

    public void saveConfig(Config config) {
        Path configPath = configsDir.resolve("%sConfig.json".formatted(config.getConfigName()));

        try (OutputStreamWriter writer = new OutputStreamWriter(Files.newOutputStream(configPath), StandardCharsets.UTF_8)) {
            writer.write(HEADER);
            gson.toJson(config, writer);
            writer.write(FOOTER);
        } catch (Exception e) {
            loggerService.exception("Исключение в ConfigManager/saveConfig: %s".formatted(e));
        }
    }
}