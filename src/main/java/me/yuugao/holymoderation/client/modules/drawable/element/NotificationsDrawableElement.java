package me.yuugao.holymoderation.client.modules.drawable.element;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import net.minecraft.client.gui.DrawContext;

public class NotificationsDrawableElement extends DrawableElement {
    public NotificationsDrawableElement(ServiceContext serviceContext, PositionMode positionMode) {
        super(serviceContext, positionMode);
    }

    @Override
    protected void initPosition(DrawContext ctx) {}

    @Override
    protected void renderContent(DrawContext ctx, int z) {
        ctx.getMatrices().push();
        ctx.getMatrices().translate(-getX(ctx), -getY(ctx), 0f);
        serviceContext.getNotificationService().renderNotifications(ctx, z);
        ctx.getMatrices().pop();
    }
}