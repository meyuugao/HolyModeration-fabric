package me.yuugao.holymoderation.client.gui.tabs.impl.main;

import me.yuugao.holymoderation.client.gui.screen.impl.MainGuiScreen;
import me.yuugao.holymoderation.client.gui.tabs.SettingsTab;
import me.yuugao.holymoderation.client.util.factory.DrawableElementFactory;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.Render2DService;
import me.yuugao.holymoderation.client.util.service.ThemeService;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.config.impl.GuiConfig;
import me.yuugao.holymoderation.client.util.service.config.impl.SettingsConfig;

public class GeneralTab extends SettingsTab {
    public GeneralTab(MainGuiScreen parent, ThemeService themeService, ConfigManagerService configManagerService,
                      MinecraftService minecraftService, Render2DService render2DService, DrawableElementFactory factory) {
        super(parent, themeService, configManagerService, minecraftService, render2DService, factory);

        SettingsConfig c = configManagerService.getSettingsConfig();
        GuiConfig g = configManagerService.getGuiConfig();

        addToggle("Звуки", c.isSoundsEnabled(), v -> {
            c.setSoundsEnabled(v);
            configManagerService.saveConfig(c);
        });

        addSlider("Громкость", 0f, 100f, 1f, c.getSoundsVolume(),
                v -> c.setSoundsVolume(Math.round(v)),
                () -> String.valueOf(c.getSoundsVolume()),
                () -> configManagerService.saveConfig(c));

        addSlider("Масштаб HUD", 0.5f, 2f, 0.05f, c.getHudScale(),
                v -> c.setHudScale(round2(v)),
                () -> String.format("%.2f", c.getHudScale()),
                () -> configManagerService.saveConfig(c));

        addToggle("Ватермарка", g.isWatermarkEnabled(), v -> {
            g.setWatermarkEnabled(v);
            configManagerService.saveConfig(g);
        });

        addToggle("Кнопка копирования", c.isCopyButtonEnabled(), v -> {
            c.setCopyButtonEnabled(v);
            configManagerService.saveConfig(c);
        });

        addTextField("Текст кнопки", "§ = &", toDisplay(c.getCopyButtonText()), s -> {
            c.setCopyButtonText(toModel(s));
            configManagerService.saveConfig(c);
        });

        addTextField("Маркер игрока", "§ = &", toDisplay(c.getPlayerMarker()), s -> {
            c.setPlayerMarker(toModel(s));
            configManagerService.saveConfig(c);
        });
    }

    private static String toDisplay(String value) {
        return value == null ? "" : value.replace('§', '&');
    }

    private static String toModel(String value) {
        return value == null ? "" : value.replace('&', '§');
    }
}
