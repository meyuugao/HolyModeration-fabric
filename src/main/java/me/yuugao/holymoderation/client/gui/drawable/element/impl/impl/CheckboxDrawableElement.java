package me.yuugao.holymoderation.client.gui.drawable.element.impl.impl;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.DrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.ScreenCtx;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.service.AnimationService;
import me.yuugao.holymoderation.client.util.service.Render2DService;

import net.minecraft.client.gui.DrawContext;

import org.joml.Matrix3x2fStack;

import java.awt.Color;
import java.util.function.Consumer;

public class CheckboxDrawableElement extends DrawableElement {
    private final Render2DService render2DService;
    private final Consumer<Boolean> onChange;

    private boolean checked = false;

    private float radius;
    private float outlineWidth;
    private float blurWidth;

    private Color boxColor;
    private Color activeColor;
    private Color checkColor;
    private Color outlineColor;

    public CheckboxDrawableElement(AnimationService animationService, Render2DService render2DService, Consumer<Boolean> onChange) {
        super(animationService, PivotMode.LEFT_UP);

        this.render2DService = render2DService;
        this.onChange = onChange;
    }

    public void setChecked(boolean checked) {
        if (this.checked == checked) return;
        this.checked = checked;
        if (onChange != null) onChange.accept(checked);
    }

    public boolean isChecked() {
        return checked;
    }

    @Override
    protected void render(DrawContext ctx, int z) {
        render2DService.renderSoftRoundedRectOutline(ctx, 0f, 0f, getWidth(), getHeight(), z,
                radius, checked ? activeColor : boxColor, outlineColor, outlineWidth, blurWidth);

        if (checked) {
            renderCheckmark(ctx, z);
        }
    }

    private void renderCheckmark(DrawContext ctx, int z) {
        Matrix3x2fStack ms = ctx.getMatrices();
        float w = getWidth();
        float h = getHeight();
        float stroke = Math.max(1.5f, w * 0.1f);

        renderCheckSegment(ctx, ms, z, w * 0.27f, h * 0.52f, w * 0.44f, h * 0.69f, stroke);
        renderCheckSegment(ctx, ms, z, w * 0.44f, h * 0.69f, w * 0.74f, h * 0.32f, stroke);
    }

    private void renderCheckSegment(DrawContext ctx, Matrix3x2fStack ms, int z,
                                    float x1, float y1, float x2, float y2, float stroke) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        float angle = (float) Math.atan2(dy, dx);
        float midX = (x1 + x2) / 2f;
        float midY = (y1 + y2) / 2f;

        ms.pushMatrix();
        ms.rotateAbout(angle, midX, midY);
        render2DService.renderSoftRoundedRect(ctx, midX - len / 2f, midY - stroke / 2f, len, stroke, z,
                stroke / 2f, checkColor, 0);
        ms.popMatrix();
    }

    public void updateRenderForParent(DrawContext ctx, float relX, float relY, float size,
                                      float parW, float parH, int z, float radius,
                                      Color boxColor, Color activeColor, Color checkColor, Color outlineColor,
                                      float outlineWidth, float blurWidth) {
        setRelativePos(relX, relY);
        setWidth(size);
        setHeight(size);

        this.radius = radius;
        this.boxColor = boxColor;
        this.activeColor = activeColor;
        this.checkColor = checkColor;
        this.outlineColor = outlineColor;
        this.outlineWidth = outlineWidth;
        this.blurWidth = blurWidth;

        super.updateRenderForParent(ctx, parW, parH, z);
    }

    @Override
    public boolean handleClick(ScreenCtx screen) {
        return handleClick(screen.screenWidth(), screen.screenHeight(), screen.mouseX(), screen.mouseY());
    }

    public boolean handleClick(float parentW, float parentH, double mouseX, double mouseY) {
        if (!isMouseOver(parentW, parentH, mouseX, mouseY)) return false;
        setChecked(!checked);
        return true;
    }
}
