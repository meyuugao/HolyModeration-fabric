package me.yuugao.holymoderation.client.di.module.impl;

import me.yuugao.holymoderation.client.di.DIContainer;
import me.yuugao.holymoderation.client.di.module.DIModule;
import me.yuugao.holymoderation.client.util.factory.DrawableElementFactory;

public class FactoriesModule implements DIModule {
    @Override
    public void configure(DIContainer container) {
        container.register(DrawableElementFactory.class, DrawableElementFactory.class);
    }
}