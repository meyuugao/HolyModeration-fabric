package me.yuugao.holymoderation.client.modules.drawable.element;

import me.yuugao.holymoderation.client.modules.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public abstract class DrawableElement extends Drawable {
    protected DrawableElement(ServiceContext serviceContext, PivotMode pivotMode) {
        super(serviceContext, pivotMode);
    }

    protected abstract void render(DrawContext ctx, int z);

    public void updateRenderForParent(DrawContext ctx, float parentWidth, float parentHeight, int z) {
        MatrixStack ms = ctx.getMatrices();

        ms.push();

        prepareMatrixForParent(ctx, parentWidth, parentHeight);

        render(ctx, z);

        ms.pop();
    }

    public void updateRenderForScreen(DrawContext ctx, int z) {
        MatrixStack ms = ctx.getMatrices();

        ms.push();

        prepareMatrixForScreen(ctx);

        render(ctx, z);

        ms.pop();
    }
}