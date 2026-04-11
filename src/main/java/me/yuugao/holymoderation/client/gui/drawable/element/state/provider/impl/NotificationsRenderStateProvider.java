package me.yuugao.holymoderation.client.gui.drawable.element.state.provider.impl;

import me.yuugao.holymoderation.client.gui.drawable.element.state.impl.NotificationsRenderState;
import me.yuugao.holymoderation.client.gui.drawable.element.state.provider.RenderStateProvider;
import me.yuugao.holymoderation.client.gui.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

public class NotificationsRenderStateProvider extends RenderStateProvider<NotificationsRenderState> {
    public NotificationsRenderStateProvider(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Override
    public NotificationsRenderState getState(RenderMode mode) {
        return new NotificationsRenderState();
    }
}