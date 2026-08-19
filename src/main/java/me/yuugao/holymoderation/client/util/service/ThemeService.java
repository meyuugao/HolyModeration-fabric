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
}
