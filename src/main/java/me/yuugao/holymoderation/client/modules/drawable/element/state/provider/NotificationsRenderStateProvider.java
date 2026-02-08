package me.yuugao.holymoderation.client.modules.drawable.element.state.provider;

import me.yuugao.holymoderation.client.modules.drawable.element.state.NotificationsRenderState;
import me.yuugao.holymoderation.client.modules.drawable.render.RenderMode;
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