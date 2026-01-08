package me.yuugao.holymoderation.client.gui.tabs;

import me.yuugao.holymoderation.client.gui.modules.GuiModule;
import me.yuugao.holymoderation.client.gui.screen.GuiScreen;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import net.minecraft.client.gui.DrawContext;

import java.util.HashMap;

public abstract class Tab<T extends GuiScreen> {
    protected final T parent;
    protected final ServiceContext serviceContext;
    protected final HashMap<String, GuiModule> modules = new HashMap<>();

    public Tab(T parent, ServiceContext serviceContext) {
        this.parent = parent;
        this.serviceContext = serviceContext;
    }

    public abstract void onRender(DrawContext context, int mouseX, int mouseY, float tickDelta);
}