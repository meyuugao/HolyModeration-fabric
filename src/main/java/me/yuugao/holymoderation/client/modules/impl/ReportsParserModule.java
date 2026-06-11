package me.yuugao.holymoderation.client.modules.impl;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.singleton.ReportsParserDrawableElement;
import me.yuugao.holymoderation.client.modules.DrawableModule;

@Singleton
public class ReportsParserModule extends DrawableModule<ReportsParserDrawableElement> {
    @Inject
    public ReportsParserModule(ReportsParserDrawableElement drawableElement) {
        super(drawableElement);
    }

    @Override
    public int getRenderPriority() {
        return 1003;
    }
}