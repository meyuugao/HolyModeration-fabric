package me.yuugao.holymoderation.client.gui.drawable.element.impl.impl;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.DrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.ScreenCtx;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.service.AnimationService;
import me.yuugao.holymoderation.client.util.service.Render2DService;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.function.Consumer;

public class ColorPickerDrawableElement extends DrawableElement {
    private enum DragTarget { NONE, SQUARE, HUE }

    private static final float HUE_GAP = 8f;

    private final Render2DService render2DService;
    private final Consumer<Color> onChange;

    private Color outlineColor;
    private Color selectedColor;

    private float squareSize;
    private float barWidth;
    private float cornerRadius;
    private float outlineWidth;
    private float hue;

    private DragTarget dragging = DragTarget.NONE;

    public ColorPickerDrawableElement(AnimationService animationService, Render2DService render2DService) {
        this(animationService, render2DService, null);
    }

    public ColorPickerDrawableElement(AnimationService animationService, Render2DService render2DService, Consumer<Color> onChange) {
        super(animationService, PivotMode.LEFT_UP);

        this.render2DService = render2DService;
        this.onChange = onChange;
    }

    @Override
    protected void render(DrawContext ctx, int z) {
        MatrixStack ms = ctx.getMatrices();

        render2DService.setupRender();

        render2DService.renderSVSquare(ms, 0f, 0f, squareSize, squareSize, z, cornerRadius, hue, outlineColor, outlineWidth);
        render2DService.renderHueBar(ms, squareSize + HUE_GAP, 0f, barWidth, squareSize, z, cornerRadius, outlineColor, outlineWidth);

        if (selectedColor != null) {
            renderSquareIndicator(ctx, ms, z);
            renderHueIndicator(ctx, ms, z);
        }

        render2DService.endRender();
    }

    private void renderSquareIndicator(DrawContext ctx, MatrixStack ms, int z) {
        float[] hsb = Color.RGBtoHSB(selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue(), null);
        float sat = hsb[1];
        float val = hsb[2];

        float cx = sat * squareSize;
        float cy = (1f - val) * squareSize;

        float ballR = squareSize * 0.06f;
        float ringW = Math.max(1.5f, squareSize * 0.02f);

        render2DService.renderSoftRoundedRect(ms, cx - ballR - ringW, cy - ballR - ringW,
                (ballR + ringW) * 2f, (ballR + ringW) * 2f, z, ballR + ringW, Color.WHITE, 0);
        render2DService.renderSoftRoundedRect(ms, cx - ballR, cy - ballR,
                ballR * 2f, ballR * 2f, z, ballR, selectedColor, 0);
    }

    private void renderHueIndicator(DrawContext ctx, MatrixStack ms, int z) {
        float y = hue * squareSize;
        float h = Math.max(2f, squareSize * 0.025f);
        float x = squareSize + HUE_GAP - 1.5f;

        render2DService.renderSoftRoundedRectOutline(ms, x, y - h / 2f, barWidth + 3f, h, z,
                h / 2f, Color.WHITE, outlineColor, 1f, 0f);
    }

    public void updateRenderForParent(DrawContext ctx, float relX, float relY, float parW, float parH,
                                      int z, float squareSize, float barWidth, float cornerRadius,
                                      Color outlineColor, float outlineWidth) {
        setRelativePos(relX, relY);
        this.squareSize = squareSize;
        this.barWidth = barWidth;
        this.cornerRadius = cornerRadius;
        this.outlineColor = outlineColor;
        this.outlineWidth = outlineWidth;

        this.width = squareSize + HUE_GAP + barWidth;
        this.height = squareSize;

        super.updateRenderForParent(ctx, parW, parH, z);
    }

    public void setSelectedColor(Color selectedColor) {
        this.selectedColor = selectedColor;
        if (selectedColor != null) {
            float[] hsb = Color.RGBtoHSB(selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue(), null);
            this.hue = hsb[0];
        }
    }

    @Nullable
    public Color getSelectedColor() {
        return selectedColor;
    }

    @Override
    public boolean handleClick(ScreenCtx screen) {
        return handleClick(screen.screenWidth(), screen.screenHeight(), screen.mouseX(), screen.mouseY());
    }

    public boolean handleClick(float parentW, float parentH, double mouseX, double mouseY) {
        if (!isMouseOver(parentW, parentH, mouseX, mouseY)) return false;

        float[] local = screenToLocal(parentW, parentH, (float) mouseX, (float) mouseY);
        if (local[0] <= squareSize) {
            dragging = DragTarget.SQUARE;
            applySquare(local[0], local[1]);
        } else if (local[0] >= squareSize + HUE_GAP) {
            dragging = DragTarget.HUE;
            applyHue(local[1]);
        } else {
            return false;
        }
        return true;
    }

    public boolean handleDrag(float parentW, float parentH, double mouseX, double mouseY) {
        if (dragging == DragTarget.NONE) return false;

        float[] local = screenToLocal(parentW, parentH, (float) mouseX, (float) mouseY);
        if (dragging == DragTarget.SQUARE) {
            applySquare(local[0], local[1]);
        } else if (dragging == DragTarget.HUE) {
            applyHue(local[1]);
        }
        return true;
    }

    public void handleRelease() {
        dragging = DragTarget.NONE;
    }

    private void applySquare(float x, float y) {
        float sat = clamp01(x / Math.max(squareSize, 0.001f));
        float val = clamp01(1f - y / Math.max(squareSize, 0.001f));
        applyColor(new Color(Color.HSBtoRGB(hue, sat, val)));
    }

    private void applyHue(float y) {
        this.hue = clamp01(y / Math.max(squareSize, 0.001f));
        if (selectedColor != null) {
            float[] hsb = Color.RGBtoHSB(selectedColor.getRed(), selectedColor.getGreen(), selectedColor.getBlue(), null);
            applyColor(new Color(Color.HSBtoRGB(hue, hsb[1], hsb[2])));
        }
    }

    private void applyColor(Color color) {
        int alpha = selectedColor == null ? 255 : selectedColor.getAlpha();
        Color withAlpha = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
        this.selectedColor = withAlpha;
        if (onChange != null) onChange.accept(withAlpha);
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
