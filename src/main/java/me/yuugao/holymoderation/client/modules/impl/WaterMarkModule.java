package me.yuugao.holymoderation.client.modules.impl;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.singleton.WatermarkDrawableElement;
import me.yuugao.holymoderation.client.modules.DrawableModule;

import lombok.Getter;

@Getter
@Singleton
public class WaterMarkModule extends DrawableModule<WatermarkDrawableElement> {
    @Inject
    public WaterMarkModule(WatermarkDrawableElement watermarkDrawableElement) {
        super(watermarkDrawableElement);
    }

    @Override
    public int getRenderPriority() {
        return 1000;
    }
}