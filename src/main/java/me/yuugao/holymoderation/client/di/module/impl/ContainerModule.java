package me.yuugao.holymoderation.client.di.module.impl;

import me.yuugao.holymoderation.client.di.DIContainer;
import me.yuugao.holymoderation.client.di.module.DIModule;

public class ContainerModule implements DIModule {
    @Override
    public void configure(DIContainer container) {
        container.registerInstance(DIContainer.class, container);
    }
}