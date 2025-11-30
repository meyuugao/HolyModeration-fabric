package me.yuugao.holymoderation.client.modules.maingui;

import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.HudRenderEvent;
import me.yuugao.holymoderation.client.modules.Module;

import net.minecraft.client.gui.screen.Screen;

public class MainGuiModule extends Module {
    private final MainGuiScreen mainGuiScreen = new MainGuiScreen();

    @Subscribe
    public void onHudRender(HudRenderEvent event) {
        if (keyBindingService.wasKeyPressed("open_main_gui")) {
            Screen currentScreen = minecraftService.getClient().currentScreen;
            if (currentScreen == null) {
                minecraftService.getClient().setScreen(mainGuiScreen);
            } else if (currentScreen.equals(mainGuiScreen)) {
                mainGuiScreen.close();
            }
        }
    }
}