package me.yuugao.holymoderation.client.modules.impl;

import me.yuugao.holymoderation.client.modules.drawable.DrawableModule;
import me.yuugao.holymoderation.client.modules.drawable.element.impl.NotificationsDrawableElement;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

public class NotificationsModule extends DrawableModule<NotificationsDrawableElement> {
    public NotificationsModule(ServiceContext serviceContext, NotificationsDrawableElement drawableElement) {
        super(serviceContext, drawableElement);
    }

    @Override
    public int getRenderPriority() {
        return 1004;
    }
}