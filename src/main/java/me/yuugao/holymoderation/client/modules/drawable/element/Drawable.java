package me.yuugao.holymoderation.client.modules.drawable.element;

import me.yuugao.holymoderation.client.modules.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.MinecraftService;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import lombok.Getter;
import lombok.Setter;

public class Drawable {
    protected final ServiceContext serviceContext;

    protected float relX;
    protected float relY;

    @Getter
    protected float widthScale = 1f, heightScale = 1f; //tip: todo: сделай возможность изменять scale от 0.3 до 3 +-
    protected float screenScale = 1f;
    protected float globalScale = 1f;

    protected PivotMode positionMode;

    @Getter
    @Setter
    private float width, height;

    public Drawable(ServiceContext serviceContext, PivotMode pivotMode) {
        this.serviceContext = serviceContext;
        this.positionMode = pivotMode;
    }

    protected void prepareMatrixForParent(DrawContext ctx, float parentWidth, float parentHeight) {
        float x = getX(parentWidth);
        float y = getY(parentHeight);

        MatrixStack ms = ctx.getMatrices();

        this.screenScale = Math.min(ctx.getScaledWindowWidth() / 1920f, ctx.getScaledWindowHeight() / 1080f);

        float[] scalePivot = getScalePivot();

        ms.translate(x - scalePivot[0] / screenScale, y - scalePivot[1] / screenScale, 0);
        ms.scale(globalScale, globalScale, 1f);
    }

    protected void prepareMatrixForScreen(DrawContext ctx) {
        float x = getX(ctx.getScaledWindowWidth());
        float y = getY(ctx.getScaledWindowHeight());

        MatrixStack ms = ctx.getMatrices();

        this.screenScale = Math.min(ctx.getScaledWindowWidth() / 1920f, ctx.getScaledWindowHeight() / 1080f);

        float[] scalePivot = getScalePivot();

        ms.translate(x, y, 0);
        ms.scale(screenScale, screenScale, 1f);
        ms.translate(-x, -y, 0);

        ms.translate(x - scalePivot[0] / screenScale, y - scalePivot[1] / screenScale, 0);
        ms.scale(globalScale, globalScale, 1f);
    }

    public float getX(float parentWidth) {
        return relX * parentWidth;
    }

    public float getY(float parentHeight) {
        return relY * parentHeight;
    }

    public void setX(float pixelX, float parentWidth) {
        relX = pixelX / parentWidth;
    }

    public void setY(float pixelY, float parentHeight) {
        relY = pixelY / parentHeight;
    }

    public float getScaledWidth() {
        return width * widthScale * globalScale * screenScale;
    }

    public float getScaledHeight() {
        return height * heightScale * globalScale * screenScale;
    }

    public float[] getScalePivot() {
        return switch (positionMode) {
            case LEFT_UP -> new float[]{0f, 0f};
            case LEFT_DOWN -> new float[]{0f, getScaledHeight()};
            case RIGHT_UP -> new float[]{getScaledWidth(), 0f};
            case RIGHT_DOWN -> new float[]{getScaledWidth(), getScaledHeight()};
            case UP -> new float[]{getScaledWidth() / 2f, 0f};
            case DOWN -> new float[]{getScaledWidth() / 2f, getScaledHeight()};
            case LEFT -> new float[]{0f, getScaledHeight() / 2f};
            case RIGHT -> new float[]{getScaledWidth(), getScaledHeight() / 2f};
            case CENTER -> new float[]{getScaledWidth() / 2f, getScaledHeight() / 2f};
        };
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
}