package me.yuugao.holymoderation.client.gui.drawable.element.impl.impl;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.DrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.ScreenCtx;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.service.AnimationService;
import me.yuugao.holymoderation.client.util.service.Render2DService;

import net.minecraft.client.gui.DrawContext;

import java.awt.Color;
import java.util.function.Consumer;

public class ToggleDrawableElement extends DrawableElement {
    private static final float THUMB_PADDING = 2f;

    private final Render2DService render2DService;
    private final Consumer<Boolean> onChange;
    private final AnimationService.Value thumbAnim;

    private boolean enabled = false;

    private float radius;
    private float outlineWidth;
    private float blurWidth;

    private Color onColor;
    private Color offColor;
    private Color thumbColor;
    private Color outlineColor;

    public ToggleDrawableElement(AnimationService animationService, Render2DService render2DService, Consumer<Boolean> onChange) {
        super(animationService, PivotMode.LEFT_UP);

        this.render2DService = render2DService;
        this.onChange = onChange;
        this.thumbAnim = animationService.createValue(0f);
    }

    public void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            thumbAnim.setTarget(enabled ? 1f : 0f);
            return;
        }
        this.enabled = enabled;
        thumbAnim.setTarget(enabled ? 1f : 0f);
        if (onChange != null) onChange.accept(enabled);
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    protected void render(DrawContext ctx, int z) {
        thumbAnim.update();
        float f = thumbAnim.get();

        Color track = enabled ? onColor : offColor;

        render2DService.renderSoftRoundedRectOutline(ctx, 0f, 0f, getWidth(), getHeight(), z,
                radius, track, outlineColor, outlineWidth, blurWidth);

        float pad = THUMB_PADDING + outlineWidth;
        float thumbSize = getHeight() - pad * 2f;
        float travel = getWidth() - thumbSize - pad * 2f;
        float thumbX = pad + travel * f;

        render2DService.renderSoftRoundedRectOutline(ctx, thumbX, pad, thumbSize, thumbSize, z,
                thumbSize / 2f, thumbColor, outlineColor, outlineWidth, blurWidth);
    }

    public void updateRenderForParent(DrawContext ctx, float relX, float relY, float width, float height,
                                      float parW, float parH, int z,
                                      Color onColor, Color offColor, Color thumbColor, Color outlineColor,
                                      float outlineWidth, float blurWidth) {
        setRelativePos(relX, relY);
        setWidth(width);
        setHeight(height);

        this.radius = height / 2f;
        this.onColor = onColor;
        this.offColor = offColor;
        this.thumbColor = thumbColor;
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
        setEnabled(!enabled);
        return true;
    }
}
