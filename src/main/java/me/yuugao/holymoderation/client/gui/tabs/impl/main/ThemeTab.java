package me.yuugao.holymoderation.client.gui.tabs.impl.main;

import me.yuugao.holymoderation.client.gui.screen.impl.MainGuiScreen;
import me.yuugao.holymoderation.client.gui.tabs.Tab;
import me.yuugao.holymoderation.client.gui.tabs.widgets.ColorFieldSet;
import me.yuugao.holymoderation.client.util.factory.DrawableElementFactory;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.Render2DService;
import me.yuugao.holymoderation.client.util.service.ThemePalette;
import me.yuugao.holymoderation.client.util.service.ThemeService;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.config.impl.GuiConfig;

import net.minecraft.client.gui.DrawContext;

public class ThemeTab extends Tab<MainGuiScreen> {
    private final ThemeService themeService;
    private final ConfigManagerService configManagerService;

    private final ColorFieldSet mainSet;
    private final ColorFieldSet secondSet;

    public ThemeTab(MainGuiScreen parent, ThemeService themeService, ConfigManagerService configManagerService,
                    MinecraftService minecraftService, Render2DService render2DService, DrawableElementFactory factory) {
        super(parent);

        this.themeService = themeService;
        this.configManagerService = configManagerService;

        GuiConfig guiConfig = configManagerService.getGuiConfig();

        this.mainSet = new ColorFieldSet(factory, minecraftService, render2DService, "Основной цвет",
                guiConfig::getMainColor,
                color -> guiConfig.setMainColor(color),
                () -> configManagerService.saveConfig(guiConfig));

        this.secondSet = new ColorFieldSet(factory, minecraftService, render2DService, "Вторичный цвет",
                guiConfig::getSecondColor,
                color -> guiConfig.setSecondColor(color),
                () -> configManagerService.saveConfig(guiConfig));
    }

    @Override
    public void onRender(DrawContext ctx, int relMouseX, int relMouseY, float tickDelta) {
        ThemePalette palette = themeService.getPalette();
        float pW = parent.getWidth();
        float pH = parent.getHeight();
        int z = parent.getRenderPriority();

        float square = Math.min(pW, pH) * 0.24f;
        float fieldW = 170f;

        mainSet.render(ctx, z, pW * 0.29f, 30f, pW, pH, square, fieldW, palette);
        secondSet.render(ctx, z, pW * 0.71f, 30f, pW, pH, square, fieldW, palette);
    }

    @Override
    public boolean onMouseClick(float mouseX, float mouseY) {
        float pW = parent.getWidth();
        float pH = parent.getHeight();
        if (mainSet.handleClick(pW, pH, mouseX, mouseY)) return true;
        return secondSet.handleClick(pW, pH, mouseX, mouseY);
    }

    @Override
    public void onMouseDrag(float mouseX, float mouseY) {
        float pW = parent.getWidth();
        float pH = parent.getHeight();
        mainSet.handleDrag(pW, pH, mouseX, mouseY);
        secondSet.handleDrag(pW, pH, mouseX, mouseY);
    }

    @Override
    public void onMouseRelease() {
        mainSet.handleRelease();
        secondSet.handleRelease();
    }

    @Override
    public boolean onCharTyped(char chr) {
        if (mainSet.onCharTyped(chr)) return true;
        return secondSet.onCharTyped(chr);
    }

    @Override
    public boolean onKeyPress(int key, int scancode, int action, int modifiers) {
        if (mainSet.onKeyPress(key, scancode, action, modifiers)) return true;
        return secondSet.onKeyPress(key, scancode, action, modifiers);
    }
}
