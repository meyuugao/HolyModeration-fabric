package me.yuugao.holymoderation.client.modules.impl;

import me.yuugao.holymoderation.client.modules.DrawableModule;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.NotificationsDrawableElement;
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