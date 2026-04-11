package me.yuugao.holymoderation.client.gui.drawable.element.impl.impl;


import me.yuugao.holymoderation.client.gui.drawable.element.impl.StatefulDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.state.impl.NotificationsRenderState;
import me.yuugao.holymoderation.client.gui.drawable.element.state.provider.impl.NotificationsRenderStateProvider;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import net.minecraft.client.gui.DrawContext;

public class NotificationsDrawableElement extends StatefulDrawableElement<NotificationsRenderState> {
    public NotificationsDrawableElement(ServiceContext serviceContext, PivotMode positionMode) {
        super(serviceContext, positionMode, new NotificationsRenderStateProvider(serviceContext));
    }

    @Override
    protected void initPosition(DrawContext ctx) {
        setRelativePos(pivotMode.getXFactor(), pivotMode.getYFactor());
        this.scale.reset(0f);
    }

    @Override
    protected void render(DrawContext ctx, int z, NotificationsRenderState state) {
        float sw = ctx.getScaledWindowWidth();
        float sh = ctx.getScaledWindowHeight();

        float anchorX = getAnchorX(sw);
        float anchorY = getAnchorY(sh);

        float left = anchorX - (pivotMode.getXFactor() * sw);
        float right = anchorX + ((1 - pivotMode.getXFactor()) * sw);
        float top = anchorY - (pivotMode.getYFactor() * sh);
        float bottom = anchorY + ((1 - pivotMode.getYFactor()) * sh);

        float[] cfg = getConfig();
        serviceContext.getNotificationsService().renderNotificationsLocal(
                ctx, z, cfg[0], cfg[1], cfg[2], left, right, top, bottom, sw);
    }

    private float[] getConfig() {
        return switch (getPivotMode()) {
            case RIGHT_DOWN, RIGHT, DOWN, UP, CENTER -> new float[]{-1f, 1f, 0f};
            case RIGHT_UP -> new float[]{1f, 1f, 0f};
            case LEFT_DOWN, LEFT -> new float[]{-1f, -1f, 0f};
            case LEFT_UP -> new float[]{1f, -1f, 0f};
        };
    }
}