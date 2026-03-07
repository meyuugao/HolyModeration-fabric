package me.yuugao.holymoderation.client.modules.drawable.element.impl;

import me.yuugao.holymoderation.client.modules.drawable.element.DrawableElement;
import me.yuugao.holymoderation.client.modules.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.Render2DService;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import org.jetbrains.annotations.Nullable;

import java.awt.Color;

public class ColorPickerDrawableElement extends DrawableElement {
    private float radius;
    private float outlineWidth;
    private Color outlineColor;
    private float centerX, centerY;

    public ColorPickerDrawableElement(ServiceContext serviceContext, PivotMode pivotMode) {
        super(serviceContext, pivotMode);
    }

    @Override
    protected void render(DrawContext ctx, int z) {
        Render2DService render2DService = serviceContext.getRender2DService();

        MatrixStack ms = ctx.getMatrices();

        render2DService.setupRender();

        ms.push();

        render2DService.renderRGBPalette(ms, 0f, 0f, z, radius, outlineColor, outlineWidth);

        ms.pop();

        render2DService.endRender();
    }

    public void updateRender(DrawContext ctx, float relX, float relY, float parentWidth, float parentHeight, int z,
                             float relScale, Color outlineColor, float outlineWidth) {
        this.relX = relX;
        this.relY = relY;
        this.radius = Math.min(parentWidth, parentHeight) * relScale;
        this.outlineColor = outlineColor;
        this.outlineWidth = outlineWidth;

        this.centerX = getX(parentWidth);
        this.centerY = getY(parentHeight);

        this.setWidth(radius * 2 + outlineWidth * 2);
        this.setHeight(radius * 2 + outlineWidth * 2);

        super.updateRenderForParent(ctx, parentWidth, parentHeight, z);
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        float dx = (float) (mouseX - centerX);
        float dy = (float) (mouseY - centerY);
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        return dist <= radius;
    }

    @Nullable
    public Color getColorFromMouse(double mouseX, double mouseY) {
        float dx = (float) (mouseX - centerX);
        float dy = (float) (mouseY - centerY);
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist > radius) return null;

        float angle = (float) Math.atan2(dy, dx);
        float hue = (angle + (float) Math.PI) / (2.0f * (float) Math.PI);
        float saturation = dist / radius;
        float value = 1.0f;

        return Color.getHSBColor(hue, saturation, value);
    }
}