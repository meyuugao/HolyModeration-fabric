package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.HudRenderEvent;
import me.yuugao.holymoderation.client.gui.screen.MainGuiScreen;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.GuiManagerService;

import net.minecraft.client.MinecraftClient;

public class GuiManagerModule extends Module {
    public GuiManagerModule(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Subscribe
    public void onHudRender(HudRenderEvent event) {
        MinecraftClient mc = serviceContext.getMinecraftService().getClient();
        GuiManagerService guiManagerService = serviceContext.getGuiManagerService();

        if (!(mc.currentScreen instanceof MainGuiScreen)) {
            guiManagerService.getDrawableModules().forEach((drawableModule) ->
                    drawableModule.render(event.getDrawContext(), false));
        }
    }
}