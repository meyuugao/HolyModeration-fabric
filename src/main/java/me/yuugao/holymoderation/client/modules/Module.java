package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.LoggerService;

public abstract class Module {
    protected final ServiceContext serviceContext;

    public Module() {
        serviceContext = new ServiceContext();
    }

    public void setLogger(LoggerService holyLogger) {
        serviceContext.setLogger(holyLogger);
    }
}