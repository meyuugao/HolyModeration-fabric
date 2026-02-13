package me.yuugao.holymoderation.client.config;

import lombok.Getter;

@Getter
public abstract class Config {
    private final String configName;

    public Config(String configName) {
        this.configName = configName;
    }
}