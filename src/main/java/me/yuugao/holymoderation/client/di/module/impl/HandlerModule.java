package me.yuugao.holymoderation.client.di.module.impl;

import me.yuugao.holymoderation.client.di.DIContainer;
import me.yuugao.holymoderation.client.di.module.DIModule;
import me.yuugao.holymoderation.client.util.handler.GlobalExceptionHandler;

public class HandlerModule implements DIModule {
    @Override
    public void configure(DIContainer container) {
        container.register(GlobalExceptionHandler.class, GlobalExceptionHandler.class);
    }
}