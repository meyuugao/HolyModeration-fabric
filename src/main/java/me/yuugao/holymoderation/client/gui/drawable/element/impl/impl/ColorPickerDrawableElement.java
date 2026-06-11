package me.yuugao.holymoderation.client.gui.drawable.element.impl.impl;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.DrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.service.AnimationService;
import me.yuugao.holymoderation.client.util.service.Render2DService;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import org.jetbrains.annotations.Nullable;

import java.awt.Color;

public class ColorPickerDrawableElement extends DrawableElement {
    private final Render2DService render2DService;

    private Color outlineColor;

    private float radius;
    private float outlineWidth;

    public ColorPickerDrawableElement(AnimationService animationService, Render2DService render2DService) {
        super(animationService, PivotMode.CENTER);

        this.render2DService = render2DService;
    }

    @Override
    protected void render(DrawContext ctx, int z) {
        MatrixStack ms = ctx.getMatrices();

        render2DService.setupRender();

        render2DService.renderRGBPalette(ms, 0f, 0f, z, radius, outlineColor, outlineWidth);

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
        float centerX = getAnchorX(parentWidth);
        float centerY = getAnchorY(parentHeight);

        float dx = (float) (mouseX - centerX);
        float dy = (float) (mouseY - centerY);
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        float effectiveRadius = radius * scale.get();

        if (dist > effectiveRadius) return null;

        float angle = (float) Math.atan2(dy, dx);
        float hue = (angle + (float) Math.PI) / (2.0f * (float) Math.PI);
        float saturation = dist / effectiveRadius;

        return Color.getHSBColor(hue, saturation, 1.0f);
    }
}