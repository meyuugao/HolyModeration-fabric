package me.yuugao.holymoderation.client.modules.drawable.element;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import net.minecraft.client.gui.DrawContext;

public class NotificationsDrawableElement extends DrawableElement {
    public NotificationsDrawableElement(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Override
    public void renderContent(DrawContext ctx, int z) {
        serviceContext.getNotificationService().renderNotifications(ctx, z);
    }
}