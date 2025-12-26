package me.yuugao.holymoderation.client.modules.gui.tabs;

import me.yuugao.holymoderation.client.modules.gui.modules.GuiModule;
import me.yuugao.holymoderation.client.modules.gui.screen.GuiScreen;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import net.minecraft.client.gui.DrawContext;

import java.util.HashMap;

public abstract class Tab {
    protected final GuiScreen parent;
    protected final ServiceContext serviceContext;
    protected final HashMap<String, GuiModule> modules = new HashMap<>();

    public Tab(GuiScreen parent, ServiceContext serviceContext) {
        this.parent = parent;
        this.serviceContext = serviceContext;
    }

    public abstract void onRender(DrawContext context, int mouseX, int mouseY, float tickDelta);
}