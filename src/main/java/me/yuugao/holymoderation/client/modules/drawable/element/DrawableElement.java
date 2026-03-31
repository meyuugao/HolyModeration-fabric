package me.yuugao.holymoderation.client.modules.drawable.element;

import me.yuugao.holymoderation.client.modules.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
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

    public void updateRender(DrawContext ctx, float parentW, float parentH, int z, float externalScale) {
        MatrixStack ms = ctx.getMatrices();
        ms.push();

        float anchorX = getAnchorX(parentW);
        float anchorY = getAnchorY(parentH);

        ms.translate(anchorX, anchorY, 0);
        ms.scale(externalScale, externalScale, 1f);
        ms.scale(scale, scale, 1f);
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

    public float[] screenToLocal(float mouseX, float mouseY, float parentW, float parentH, float externalScale) {
        float anchorX = getAnchorX(parentW);
        float anchorY = getAnchorY(parentH);
        float totalScale = this.scale * externalScale;

        float localX = (mouseX - anchorX) / totalScale + getBasePivotX();
        float localY = (mouseY - anchorY) / totalScale + getBasePivotY();

        return new float[]{localX, localY};
    }
}