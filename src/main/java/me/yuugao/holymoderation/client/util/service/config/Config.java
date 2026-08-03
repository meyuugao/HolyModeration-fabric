package me.yuugao.holymoderation.client.util.service.config;

import java.awt.Color;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonSerializer;
import lombok.Getter;

@Getter
public abstract class Config {
    private final Gson gson;
    private final String configName;

    public Config(String configName) {
        JsonSerializer<Color> colorSerializer = (src, typeOfSrc, context) ->
                context.serialize(src.getRGB());
        JsonDeserializer<Color> colorDeserializer = (json, typeOfT, context) ->
                new Color(json.getAsInt(), true);

        this.gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .registerTypeAdapter(Color.class, colorSerializer)
                .registerTypeAdapter(Color.class, colorDeserializer)
                .setPrettyPrinting()
                .create();

        this.configName = configName;
    }
}