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
    @Expose
    private final Map<String, Color> hudColors = new HashMap<>();
    @Expose
    private final Map<String, Color> hudColors2 = new HashMap<>();
    @Expose
    private final Map<String, Boolean> hudColorEnabled = new HashMap<>();

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

    public Color getHudColor(String elementId) {
        return hudColors.getOrDefault(elementId, secondColor);
    }

    public void setHudColor(String elementId, Color color) {
        hudColors.put(elementId, color);
    }

    public Color getHudColor2(String elementId) {
        return hudColors2.getOrDefault(elementId, mainColor);
    }

    public void setHudColor2(String elementId, Color color) {
        hudColors2.put(elementId, color);
    }

    public boolean isHudColorEnabled(String elementId) {
        return hudColorEnabled.getOrDefault(elementId, false);
    }

    public void setHudColorEnabled(String elementId, boolean enabled) {
        hudColorEnabled.put(elementId, enabled);
    }
}