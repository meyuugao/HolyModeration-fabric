package me.yuugao.holymoderation.client.gui.drawable.element.impl;

import me.yuugao.holymoderation.client.gui.drawable.element.Drawable;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.service.AnimationService;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

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

        MatrixStack ms = ctx.getMatrices();

        ms.push();

        float anchorX = getAnchorX(parentW);
        float anchorY = getAnchorY(parentH);

        ms.translate(anchorX, anchorY, 0);
        ms.scale(screenScale, screenScale, 1f);
        ms.scale(scale.get(), scale.get(), 1f);
        ms.translate(-getBasePivotX(), -getBasePivotY(), 0);

        render(ctx, z);

        ms.pop();
    }

    public void updateRenderForScreen(DrawContext ctx, int z, float screenScale) {
        updateRender(ctx, ctx.getScaledWindowWidth(), ctx.getScaledWindowHeight(), z, screenScale);
    }

    public void updateRenderForParent(DrawContext ctx, float parW, float parH, int z) {
        updateRender(ctx, parW, parH, z, 1f);
    }

    /**
     * Hit-test a click in screen space and handle it if this element (or a child) is under the cursor.
     * Returns true if the click was consumed (a button fired), false to let the caller try others.
     * Base implementation is a no-op; interactive elements override this.
     */
    public boolean handleClick(ScreenCtx screen) {
        return false;
    }
}