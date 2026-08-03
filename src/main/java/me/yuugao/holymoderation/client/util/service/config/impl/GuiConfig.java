package me.yuugao.holymoderation.client.util.service.config.impl;

import me.yuugao.holymoderation.client.util.service.config.Config;

import java.awt.Color;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuiConfig extends Config {
    @Expose
    private boolean watermarkEnabled = true;
    @Expose
    private Color mainColor = new Color(10, 20, 40, 255);
    @Expose
    private Color secondColor = new Color(60, 120, 220, 255);

    public GuiConfig() {
        super("gui");
    }
}