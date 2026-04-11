package me.yuugao.holymoderation.client.gui.drawable.element.impl;

import me.yuugao.holymoderation.client.gui.drawable.element.Drawable;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.Render2DService;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import lombok.Getter;
import lombok.Setter;

public abstract class DrawableElement extends Drawable {
    @Getter @Setter
    private boolean draggable = true;

    protected DrawableElement(ServiceContext serviceContext, PivotMode pivotMode) {
        super(serviceContext, pivotMode);
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
}