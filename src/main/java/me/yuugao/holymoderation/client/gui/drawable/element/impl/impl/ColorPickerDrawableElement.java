package me.yuugao.holymoderation.client.gui.drawable.element.impl.impl;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.DrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.ScreenCtx;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.service.AnimationService;
import me.yuugao.holymoderation.client.util.service.Render2DService;

import net.minecraft.client.gui.DrawContext;

import org.jetbrains.annotations.Nullable;

import java.awt.Color;
import java.util.function.Consumer;

public class ColorPickerDrawableElement extends DrawableElement {
    private final Render2DService render2DService;
    private final Consumer<Color> onChange;

    private Color outlineColor;
    private Color selectedColor;

    private float radius;
    private float outlineWidth;

    private boolean dragging = false;

    public ColorPickerDrawableElement(AnimationService animationService, Render2DService render2DService) {
        this(animationService, render2DService, null);
    }

    public ColorPickerDrawableElement(AnimationService animationService, Render2DService render2DService, Consumer<Color> onChange) {
        super(animationService, PivotMode.CENTER);

        this.render2DService = render2DService;
        this.onChange = onChange;
    }

    @Override
    protected void render(DrawContext ctx, int z) {
        render2DService.setupRender();

        render2DService.renderRGBPalette(ctx, 0f, 0f, z, radius, outlineColor, outlineWidth);

        if (selectedColor != null) {
            float cx = getWidth() / 2f;
            float cy = getHeight() / 2f;
            float ballR = radius * 0.20f;
            float ringW = Math.max(2f, radius * 0.055f);

            render2DService.renderSoftRoundedRect(ctx, cx - ballR - ringW, cy - ballR - ringW,
                    (ballR + ringW) * 2f, (ballR + ringW) * 2f, z, ballR + ringW, outlineColor, 0);
            render2DService.renderSoftRoundedRect(ctx, cx - ballR, cy - ballR,
                    ballR * 2f, ballR * 2f, z, ballR, selectedColor, 0);

            float hlR = ballR * 0.42f;
            render2DService.renderSoftRoundedRect(ctx, cx - ballR * 0.45f, cy - ballR * 0.58f,
                    hlR * 2f, hlR * 2f, z, hlR, new Color(255, 255, 255, 115), 0);
        }

        render2DService.endRender();
    }

    public void updateRenderForScreen(DrawContext ctx, float relX, float relY, int z,
                                      float radius, Color outlineColor, float outlineWidth) {
        updateRender(relX, relY, radius, outlineColor, outlineWidth);
        float screenScale = Math.min(ctx.getScaledWindowWidth() / 1280f, ctx.getScaledWindowHeight() / 720f);
        super.updateRenderForScreen(ctx, z, screenScale);
    }

    public void updateRenderForParent(DrawContext ctx, float relX, float relY, float parW, float parH,
                                      int z, float radius, Color outlineColor, float outlineWidth) {
        updateRender(relX, relY, radius, outlineColor, outlineWidth);
        super.updateRenderForParent(ctx, parW, parH, z);
    }

    private void updateRender(float relX, float relY, float radius, Color outlineColor, float outlineWidth) {
        setRelativePos(relX, relY);
        this.radius = radius;
        this.outlineColor = outlineColor;
        this.outlineWidth = outlineWidth;

        this.width = radius * 2 + outlineWidth * 2;
        this.height = radius * 2 + outlineWidth * 2;
    }

    @Override
    public boolean isMouseOver(float parentWidth, float parentHeight, double mouseX, double mouseY) {
        float centerX = getAnchorX(parentWidth);
        float centerY = getAnchorY(parentHeight);

        float dx = (float) (mouseX - centerX);
        float dy = (float) (mouseY - centerY);
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        return dist <= radius * scale.get();
    }

    @Nullable
    public Color getColorFromMouse(float parentWidth, float parentHeight, double mouseX, double mouseY) {
        return getColorFromMouse(parentWidth, parentHeight, mouseX, mouseY, false);
    }

    private Color getColorFromMouse(float parentWidth, float parentHeight, double mouseX, double mouseY, boolean clamp) {
        float centerX = getAnchorX(parentWidth);
        float centerY = getAnchorY(parentHeight);

        float dx = (float) (mouseX - centerX);
        float dy = (float) (mouseY - centerY);
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        float effectiveRadius = radius * scale.get();

        if (dist > effectiveRadius) {
            if (!clamp) return null;
            dist = effectiveRadius;
        }

        float angle = (float) Math.atan2(dy, dx);
        float hue = (angle + (float) Math.PI) / (2.0f * (float) Math.PI);
        float saturation = dist / effectiveRadius;

        return Color.getHSBColor(hue, saturation, 1.0f);
    }

    public void setSelectedColor(Color selectedColor) {
        this.selectedColor = selectedColor;
    }

    @Nullable
    public Color getSelectedColor() {
        return selectedColor;
    }

    @Override
    public boolean handleClick(ScreenCtx screen) {
        if (!isMouseOver(screen.screenWidth(), screen.screenHeight(), screen.mouseX(), screen.mouseY())) return false;
        dragging = true;
        applyColor(getColorFromMouse(screen.screenWidth(), screen.screenHeight(), screen.mouseX(), screen.mouseY(), true));
        return true;
    }

    public boolean handleDrag(float parentW, float parentH, double mouseX, double mouseY) {
        if (!dragging) return false;
        applyColor(getColorFromMouse(parentW, parentH, mouseX, mouseY, true));
        return true;
    }

    public void handleRelease() {
        dragging = false;
    }

    private void applyColor(Color color) {
        if (color == null) return;
        this.selectedColor = color;
        if (onChange != null) onChange.accept(color);
    }
}