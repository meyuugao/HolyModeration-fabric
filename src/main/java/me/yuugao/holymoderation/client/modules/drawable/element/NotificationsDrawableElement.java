package me.yuugao.holymoderation.client.modules.drawable.element;

import me.yuugao.holymoderation.client.modules.drawable.element.state.NotificationsRenderState;
import me.yuugao.holymoderation.client.modules.drawable.element.state.provider.NotificationsRenderStateProvider;
import me.yuugao.holymoderation.client.modules.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import net.minecraft.client.gui.DrawContext;

public class NotificationsDrawableElement extends DrawableElement<NotificationsRenderState> {
    public NotificationsDrawableElement(ServiceContext serviceContext, PivotMode positionMode) {
        super(serviceContext, positionMode, new NotificationsRenderStateProvider(serviceContext));
    }

    @Override
    protected void initPosition(DrawContext ctx) {
        relX = switch (positionMode) {
            case LEFT_UP, LEFT, LEFT_DOWN -> 0f;
            case RIGHT_UP, RIGHT, RIGHT_DOWN -> 1f;
            default -> 0.5f;
        };
        relY = switch (positionMode) {
            case LEFT_UP, UP, RIGHT_UP -> 0f;
            case LEFT_DOWN, DOWN, RIGHT_DOWN -> 1f;
            default -> 0.5f;
        };
    }

    @Override
    protected void render(DrawContext ctx, int z, NotificationsRenderState state) {
        float localW = ctx.getScaledWindowWidth() / widthScale;
        float localH = ctx.getScaledWindowHeight() / heightScale;

        float left = -relX * localW;
        float right = (1 - relX) * localW;
        float top = -relY * localH;
        float bottom = (1 - relY) * localH;

        float[] cfg = getConfig();
        serviceContext.getNotificationsService().renderNotificationsLocal(
                ctx, z, cfg[0], cfg[1], cfg[2], left, right, top, bottom, localW);
    }

    @Override
    public float[] getScalePivot() {
        return new float[]{0f, 0f};
    }

    private float[] getConfig() {
        return switch (positionMode) {
            case RIGHT_DOWN, RIGHT, DOWN, UP, CENTER -> new float[]{-1f, 1f, 0f};
            case RIGHT_UP -> new float[]{1f, 1f, 0f};
            case LEFT_DOWN, LEFT -> new float[]{-1f, -1f, 0f};
            case LEFT_UP -> new float[]{1f, -1f, 0f};
        };
    }
}