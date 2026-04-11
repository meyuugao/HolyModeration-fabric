package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.LoggerService;

public abstract class Module {
    protected final ServiceContext serviceContext;

    public Module(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }

    public void setLogger(LoggerService holyLogger) {
        serviceContext.setLogger(holyLogger);
    }
}