package me.yuugao.holymoderation.client.gui.drawable.element.impl.impl;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.StatefulDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.state.impl.NotificationsRenderState;
import me.yuugao.holymoderation.client.gui.drawable.element.state.provider.impl.NotificationsRenderStateProvider;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class NotificationsDrawableElement extends StatefulDrawableElement<NotificationsRenderState> {
    public NotificationsDrawableElement(ServiceContext serviceContext, PivotMode positionMode) {
        super(serviceContext, positionMode, new NotificationsRenderStateProvider(serviceContext));
    }

    @Override
    protected void initPosition(DrawContext ctx) {
        setRelativePos(pivotMode.getXFactor(), pivotMode.getYFactor());
    }

    @Override
    protected void render(DrawContext ctx, int z, NotificationsRenderState state) {
    }

    @Override
    public void updateRender(DrawContext ctx, float parentW, float parentH, int z, float screenScale) {
        this.screenScale = screenScale;

        MatrixStack ms = ctx.getMatrices();
        ms.push();
        ms.scale(screenScale, screenScale, 1f);

        float w = parentW / screenScale;
        float h = parentH / screenScale;
        float[] cfg = getConfig();
        serviceContext.getNotificationsService().renderNotificationsLocal(
                ctx, z, cfg[0], cfg[1], cfg[2], w, h);

        ms.pop();
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