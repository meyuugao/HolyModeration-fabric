package me.yuugao.holymoderation.client.gui.tabs.impl.main;

import me.yuugao.holymoderation.client.di.DIAccessor;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.ToggleDrawableElement;
import me.yuugao.holymoderation.client.gui.screen.impl.MainGuiScreen;
import me.yuugao.holymoderation.client.gui.tabs.SettingsTab;
import me.yuugao.holymoderation.client.gui.tabs.widgets.ColorFieldSet;
import me.yuugao.holymoderation.client.gui.tabs.widgets.PopupColorEditor;
import me.yuugao.holymoderation.client.util.factory.DrawableElementFactory;
import me.yuugao.holymoderation.client.util.service.GuiManagerService;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.Render2DService;
import me.yuugao.holymoderation.client.util.service.ThemePalette;
import me.yuugao.holymoderation.client.util.service.ThemeService;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.config.impl.GuiConfig;
import me.yuugao.holymoderation.client.util.service.config.impl.SettingsConfig;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class AppearanceTab extends SettingsTab {
    private static final float SWATCH_SIZE = 16f;

    private final ColorFieldSet mainSet;
    private final ColorFieldSet secondSet;
    private final List<ElementColorRow> elementRows = new ArrayList<>();
    private final PopupColorEditor popup;

    private static class ElementColorRow {
        final String id;
        final ToggleDrawableElement toggle;
        float y;
        float swatchX, swatchY, swatch2X, swatch2Y;

        ElementColorRow(String id, ToggleDrawableElement toggle) {
            this.id = id;
            this.toggle = toggle;
        }
    }

    public AppearanceTab(MainGuiScreen parent, ThemeService themeService, ConfigManagerService configManagerService,
                         MinecraftService minecraftService, Render2DService render2DService, DrawableElementFactory factory) {
        super(parent, themeService, configManagerService, minecraftService, render2DService, factory);

        SettingsConfig c = configManagerService.getSettingsConfig();
        GuiConfig g = configManagerService.getGuiConfig();

        addSlider("Масштаб HUD", 0.5f, 2f, 0.05f, c.getHudScale(),
                v -> c.setHudScale(round2(v)),
                () -> String.format("%.2f", c.getHudScale()),
                () -> configManagerService.saveConfig(c));

        addToggle("Ватермарка", g.isWatermarkEnabled(), v -> {
            g.setWatermarkEnabled(v);
            configManagerService.saveConfig(g);
        });

        this.mainSet = new ColorFieldSet(factory, minecraftService, render2DService, "Основной цвет",
                g::getMainColor,
                color -> g.setMainColor(color),
                () -> configManagerService.saveConfig(g));
        this.secondSet = new ColorFieldSet(factory, minecraftService, render2DService, "Вторичный цвет",
                g::getSecondColor,
                color -> g.setSecondColor(color),
                () -> configManagerService.saveConfig(g));

        this.popup = new PopupColorEditor(factory, minecraftService, render2DService,
                () -> configManagerService.saveConfig(g));

        for (String id : hudElementIds()) {
            ToggleDrawableElement toggle = factory.createToggle(v -> {
                g.setHudColorEnabled(id, v);
                configManagerService.saveConfig(g);
            });
            toggle.setEnabled(g.isHudColorEnabled(id));

            elementRows.add(new ElementColorRow(id, toggle));
        }
    }

    private List<String> hudElementIds() {
        List<String> ids = new ArrayList<>();
        try {
            GuiManagerService guiManagerService = DIAccessor.getDI().get(GuiManagerService.class);
            for (var module : guiManagerService.getDrawableModules()) {
                me.yuugao.holymoderation.client.gui.drawable.element.impl.DrawableElement elem = module.getDrawableElement();
                if (elem instanceof me.yuugao.holymoderation.client.gui.drawable.element.impl.StatefulDrawableElement<?> stateful) {
                    String id = stateful.getHudElementId();
                    if (!"notifications".equals(id) && !ids.contains(id)) {
                        ids.add(id);
                    }
                }
            }
        } catch (Exception ignored) {
        }
        if (ids.isEmpty()) {
            ids.addAll(List.of("watermark", "checkouts", "spy", "reportsparser"));
        }
        return ids;
    }

    private float setsHeight() {
        float square = Math.min(parent.getWidth(), parent.getHeight()) * 0.17f;
        return square + 96f;
    }

    @Override
    protected float extraHeight() {
        float square = Math.min(parent.getWidth(), parent.getHeight()) * 0.17f;
        float setsHeight = square + 96f;
        TextRenderer tr = minecraftService.getClient().textRenderer;
        return 8f + setsHeight + tr.fontHeight + 10f + elementRows.size() * 26f + 10f;
    }

    @Override
    protected void renderExtra(DrawContext ctx, ThemePalette palette, float pW, float pH, int z, float scroll) {
        TextRenderer tr = minecraftService.getClient().textRenderer;

        float setsY = rowsEndY() - scroll + 8f;
        float square = Math.min(pW, pH) * 0.17f;
        float fieldW = 150f;

        mainSet.render(ctx, z, pW * 0.29f, setsY, pW, pH, square, fieldW, palette);
        secondSet.render(ctx, z, pW * 0.71f, setsY, pW, pH, square, fieldW, palette);

        float listY = setsY + square + 96f;
        renderText(ctx, z, "Уникальные цвета HUD", PAD, listY, tr, palette.textSecondary);
        listY += tr.fontHeight + 10f;

        float fieldX = PAD + 150f;

        for (ElementColorRow row : elementRows) {
            row.y = listY;

            renderText(ctx, z, row.id, PAD, listY + 7f, tr, palette.textPrimary);

            row.toggle.updateRenderForParent(ctx, (fieldX - 50f) / pW, (listY + 1f) / pH, 42f, 20f, pW, pH, z,
                    palette.primary, palette.surface, palette.textPrimary, palette.outline, 1.5f, 2f);

            Color color = configManagerService.getGuiConfig().getHudColor(row.id);
            Color color2 = configManagerService.getGuiConfig().getHudColor2(row.id);
            row.swatchX = fieldX + 6f;
            row.swatchY = listY + 3f;
            row.swatch2X = fieldX + 6f + SWATCH_SIZE + 6f;
            row.swatch2Y = listY + 3f;
            render2DService.renderSoftRoundedRect(ctx, row.swatchX, row.swatchY, SWATCH_SIZE, SWATCH_SIZE, z,
                    5f, color, 0);
            render2DService.renderSoftRoundedRectOutline(ctx, row.swatchX, row.swatchY, SWATCH_SIZE, SWATCH_SIZE, z,
                    5f, color, palette.outline, 1.2f, 1f);
            render2DService.renderSoftRoundedRect(ctx, row.swatch2X, row.swatch2Y, SWATCH_SIZE, SWATCH_SIZE, z,
                    5f, color2, 0);
            render2DService.renderSoftRoundedRectOutline(ctx, row.swatch2X, row.swatch2Y, SWATCH_SIZE, SWATCH_SIZE, z,
                    5f, color2, palette.outline, 1.2f, 1f);

            listY += 26f;
        }
    }

    @Override
    protected void renderOverlay(DrawContext ctx, ThemePalette palette, float pW, float pH, int z) {
        popup.render(ctx, z, palette, pW, pH);
    }

    private void renderText(DrawContext ctx, int z, String text, float x, float y, TextRenderer tr, Color color) {
        render2DService.renderText(tr, Text.literal(text).asOrderedText(),
                (int) x, (int) y, z, color.getRGB(), false, ctx);
    }

    @Override
    public boolean onMouseClick(float mouseX, float mouseY) {
        float pW = parent.getWidth();
        float pH = parent.getHeight();

        if (popup.handleClick(pW, pH, mouseX, mouseY)) return true;

        if (super.onMouseClick(mouseX, mouseY)) return true;

        if (mainSet.handleClick(pW, pH, mouseX, mouseY)) return true;
        if (secondSet.handleClick(pW, pH, mouseX, mouseY)) return true;

        for (ElementColorRow row : elementRows) {
            if (row.toggle.handleClick(pW, pH, mouseX, mouseY)) return true;
            GuiConfig g = configManagerService.getGuiConfig();
            if (mouseX >= row.swatchX && mouseX <= row.swatchX + SWATCH_SIZE
                    && mouseY >= row.swatchY && mouseY <= row.swatchY + SWATCH_SIZE) {
                popup.open("Цвет: " + row.id, g.getHudColor(row.id), c -> g.setHudColor(row.id, c));
                return true;
            }
            if (mouseX >= row.swatch2X && mouseX <= row.swatch2X + SWATCH_SIZE
                    && mouseY >= row.swatch2Y && mouseY <= row.swatch2Y + SWATCH_SIZE) {
                popup.open("Акцент: " + row.id, g.getHudColor2(row.id), c -> g.setHudColor2(row.id, c));
                return true;
            }
        }
        return false;
    }

    @Override
    public void onMouseDrag(float mouseX, float mouseY) {
        float pW = parent.getWidth();
        float pH = parent.getHeight();
        if (popup.isVisible()) {
            popup.handleDrag(pW, pH, mouseX, mouseY);
            return;
        }
        super.onMouseDrag(mouseX, mouseY);
        mainSet.handleDrag(pW, pH, mouseX, mouseY);
        secondSet.handleDrag(pW, pH, mouseX, mouseY);
    }

    @Override
    public void onMouseRelease() {
        if (popup.isVisible()) {
            popup.handleRelease();
            return;
        }
        super.onMouseRelease();
        mainSet.handleRelease();
        secondSet.handleRelease();
    }

    @Override
    public boolean onCharTyped(char chr) {
        if (popup.onCharTyped(chr)) return true;
        if (super.onCharTyped(chr)) return true;
        if (mainSet.onCharTyped(chr)) return true;
        return secondSet.onCharTyped(chr);
    }

    @Override
    public boolean onKeyPress(int key, int scancode, int action, int modifiers) {
        if (popup.onKeyPress(key, scancode, action, modifiers)) return true;
        if (super.onKeyPress(key, scancode, action, modifiers)) return true;
        if (mainSet.onKeyPress(key, scancode, action, modifiers)) return true;
        return secondSet.onKeyPress(key, scancode, action, modifiers);
    }
}
