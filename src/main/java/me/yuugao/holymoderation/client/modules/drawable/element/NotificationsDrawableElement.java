package me.yuugao.holymoderation.client.modules.drawable.element;

import me.yuugao.holymoderation.client.modules.drawable.element.state.NotificationsRenderState;
import me.yuugao.holymoderation.client.modules.drawable.element.state.provider.NotificationsRenderStateProvider;
import me.yuugao.holymoderation.client.modules.drawable.render.PositionMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.NotificationsService;

import net.minecraft.client.gui.DrawContext;

public class NotificationsDrawableElement extends DrawableElement<NotificationsRenderState> {
    public NotificationsDrawableElement(ServiceContext serviceContext, PositionMode positionMode) {
        super(serviceContext, positionMode, new NotificationsRenderStateProvider(serviceContext));
    }

    @Override
    protected void initPosition(DrawContext ctx) {
    }

    @Override
    protected void render(DrawContext ctx, int z, NotificationsRenderState state) {
        NotificationsService notificationsService = serviceContext.getNotificationsService();

        ctx.getMatrices().push();
        ctx.getMatrices().translate(-getX(ctx), -getY(ctx), 0f);
        notificationsService.renderNotifications(ctx, z);
        ctx.getMatrices().pop();
    }
}