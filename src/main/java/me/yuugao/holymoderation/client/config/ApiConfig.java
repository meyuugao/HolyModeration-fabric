package me.yuugao.holymoderation.client.config;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

@Getter
public class ApiConfig extends Config {
    public ApiConfig() {
        super("api");
    }

    private final String currentVersion = "2.10alpha"; //tip: измени перед релизом если нужно

    @Expose
    @Setter
    private String apiToken = StringUtils.EMPTY;
}