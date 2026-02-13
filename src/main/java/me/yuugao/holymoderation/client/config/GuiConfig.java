package me.yuugao.holymoderation.client.config;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuiConfig extends Config {
    @Expose
    private boolean watermarkEnabled = true;

    public GuiConfig() {
        super("gui");
    }
}