package me.yuugao.holymoderation.client.modules.drawable.element;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import lombok.Getter;

public abstract class DrawableElement {
    protected final ServiceContext serviceContext;

    protected float relX;
    protected float relY;

    @Getter
    protected float width, height;

    @Getter
    protected float widthScale = 1f, heightScale = 1f; //tip: todo: сделай возможность изменять scale от 0.3 до 3 +-
    protected PositionMode positionMode;
    protected boolean positioned = false;

    protected DrawableElement(ServiceContext serviceContext, PositionMode positionMode) {
        this.serviceContext = serviceContext;
        this.positionMode = positionMode;
    }

    protected abstract void renderContent(DrawContext ctx, int z);

    protected abstract void initPosition(DrawContext ctx);

    public final void render(DrawContext ctx, int z) {
        if (!positioned) {
            initPosition(ctx);
            positioned = true;
        }

        float x = getX(ctx);
        float y = getY(ctx);

        MatrixStack ms = ctx.getMatrices();
        ms.push();
        ms.translate(x, y, 0f);
        ms.scale(widthScale, heightScale, 0f);
        try {
            renderContent(ctx, z);
        } finally {
            ms.pop();
        }
    }

    public float getX(DrawContext ctx) {
        return relX * ctx.getScaledWindowWidth();
    }

    public float getY(DrawContext ctx) {
        return relY * ctx.getScaledWindowHeight();
    }

    public void setX(float pixelX, DrawContext ctx) {
        relX = pixelX / ctx.getScaledWindowWidth();
    }

    public void setY(float pixelY, DrawContext ctx) {
        relY = pixelY / ctx.getScaledWindowHeight();
    }

    public float[] topLeftLocal() {
        return switch (positionMode) {
            case CENTER -> new float[]{-width / 2f, -height / 2f};
            case LEFT_UP -> new float[]{0f, 0f};
            case LEFT_DOWN -> new float[]{0f, -height};
            case RIGHT_UP -> new float[]{-width, 0f};
            case RIGHT_DOWN -> new float[]{-width, -height};
            case UP -> new float[]{-width / 2f, 0f};
            case DOWN -> new float[]{-width / 2f, -height};
            case LEFT -> new float[]{0f, -height / 2f};
            case RIGHT -> new float[]{-width, -height / 2f};
        };
    }

    public float[] scalePivotLocal() {
        return switch (positionMode) {
            case CENTER -> new float[]{width / 2f, height / 2f};
            case LEFT_UP -> new float[]{0f, 0f};
            case LEFT_DOWN -> new float[]{0f, height};
            case RIGHT_UP -> new float[]{width, 0f};
            case RIGHT_DOWN -> new float[]{width, height};
            case UP -> new float[]{width / 2f, 0f};
            case DOWN -> new float[]{width / 2f, height};
            case LEFT -> new float[]{0f, height / 2f};
            case RIGHT -> new float[]{width, height / 2f};
        };
    }
}