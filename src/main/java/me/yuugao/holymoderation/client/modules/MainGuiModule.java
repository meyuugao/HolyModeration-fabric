package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.HudRenderEvent;
import me.yuugao.holymoderation.client.gui.screen.MainGuiScreen;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import net.minecraft.client.gui.screen.Screen;

public class MainGuiModule extends Module {
    private final MainGuiScreen mainGuiScreen = new MainGuiScreen(serviceContext);

    public MainGuiModule(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Subscribe
    public void onHudRender(HudRenderEvent event) {
        if (serviceContext.getInputService().wasKeyPressed("open_main_gui")) {
            Screen currentScreen = serviceContext.getMinecraftService().getClient().currentScreen;
            if (currentScreen == null) {
                serviceContext.getMinecraftService().getClient().setScreen(mainGuiScreen);
            } else if (currentScreen.equals(mainGuiScreen)) {
                mainGuiScreen.close();
            }
        }
    }
}