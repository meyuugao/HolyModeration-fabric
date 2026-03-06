package me.yuugao.holymoderation.client.gui.modules;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import lombok.Setter;

public abstract class GuiModule {
    protected final ServiceContext serviceContext;
    @Setter
    protected float relX;
    @Setter
    protected float relY;

    public GuiModule(ServiceContext serviceContext, float relX, float relY) {
        this.serviceContext = serviceContext;
        this.relX = relX;
        this.relY = relY;
    }
}