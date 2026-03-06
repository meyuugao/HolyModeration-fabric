package me.yuugao.holymoderation.client.gui.modules;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

public class ChildGuiModule extends GuiModule {
    public ChildGuiModule(ServiceContext serviceContext, float relX, float relY) {
        super(serviceContext, relX, relY);
    }

    public float getX(float parentX, float parentWidth) {
        return parentX + relX * parentWidth;
    }

    public float getY(float parentY, float parentHeight) {
        return parentY + relY * parentHeight;
    }
}