package me.yuugao.holymoderation.client.gui.modules;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

public class SingleGuiModule extends GuiModule {
    public SingleGuiModule(ServiceContext serviceContext, float relX, float relY) {
        super(serviceContext, relX, relY);
    }

    public float getX(float windowWidth) {
        return relX * windowWidth;
    }

    public float getY(float windowHeight) {
        return relY * windowHeight;
    }
}