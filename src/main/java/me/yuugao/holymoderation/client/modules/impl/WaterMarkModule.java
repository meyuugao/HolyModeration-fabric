package me.yuugao.holymoderation.client.modules.impl;

import me.yuugao.holymoderation.client.modules.drawable.DrawableModule;
import me.yuugao.holymoderation.client.modules.drawable.element.impl.WatermarkDrawableElement;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import lombok.Getter;

@Getter
public class WaterMarkModule extends DrawableModule<WatermarkDrawableElement> {
    public WaterMarkModule(ServiceContext serviceContext, WatermarkDrawableElement watermarkDrawableElement) {
        super(serviceContext, watermarkDrawableElement);
    }

    @Override
    public int getRenderPriority() {
        return 1000;
    }
}