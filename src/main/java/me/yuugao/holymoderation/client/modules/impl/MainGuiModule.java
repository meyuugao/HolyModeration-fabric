package me.yuugao.holymoderation.client.modules.impl;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.screen.impl.MainGuiScreen;
import me.yuugao.holymoderation.client.util.service.InputService;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.eventbus.Subscribe;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.render.RenderEvent;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class MainGuiModule {
    private final InputService inputService;
    private final MinecraftService minecraftService;
    private final MainGuiScreen mainGuiScreen;

    @Subscribe
    public void onHudRender(RenderEvent event) {
        MinecraftClient mc = minecraftService.getClient();
        if (inputService.wasKeyBindPressed("open_main_gui")) {
            Screen currentScreen = mc.currentScreen;
            if (currentScreen == null) {
                mc.execute(() -> mc.setScreen(mainGuiScreen));
            } else if (currentScreen == mainGuiScreen) {
                mainGuiScreen.close();
            }
        }
    }
}