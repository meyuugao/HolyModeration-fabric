package me.yuugao.holymoderation.client.gui.drawable.element.state.provider.impl;

import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.drawable.element.state.impl.NotificationsRenderState;
import me.yuugao.holymoderation.client.gui.drawable.element.state.provider.RenderStateProvider;
import me.yuugao.holymoderation.client.gui.drawable.render.RenderMode;

@Singleton
public class NotificationsRenderStateProvider extends RenderStateProvider<NotificationsRenderState> {
    @Override
    public NotificationsRenderState getState(RenderMode mode) {
        return new NotificationsRenderState();
    }
}