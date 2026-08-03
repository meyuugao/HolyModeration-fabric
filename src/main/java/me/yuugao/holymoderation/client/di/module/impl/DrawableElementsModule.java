package me.yuugao.holymoderation.client.di.module.impl;

import me.yuugao.holymoderation.client.di.DIContainer;
import me.yuugao.holymoderation.client.di.module.DIModule;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.ColorPickerDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.singleton.*;

public class DrawableElementsModule implements DIModule {
    @Override
    public void configure(DIContainer container) {
        container.register(CheckoutsDrawableElement.class, CheckoutsDrawableElement.class);
        container.register(ColorPickerDrawableElement.class, ColorPickerDrawableElement.class);
        container.register(NotificationsDrawableElement.class, NotificationsDrawableElement.class);
        container.register(ReportsParserDrawableElement.class, ReportsParserDrawableElement.class);
        container.register(SpyDrawableElement.class, SpyDrawableElement.class);
        container.register(WatermarkDrawableElement.class, WatermarkDrawableElement.class);
    }
}