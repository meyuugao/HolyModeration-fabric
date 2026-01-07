package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.HudRenderEvent;
import me.yuugao.holymoderation.client.modules.drawable.DrawableModule;
import me.yuugao.holymoderation.client.modules.drawable.element.WatermarkDrawableElement;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import lombok.Getter;

@Getter
public class WaterMarkModule extends DrawableModule<WatermarkDrawableElement> {
    private final int renderPriority = 98;

    public WaterMarkModule(ServiceContext serviceContext, WatermarkDrawableElement watermarkDrawableElement) {
        super(serviceContext, watermarkDrawableElement);
    }
}