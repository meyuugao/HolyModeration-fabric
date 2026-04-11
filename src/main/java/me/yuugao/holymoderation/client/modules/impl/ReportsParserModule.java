package me.yuugao.holymoderation.client.modules.impl;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.ReportsParserDrawableElement;
import me.yuugao.holymoderation.client.modules.DrawableModule;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

public class ReportsParserModule extends DrawableModule<ReportsParserDrawableElement> {
    public ReportsParserModule(ServiceContext serviceContext, ReportsParserDrawableElement drawableElement) {
        super(serviceContext, drawableElement);
    }

    @Override
    public int getRenderPriority() {
        return 1003;
    }
}