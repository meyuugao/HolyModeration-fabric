package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.modules.drawable.DrawableModule;
import me.yuugao.holymoderation.client.modules.drawable.element.ReportsParserDrawableElement;
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