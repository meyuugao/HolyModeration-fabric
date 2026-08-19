package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.config.impl.GuiConfig;

import java.awt.Color;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class ThemeService {
    private final ConfigManagerService configManagerService;

    private Color cachedMain;
    private Color cachedSecond;
    private ThemePalette cachedPalette;

    public ThemePalette getPalette() {
        GuiConfig guiConfig = configManagerService.getGuiConfig();
        Color main = guiConfig.getMainColor();
        Color second = guiConfig.getSecondColor();

        if (cachedPalette == null || !main.equals(cachedMain) || !second.equals(cachedSecond)) {
            cachedMain = main;
            cachedSecond = second;
            cachedPalette = ThemePalette.from(main, second);
        }
        return cachedPalette;
    }

    public Color getMainColor() {
        return configManagerService.getGuiConfig().getMainColor();
    }

    public Color getSecondColor() {
        return configManagerService.getGuiConfig().getSecondColor();
    }

    public Color fillColor(String hudElementId, Color base) {
        GuiConfig guiConfig = configManagerService.getGuiConfig();
        if (guiConfig.isHudColorEnabled(hudElementId)) {
            return guiConfig.getHudColor(hudElementId);
        }
        return base;
    }

    public Color accentColor(String hudElementId, Color base) {
        GuiConfig guiConfig = configManagerService.getGuiConfig();
        if (guiConfig.isHudColorEnabled(hudElementId)) {
            return guiConfig.getHudColor2(hudElementId);
        }
        return base;
    }

    public int readableOn(Color background) {
        double luminance = (0.299 * background.getRed() + 0.587 * background.getGreen() + 0.114 * background.getBlue()) / 255.0;
        return luminance > 0.6 ? 0xFF181A20 : 0xFFFFFFFF;
    }
}
