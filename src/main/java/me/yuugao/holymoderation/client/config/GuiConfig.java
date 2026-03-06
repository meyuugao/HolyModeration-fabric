package me.yuugao.holymoderation.client.config;

import java.awt.Color;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;

@Getter
public class GuiConfig extends Config {
    @Expose
    @Setter
    private boolean watermarkEnabled = true;
    @Expose
    @Setter
    private Color mainColor = new Color(10, 20, 40);
    @Expose
    @Setter
    private Color secondColor = new Color(60, 120, 220);

    public GuiConfig() {
        super("gui");
    }
}