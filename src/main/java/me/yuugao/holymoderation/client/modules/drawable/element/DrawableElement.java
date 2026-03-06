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
import lombok.Setter;

public abstract class DrawableElement<T extends RenderState> {
    protected final ServiceContext serviceContext;
    protected final RenderStateProvider<T> stateProvider;

    protected float relX;
    protected float relY;
    @Getter
    protected float widthScale = 1f, heightScale = 1f; //tip: todo: сделай возможность изменять scale от 0.3 до 3 +-
    protected PositionMode positionMode;
    protected boolean positioned = false;
    @Getter
    @Setter
    private float width, height;

    protected DrawableElement(ServiceContext serviceContext, PositionMode positionMode,
                              RenderStateProvider<T> stateProvider) {
        this.serviceContext = serviceContext;
        this.positionMode = positionMode;
        this.stateProvider = stateProvider;
    }

    protected abstract void initPosition(DrawContext ctx);

    protected abstract void render(DrawContext ctx, int z, T state);

    public void updateRender(DrawContext ctx, int z, RenderMode mode) {
        if (!positioned) {
            initPosition(ctx);
            positioned = true;
        }

        T state = stateProvider.getState(mode);
        if (state == null) return;

        float scale = getCurrentScale();
        float[] scalePivot = getScalePivot();

        MatrixStack ms = ctx.getMatrices();
        ms.push();

        float x = getX(ctx);
        float y = getY(ctx);

        ms.translate(x - scalePivot[0] * scale, y - scalePivot[1] * scale, 0);
        ms.scale(scale, scale, 1f);

        render(ctx, z, state);

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

    public float getScaledWidth() {
        return width * widthScale;
    }

    public float getScaledHeight() {
        return height * heightScale;
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
        };
    }

    protected float getCurrentScale() {
        return 1f;
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