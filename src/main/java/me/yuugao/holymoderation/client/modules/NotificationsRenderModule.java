package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.HudRenderEvent;

public class NotificationsRenderModule extends Module {
    @Subscribe
    public void onHudRender(HudRenderEvent event) {
        serviceContext.getNotificationService().renderNotifications(event.getDrawContext());
    }
}