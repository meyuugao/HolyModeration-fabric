package me.yuugao.holymoderation.client.modules.maingui;

import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.HudRenderEvent;
import me.yuugao.holymoderation.client.modules.Module;

public class MainGuiModule extends Module {
    private final MainGuiScreen mainGuiScreen = new MainGuiScreen();

    @Subscribe
    public void onHudRender(HudRenderEvent event) {
        if (keyBindingService.wasKeyPressed("open_main_gui")) {
            minecraftService.getClient().setScreen(mainGuiScreen);
        }
    }
}