package me.yuugao.holymoderation.client.modules.drawable.element;

import me.yuugao.holymoderation.client.modules.drawable.element.state.RenderState;
import me.yuugao.holymoderation.client.modules.drawable.element.state.provider.RenderStateProvider;
import me.yuugao.holymoderation.client.modules.drawable.render.PositionMode;
import me.yuugao.holymoderation.client.modules.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.MinecraftService;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import lombok.Getter;

public abstract class DrawableElement<T extends RenderState> {
    protected final ServiceContext serviceContext;
    protected final RenderStateProvider<T> stateProvider;

    protected float relX;
    protected float relY;

    @Getter
    protected float width, height;

    @Getter
    protected float widthScale = 1f, heightScale = 1f; //tip: todo: сделай возможность изменять scale от 0.3 до 3 +-

    protected PositionMode positionMode;
    protected boolean positioned = false;

    protected DrawableElement(ServiceContext serviceContext, PositionMode positionMode,
                              RenderStateProvider<T> stateProvider) {
        this.serviceContext = serviceContext;
        this.positionMode = positionMode;
        this.stateProvider = stateProvider;
    }

    protected abstract void initPosition(DrawContext ctx);

    protected abstract void render(DrawContext ctx, int z, T state);

    public final void updateRender(DrawContext ctx, int z, RenderMode mode) {
        if (!positioned) {
            initPosition(ctx);
            positioned = true;
        }

        MatrixStack ms = ctx.getMatrices();
        ms.push();
        ms.translate(getX(ctx), getY(ctx), 0f);
        ms.scale(widthScale, heightScale, 1f);

        T state = stateProvider.getState(mode);
        if (state != null) {
            render(ctx, z, state);
        }

        ms.pop();
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