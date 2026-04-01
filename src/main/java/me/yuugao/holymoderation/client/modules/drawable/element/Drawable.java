package me.yuugao.holymoderation.client.modules.drawable.element;

import me.yuugao.holymoderation.client.modules.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.MinecraftService;
import net.minecraft.client.gui.DrawContext;

import lombok.Getter;
import lombok.Setter;

public class Drawable implements MouseHoverable {
    protected final ServiceContext serviceContext;

    @Getter
    @Setter
    protected float width, height;
    @Getter
    protected float screenScale;

    @Getter
    protected float scale = 1f;

    protected float relX, relY;

    @Getter
    protected final PivotMode pivotMode;

    public Drawable(ServiceContext serviceContext, PivotMode pivotMode) {
        this.serviceContext = serviceContext;
        this.pivotMode = pivotMode;
    }

    public void setRelativePos(float relX, float relY) {
        this.relX = relX;
        this.relY = relY;
    }

    public float getScaledWidth() {
        return width * scale * screenScale;
    }

    public float getScaledHeight() {
        return height * scale * screenScale;
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

    protected float animate(float current, float target, float speed) {
        MinecraftService minecraftService = serviceContext.getMinecraftService();
        float delta = minecraftService.getClient().getLastFrameDuration();
        float step = (target - current) * delta * speed;

        if (Math.abs(step) < 0.0001f) return target;

        float result = current + step;
        if (step > 0) return Math.min(result, target);
        else return Math.max(result, target);
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
        float totalScale = scale * screenScale;

        float localX = (mouseX - anchorX) / totalScale + getBasePivotX();
        float localY = (mouseY - anchorY) / totalScale + getBasePivotY();

        return new float[]{localX, localY};
    }
}