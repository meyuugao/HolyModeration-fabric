package me.yuugao.holymoderation.client.gui.modules;

import me.yuugao.holymoderation.client.gui.tabs.Tab;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

public abstract class GuiModule {
    protected final ServiceContext serviceContext;

    public GuiModule(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }
}
