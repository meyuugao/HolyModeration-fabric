package me.yuugao.holymoderation.client.gui.drawable.element.state.provider;

import me.yuugao.holymoderation.client.gui.drawable.element.state.RenderState;
import me.yuugao.holymoderation.client.gui.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

public abstract class RenderStateProvider<T extends RenderState> {
    protected final ServiceContext serviceContext;

    public RenderStateProvider(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }

    public abstract T getState(RenderMode mode);
}