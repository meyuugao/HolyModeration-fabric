package me.yuugao.holymoderation.client.gui.drawable.element;

import me.yuugao.holymoderation.client.gui.drawable.MouseHoverable;
import me.yuugao.holymoderation.client.gui.drawable.MouseScrollable;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.AnimationService;

import net.minecraft.client.gui.DrawContext;

import lombok.Getter;
import lombok.Setter;

public class Drawable implements MouseHoverable, MouseScrollable {
    protected final ServiceContext serviceContext;
    protected final AnimationService animationService;

    @Getter
    @Setter
    protected float width, height;
    @Getter
    protected float screenScale;

    protected final AnimationService.Value scale;

    protected float relX, relY;

    @Getter
    protected final PivotMode pivotMode;

    public Drawable(ServiceContext serviceContext, PivotMode pivotMode) {
        this.serviceContext = serviceContext;
        this.pivotMode = pivotMode;
        this.animationService = serviceContext.getAnimationService();
        this.scale = serviceContext.getAnimationService().createValue(1f);
    }

    public float getScale() {
        return scale.get();
    }

    public void setRelativePos(float relX, float relY) {
        this.relX = relX;
        this.relY = relY;
    }

    public float getScaledWidth() {
        return width * scale.get() * screenScale;
    }

    public float getScaledHeight() {
        return height * scale.get() * screenScale;
    }

    public float getBasePivotX() {
        return pivotMode.getXFactor() * width;
    }

    public float getBasePivotY() {
        return pivotMode.getYFactor() * height;
    }

    public float getPivotOffsetX() {
        return pivotMode.getXFactor() * getScaledWidth();
    }

    public float getPivotOffsetY() {
        return pivotMode.getYFactor() * getScaledHeight();
    }

    public float getAnchorX(float parentWidth) {
        return parentWidth * relX;
    }

    public float getAnchorY(float parentHeight) {
        return parentHeight * relY;
    }

    public float getAbsoluteX(float parentWidth) {
        return getAnchorX(parentWidth) - getPivotOffsetX();
    }

    public float getAbsoluteY(float parentHeight) {
        return getAnchorY(parentHeight) - getPivotOffsetY();
    }

    @Override
    public boolean isMouseOver(float parentWidth, float parentHeight, double mouseX, double mouseY) {
        float x = getAbsoluteX(parentWidth);
        float y = getAbsoluteY(parentHeight);
        return mouseX >= x && mouseX <= x + getScaledWidth() && mouseY >= y && mouseY <= y + getScaledHeight();
    }

    @Override
    public boolean isGlobalMouseOver(float parX, float parY, float parW, float parH, double mouseX, double mouseY) {
        return isMouseOver(parW, parH, mouseX, mouseY);
    }

    @Override
    public boolean isGlobalMouseOver(DrawContext ctx, double mouseX, double mouseY) {
        return isMouseOver(ctx.getScaledWindowWidth(), ctx.getScaledWindowHeight(), mouseX, mouseY);
    }

    @Override
    public boolean isRelMouseOver(float parW, float parH, double mouseX, double mouseY) {
        return isMouseOver(parW, parH, mouseX, mouseY);
    }

    public float[] screenToLocal(float parW, float parH, float mouseX, float mouseY) {
        float anchorX = getAnchorX(parW);
        float anchorY = getAnchorY(parH);
        float totalScale = scale.get() * screenScale;

        float localX = (mouseX - anchorX) / totalScale + getBasePivotX();
        float localY = (mouseY - anchorY) / totalScale + getBasePivotY();

        return new float[]{localX, localY};
    }

    @Override
    public void onMouseScroll(double dx, double dy, int x, int y) {}
}