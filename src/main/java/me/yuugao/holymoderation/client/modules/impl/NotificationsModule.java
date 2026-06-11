package me.yuugao.holymoderation.client.modules.impl;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.singleton.NotificationsDrawableElement;
import me.yuugao.holymoderation.client.modules.DrawableModule;
import me.yuugao.holymoderation.client.util.service.GuiManagerService;

@Singleton
public class NotificationsModule extends DrawableModule<NotificationsDrawableElement> {
    @Inject
    public NotificationsModule(NotificationsDrawableElement drawableElement, GuiManagerService guiManagerService) {
        super(drawableElement);
    }

    @Override
    public int getRenderPriority() {
        return 1004;
    }
}