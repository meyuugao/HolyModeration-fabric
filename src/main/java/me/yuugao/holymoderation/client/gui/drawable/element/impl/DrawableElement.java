package me.yuugao.holymoderation.client.gui.drawable.element.impl;

import me.yuugao.holymoderation.client.gui.drawable.element.Drawable;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.service.AnimationService;

import net.minecraft.client.gui.DrawContext;

import org.joml.Matrix3x2fStack;

import lombok.Getter;
import lombok.Setter;

public abstract class DrawableElement extends Drawable {
    @Getter
    @Setter
    private boolean draggable = true;

    protected DrawableElement(AnimationService animationService, PivotMode pivotMode) {
        super(animationService, pivotMode);
    }

    protected abstract void render(DrawContext ctx, int z);

    public void updateRender(DrawContext ctx, float parentW, float parentH, int z, float screenScale) {
        this.screenScale = screenScale;

        Matrix3x2fStack ms = ctx.getMatrices();

        ms.pushMatrix();

        float anchorX = getAnchorX(parentW);
        float anchorY = getAnchorY(parentH);

        ms.translate(anchorX, anchorY);
        ms.scale(screenScale, screenScale);
        ms.scale(scale.get(), scale.get());
        ms.translate(-getBasePivotX(), -getBasePivotY());

        render(ctx, z);

        ms.popMatrix();
    }

    public void updateRenderForScreen(DrawContext ctx, int z, float screenScale) {
        updateRender(ctx, ctx.getScaledWindowWidth(), ctx.getScaledWindowHeight(), z, screenScale);
    }

    public void updateRenderForParent(DrawContext ctx, float parW, float parH, int z) {
        updateRender(ctx, parW, parH, z, 1f);
    }

    public boolean handleClick(ScreenCtx screen) {
        return false;
    }
}