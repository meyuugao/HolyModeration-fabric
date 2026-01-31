package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.HudRenderEvent;
import me.yuugao.holymoderation.client.gui.screen.MainGuiScreen;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.InputService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.MinecraftService;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

public class MainGuiModule extends Module {
    private final MainGuiScreen mainGuiScreen = new MainGuiScreen(serviceContext);

    public MainGuiModule(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Subscribe
    public void onHudRender(HudRenderEvent event) {
        InputService inputService = serviceContext.getInputService();
        MinecraftService minecraftService = serviceContext.getMinecraftService();

        MinecraftClient mc = minecraftService.getClient();

        if (inputService.wasKeyBindPressed("open_main_gui")) {
            Screen currentScreen = mc.currentScreen;
            if (currentScreen == null) {
                mc.setScreen(mainGuiScreen);
            } else if (currentScreen.equals(mainGuiScreen)) {
                mainGuiScreen.close();
            }
        }
    }
}