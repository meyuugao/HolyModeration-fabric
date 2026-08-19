package me.yuugao.holymoderation.client.gui.tabs.widgets;

import me.yuugao.holymoderation.client.util.factory.DrawableElementFactory;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.Render2DService;
import me.yuugao.holymoderation.client.util.service.ThemePalette;
import me.yuugao.holymoderation.client.util.service.config.impl.GuiConfig;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.function.Consumer;

public class PopupColorEditor {
    private final DrawableElementFactory factory;
    private final MinecraftService minecraftService;
    private final Render2DService render2DService;
    private final GuiConfig guiConfig;
    private final Runnable save;

    private boolean visible = false;
    private String elementId = "";
    private String title = "";
    private ColorFieldSet fieldSet;
    private float cbX, cbY;
    private static final float CB_SIZE = 18f;

    public PopupColorEditor(DrawableElementFactory factory, MinecraftService minecraftService,
                            Render2DService render2DService, GuiConfig guiConfig, Runnable save) {
        this.factory = factory;
        this.minecraftService = minecraftService;
        this.render2DService = render2DService;
        this.guiConfig = guiConfig;
        this.save = save;
    }

    public boolean isVisible() {
        return visible;
    }

    public String getElementId() {
        return elementId;
    }

    public void open(String elementId, String title, Color initial) {
        this.elementId = elementId;
        this.title = title;
        this.visible = true;

        final Color[] holder = {initial};
        this.fieldSet = new ColorFieldSet(factory, minecraftService, render2DService, title,
                () -> holder[0],
                color -> {
                    holder[0] = color;
                    guiConfig.setHudColor(elementId, color);
                    save.run();
                },
                () -> {
                });
    }

    public void close() {
        visible = false;
        fieldSet = null;
    }

    public void render(DrawContext ctx, int z, ThemePalette palette, float pW, float pH) {
        if (!visible || fieldSet == null) return;

        TextRenderer tr = minecraftService.getClient().textRenderer;
        float square = Math.min(pW, pH) * 0.22f;
        float fieldW = 180f;
        float panelW = Math.max(fieldW, square + 24f) + 28f;
        float panelH = square + 110f;
        float px = (pW - panelW) / 2f;
        float py = (pH - panelH) / 2f;

        render2DService.renderSoftRoundedRect(ctx.getMatrices(), px, py, panelW, panelH, z + 1, 12f, palette.surfaceElevated, 0);
        render2DService.renderSoftRoundedRectOutline(ctx.getMatrices(), px, py, panelW, panelH, z + 1, 12f,
                palette.surfaceElevated, palette.outline, 1.5f, 2f);

        cbX = px + panelW - CB_SIZE - 8f;
        cbY = py + 8f;
        render2DService.renderSoftRoundedRect(ctx.getMatrices(), cbX, cbY, CB_SIZE, CB_SIZE, z + 2, 6f, palette.surface, 0);
        String cross = "✕";
        render2DService.renderText(tr, Text.literal(cross).asOrderedText(),
                (int) (cbX + (CB_SIZE - tr.getWidth(cross)) / 2f), (int) (cbY + (CB_SIZE - tr.fontHeight) / 2f + 1f),
                z + 2, palette.textSecondary.getRGB(), false, ctx);

        fieldSet.render(ctx, z + 2, pW / 2f, py + 8f, pW, pH, square, fieldW, palette);
    }

    public boolean handleClick(float pW, float pH, double mouseX, double mouseY) {
        if (!visible) return false;
        if (mouseX >= cbX && mouseX <= cbX + CB_SIZE && mouseY >= cbY && mouseY <= cbY + CB_SIZE) {
            close();
            return true;
        }
        return fieldSet != null && fieldSet.handleClick(pW, pH, mouseX, mouseY);
    }

    public void handleDrag(float pW, float pH, double mouseX, double mouseY) {
        if (visible && fieldSet != null) fieldSet.handleDrag(pW, pH, mouseX, mouseY);
    }

    public void handleRelease() {
        if (visible && fieldSet != null) fieldSet.handleRelease();
    }

    public boolean onCharTyped(char c) {
        return visible && fieldSet != null && fieldSet.onCharTyped(c);
    }

    public boolean onKeyPress(int key, int scancode, int action, int modifiers) {
        return visible && fieldSet != null && fieldSet.onKeyPress(key, scancode, action, modifiers);
    }
}
