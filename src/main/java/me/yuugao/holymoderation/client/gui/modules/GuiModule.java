package me.yuugao.holymoderation.client.gui.modules;

import me.yuugao.holymoderation.client.gui.tabs.Tab;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

public abstract class GuiModule {
    protected final Tab parent;
    protected final ServiceContext serviceContext;

    public GuiModule(Tab parent, ServiceContext serviceContext) {
        this.parent = parent;
        this.serviceContext = serviceContext;
    }
}
