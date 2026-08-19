package me.yuugao.holymoderation.client.gui.tabs.impl.main;

import me.yuugao.holymoderation.client.di.DIAccessor;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.SearchDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.ToggleDrawableElement;
import me.yuugao.holymoderation.client.gui.screen.impl.MainGuiScreen;
import me.yuugao.holymoderation.client.gui.tabs.SettingsTab;
import me.yuugao.holymoderation.client.gui.tabs.widgets.ColorFieldSet;
import me.yuugao.holymoderation.client.util.factory.DrawableElementFactory;
import me.yuugao.holymoderation.client.util.service.GuiManagerService;
import me.yuugao.holymoderation.client.util.service.HudOverrideService;
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
    private final ColorFieldSet mainSet;
    private final ColorFieldSet secondSet;
    private final List<ElementColorRow> elementRows = new ArrayList<>();
    private final HudOverrideService hudOverrideService;

    private static class ElementColorRow {
        final String id;
        final ToggleDrawableElement toggle;
        final SearchDrawableElement hex;
        float y;

        ElementColorRow(String id, ToggleDrawableElement toggle, SearchDrawableElement hex) {
            this.id = id;
            this.toggle = toggle;
            this.hex = hex;
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

        this.hudOverrideService = DIAccessor.getDI().get(HudOverrideService.class);

        for (String id : hudElementIds()) {
            ToggleDrawableElement toggle = factory.createToggle(v -> {
                g.setHudColorEnabled(id, v);
                configManagerService.saveConfig(g);
            });
            toggle.setEnabled(g.isHudColorEnabled(id));

            SearchDrawableElement hex = factory.createSearch(s -> {
                Color color = parseHex(s);
                if (color == null) return;
                g.setHudColor(id, color);
                configManagerService.saveConfig(g);
            });
            hex.setCentered(true);
            hex.setPlaceholder("RRGGBB");
            hex.setQuerySilent(hexText(g.getHudColor(id)));

            elementRows.add(new ElementColorRow(id, toggle, hex));
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

    @Override
    protected void renderExtra(DrawContext ctx, ThemePalette palette, float pW, float pH, int z) {
        TextRenderer tr = minecraftService.getClient().textRenderer;

        float setsY = rowsEndY() + 8f;
        float square = Math.min(pW, pH) * 0.17f;
        float fieldW = 150f;

        mainSet.render(ctx, z, pW * 0.29f, setsY, pW, pH, square, fieldW, palette);
        secondSet.render(ctx, z, pW * 0.71f, setsY, pW, pH, square, fieldW, palette);

        float listY = setsY + square + 66f;
        renderText(ctx, z, "Уникальные цвета HUD (ПКМ по виджету)", PAD, listY, tr, palette.textSecondary);
        listY += tr.fontHeight + 8f;

        float fieldX = PAD + 150f;
        float hexW = 70f;
        float hexX = pW - PAD - hexW;

        for (ElementColorRow row : elementRows) {
            row.y = listY;

            boolean selected = hudOverrideService.getSelectedId() != null
                    && hudOverrideService.getSelectedId().equals(row.id);

            renderText(ctx, z, row.id, PAD, listY + 7f, tr, selected ? palette.primaryBright : palette.textPrimary);

            row.toggle.updateRenderForParent(ctx, (fieldX - 50f) / pW, (listY + 1f) / pH, 42f, 20f, pW, pH, z,
                    palette.primary, palette.surface, palette.textPrimary, palette.outline, 1.5f, 2f);

            Color color = configManagerService.getGuiConfig().getHudColor(row.id);
            render2DService.renderSoftRoundedRectOutline(ctx, fieldX + 6f, listY + 4f, 14f, 14f, z,
                    4f, color, palette.outline, 1f, 1f);

            row.hex.updateRenderForParent(ctx, hexX / pW, (listY + 1f) / pH, hexW, 18f, pW, pH, z,
                    6f, palette.surface, palette.outline, palette.textPrimary, palette.textMuted, palette.primary, 1f, 2f);

            listY += 26f;
        }
    }

    private void renderText(DrawContext ctx, int z, String text, float x, float y, TextRenderer tr, Color color) {
        render2DService.renderText(tr, Text.literal(text).asOrderedText(),
                (int) x, (int) y, z, color.getRGB(), false, ctx);
    }

    @Override
    public boolean onMouseClick(float mouseX, float mouseY) {
        float pW = parent.getWidth();
        float pH = parent.getHeight();

        if (super.onMouseClick(mouseX, mouseY)) return true;

        if (mainSet.handleClick(pW, pH, mouseX, mouseY)) return true;
        if (secondSet.handleClick(pW, pH, mouseX, mouseY)) return true;

        for (ElementColorRow row : elementRows) {
            if (row.toggle.handleClick(pW, pH, mouseX, mouseY)) return true;
            boolean over = row.hex.isMouseOver(pW, pH, mouseX, mouseY);
            row.hex.setFocused(over);
            if (over) return true;
        }
        return false;
    }

    @Override
    public void onMouseDrag(float mouseX, float mouseY) {
        float pW = parent.getWidth();
        float pH = parent.getHeight();
        super.onMouseDrag(mouseX, mouseY);
        mainSet.handleDrag(pW, pH, mouseX, mouseY);
        secondSet.handleDrag(pW, pH, mouseX, mouseY);
    }

    @Override
    public void onMouseRelease() {
        super.onMouseRelease();
        mainSet.handleRelease();
        secondSet.handleRelease();
    }

    @Override
    public boolean onCharTyped(char chr) {
        if (super.onCharTyped(chr)) return true;
        if (mainSet.onCharTyped(chr)) return true;
        return secondSet.onCharTyped(chr);
    }

    @Override
    public boolean onKeyPress(int key, int scancode, int action, int modifiers) {
        if (super.onKeyPress(key, scancode, action, modifiers)) return true;
        if (mainSet.onKeyPress(key, scancode, action, modifiers)) return true;
        return secondSet.onKeyPress(key, scancode, action, modifiers);
    }

    private static String hexText(Color c) {
        return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
    }

    private static Color parseHex(String value) {
        String t = value.trim().replace("#", "");
        try {
            if (t.length() == 6) {
                int rgb = Integer.parseInt(t, 16);
                return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF);
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }
}
