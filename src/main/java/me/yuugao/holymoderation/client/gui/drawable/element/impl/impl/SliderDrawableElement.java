package me.yuugao.holymoderation.client.gui.drawable.element.impl.impl;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.DrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.ScreenCtx;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.service.AnimationService;
import me.yuugao.holymoderation.client.util.service.Render2DService;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.util.function.Consumer;

public class SliderDrawableElement extends DrawableElement {
    private static final float TRACK_HEIGHT = 6f;
    private static final float KNOB_RADIUS = 7f;
    private static final float KNOB_PADDING = 3f;

    private final Render2DService render2DService;
    private final Consumer<Float> onChange;
    private final AnimationService.Value knobAnim;

    private float min = 0f;
    private float max = 1f;
    private float value = 0f;
    private float step = 0.01f;

    private float radius = 3f;
    private float outlineWidth = 2f;
    private float blurWidth = 3f;

    private Color trackColor;
    private Color fillColor;
    private Color knobColor;
    private Color outlineColor;

    private boolean dragging = false;

    public SliderDrawableElement(AnimationService animationService, Render2DService render2DService, Consumer<Float> onChange) {
        super(animationService, PivotMode.LEFT_UP);

        this.render2DService = render2DService;
        this.onChange = onChange;
        this.knobAnim = animationService.createValue(fraction());
    }

    public void setRange(float min, float max) {
        this.min = min;
        this.max = max;
        this.value = clamp(value);
        knobAnim.reset(fraction());
    }

    public void setStep(float step) {
        this.step = step;
    }

    public void setValue(float value) {
        float next = clamp(value);
        if (Float.compare(next, this.value) == 0) {
            knobAnim.reset(fraction());
            return;
        }
        this.value = next;
        knobAnim.setTarget(fraction());
        if (onChange != null) onChange.accept(this.value);
    }

    public float getValue() {
        return value;
    }

    public float getFraction() {
        return fraction();
    }

    public boolean isDragging() {
        return dragging;
    }

    private float fraction() {
        float range = max - min;
        return range <= 0f ? 0f : clamp01((value - min) / range);
    }

    @Override
    protected void render(DrawContext ctx, int z) {
        MatrixStack ms = ctx.getMatrices();
        knobAnim.update();

        float f = clamp01(knobAnim.get());
        float trackY = (getHeight() - TRACK_HEIGHT) / 2f;
        float fillWidth = getWidth() * f;

        render2DService.setupRender();

        render2DService.renderSoftRoundedRectOutline(ms, 0f, trackY, getWidth(), TRACK_HEIGHT, z,
                radius, trackColor, outlineColor, outlineWidth, blurWidth);
        if (fillWidth > 1f) {
            render2DService.renderSoftRoundedRect(ms, 0f, trackY, Math.min(fillWidth, getWidth()), TRACK_HEIGHT, z,
                    radius, fillColor, (int) blurWidth);
        }

        float knobX = getWidth() * f - KNOB_RADIUS;
        float knobY = getHeight() / 2f - KNOB_RADIUS;
        render2DService.renderSoftRoundedRectOutline(ms, knobX, knobY, KNOB_RADIUS * 2f, KNOB_RADIUS * 2f, z,
                KNOB_RADIUS, knobColor, outlineColor, outlineWidth, blurWidth);

        render2DService.endRender();
    }

    public void updateRenderForParent(DrawContext ctx, float relX, float relY, float width,
                                      float parW, float parH, int z, float radius,
                                      Color trackColor, Color fillColor, Color knobColor,
                                      Color outlineColor, float outlineWidth, float blurWidth) {
        setRelativePos(relX, relY);
        setWidth(width);
        setHeight(KNOB_RADIUS * 2f + KNOB_PADDING * 2f);

        this.radius = radius;
        this.trackColor = trackColor;
        this.fillColor = fillColor;
        this.knobColor = knobColor;
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
        dragging = true;
        updateFromMouse(parentW, parentH, (float) mouseX);
        return true;
    }

    public boolean handleDrag(float parentW, float parentH, double mouseX, double mouseY) {
        if (!dragging) return false;
        updateFromMouse(parentW, parentH, (float) mouseX);
        return true;
    }

    public void handleRelease() {
        dragging = false;
    }

    public boolean handleScroll(float parentW, float parentH, double dy, double mouseX, double mouseY) {
        if (!isMouseOver(parentW, parentH, mouseX, mouseY)) return false;
        float delta = step > 0f ? (float) (-dy * step) : (float) (-dy * (max - min) * 0.05f);
        setValue(clamp(value + delta));
        return true;
    }

    private void updateFromMouse(float parentW, float parentH, float mouseX) {
        float[] local = screenToLocal(parentW, parentH, mouseX, 0f);
        float f = clamp01(local[0] / getWidth());

        float range = max - min;
        float next = min + f * range;
        if (step > 0f) next = Math.round(next / step) * step;

        setValue(clamp(next));
    }

    private float clamp(float v) {
        return Math.max(min, Math.min(max, v));
    }

    private static float clamp01(float v) {
        return Math.max(0f, Math.min(1f, v));
    }
}
