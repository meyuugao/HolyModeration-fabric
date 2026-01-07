package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.HudRenderEvent;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

public class GuiManagerModule extends Module {
    public GuiManagerModule(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Subscribe
    public void onHudRender(HudRenderEvent event) {
        serviceContext.getGuiManagerService().getDrawableModules().forEach((drawableModule) -> drawableModule.render(event));
    }
}