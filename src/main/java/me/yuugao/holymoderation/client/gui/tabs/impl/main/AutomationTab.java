package me.yuugao.holymoderation.client.gui.tabs.impl.main;

import me.yuugao.holymoderation.client.gui.screen.impl.MainGuiScreen;
import me.yuugao.holymoderation.client.gui.tabs.SettingsTab;
import me.yuugao.holymoderation.client.util.factory.DrawableElementFactory;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.Render2DService;
import me.yuugao.holymoderation.client.util.service.ThemeService;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.config.impl.SettingsConfig;

public class AutomationTab extends SettingsTab {
    public AutomationTab(MainGuiScreen parent, ThemeService themeService, ConfigManagerService configManagerService,
                         MinecraftService minecraftService, Render2DService render2DService, DrawableElementFactory factory) {
        super(parent, themeService, configManagerService, minecraftService, render2DService, factory);

        SettingsConfig c = configManagerService.getSettingsConfig();

        addToggle("Авто-ваниш", c.isAutoVanishEnabled(), v -> {
            c.setAutoVanishEnabled(v);
            configManagerService.saveConfig(c);
        });
        addToggle("Авто-полёт", c.isAutoFlyEnabled(), v -> {
            c.setAutoFlyEnabled(v);
            configManagerService.saveConfig(c);
        });
        addToggle("Авто-GM3", c.isAutoGm3Enabled(), v -> {
            c.setAutoGm3Enabled(v);
            configManagerService.saveConfig(c);
        });
        addToggle("Авто-HAC", c.isAutoHacAlertsEnabled(), v -> {
            c.setAutoHacAlertsEnabled(v);
            configManagerService.saveConfig(c);
        });
        addToggle("Авто-бог", c.isAutoGodEnabled(), v -> {
            c.setAutoGodEnabled(v);
            configManagerService.saveConfig(c);
        });
        addToggle("DupeIP", c.isDupeIpEnabled(), v -> {
            c.setDupeIpEnabled(v);
            configManagerService.saveConfig(c);
        });
        addToggle("Авто-AnyDesk", c.isAutoAnyDeskEnabled(), v -> {
            c.setAutoAnyDeskEnabled(v);
            configManagerService.saveConfig(c);
        });
        addToggle("Авто-TP на проверку", c.isAutoCheckoutTpEnabled(), v -> {
            c.setAutoCheckoutTpEnabled(v);
            configManagerService.saveConfig(c);
        });
        addToggle("Авто-Spy TP", c.isAutoSpyTpEnabled(), v -> {
            c.setAutoSpyTpEnabled(v);
            configManagerService.saveConfig(c);
        });

        addSlider("Задержка spy", 0f, 60f, 1f, c.getSpyDelay(),
                v -> c.setSpyDelay(Math.round(v)),
                () -> c.getSpyDelay() + " сек",
                () -> configManagerService.saveConfig(c));
    }
}
