package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.RenderEvent;
import me.yuugao.holymoderation.client.gui.screen.MainGuiScreen;
import me.yuugao.holymoderation.client.modules.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.GuiManagerService;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

public class GuiManagerModule extends Module {
    private static final Class<?>[] guiScreens = {
            MainGuiScreen.class,
            ChatScreen.class,
            HandledScreen.class
    };

    public GuiManagerModule(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Subscribe
    public void onRender(RenderEvent event) {
        MinecraftClient mc = serviceContext.getMinecraftService().getClient();
        GuiManagerService guiManagerService = serviceContext.getGuiManagerService();

        Screen currentScreen = mc.currentScreen;

        if (currentScreen != null && !shouldGuiRender(currentScreen)) return;

        guiManagerService.getDrawableModules().forEach((drawableModule) ->
                drawableModule.render(event.getDrawContext(), currentScreen instanceof MainGuiScreen
                        ? RenderMode.CONFIG : RenderMode.LIVE));
    }

    private boolean shouldGuiRender(Screen screen) {
        for (Class<?> clazz : guiScreens) {
            if (clazz.isAssignableFrom(screen.getClass())) {
                return true;
            }
        }
        return false;
    }
}