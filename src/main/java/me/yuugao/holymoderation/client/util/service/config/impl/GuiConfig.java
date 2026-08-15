package me.yuugao.holymoderation.client.util.service.config.impl;

import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.service.config.Config;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

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
    @Expose
    private final Map<String, Float> hudScales = new HashMap<>();
    @Expose
    private final Map<String, PivotMode> hudPivotModes = new HashMap<>();

    public GuiConfig() {
        super("gui");
    }

    public float getHudScale(String elementId) {
        return hudScales.getOrDefault(elementId, 1f);
    }

    public void setHudScale(String elementId, float scale) {
        hudScales.put(elementId, scale);
    }

    public PivotMode getPivotMode(String elementId) {
        return hudPivotModes.getOrDefault(elementId, PivotMode.RIGHT_DOWN);
    }

    public void setPivotMode(String elementId, PivotMode pivotMode) {
        hudPivotModes.put(elementId, pivotMode);
    }
}