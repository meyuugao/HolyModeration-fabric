package me.yuugao.holymoderation.client.modules.impl;

import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.impl.input.MouseScrollEvent;
import me.yuugao.holymoderation.client.eventbus.event.impl.render.RenderEvent;
import me.yuugao.holymoderation.client.gui.screen.impl.MainGuiScreen;
import me.yuugao.holymoderation.client.modules.Module;
import me.yuugao.holymoderation.client.modules.DrawableModule;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.DrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.GuiManagerService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.InputService;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

import java.util.ArrayList;
import java.util.Collections;

public class GuiManagerModule extends Module {
    private static final Class<?>[] guiScreens = {
            MainGuiScreen.class,
            ChatScreen.class,
            HandledScreen.class
    };
    private DrawableModule<?> dragging;
    private float dragOffsetX;
    private float dragOffsetY;

    public GuiManagerModule(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Subscribe
    public void onRender(RenderEvent event) {
        MinecraftClient mc = serviceContext.getMinecraftService().getClient();
        InputService inputService = serviceContext.getInputService();
        GuiManagerService guiManagerService = serviceContext.getGuiManagerService();

        Screen currentScreen = mc.currentScreen;
        DrawContext ctx = event.getDrawContext();

        if (currentScreen != null && !shouldGuiRender(currentScreen)) return;

        guiManagerService.getDrawableModules().forEach((drawableModule) ->
                drawableModule.render(event.getDrawContext(), currentScreen instanceof MainGuiScreen
                        ? RenderMode.CONFIG : RenderMode.LIVE));

        if (inputService.isMouseButtonHeld(0)) {
            if (dragging == null && inputService.wasMouseButtonPressed(0)) {
                ArrayList<DrawableModule<?>> list = new ArrayList<>(guiManagerService.getDrawableModules());
                Collections.reverse(list);
                for (DrawableModule<?> d : list) {
                    DrawableElement elem = d.getDrawableElement();

                    if (elem.isDraggable()) {
                        float sw = ctx.getScaledWindowWidth();
                        float sh = ctx.getScaledWindowHeight();
                        float anchorX = elem.getAnchorX(sw);
                        float anchorY = elem.getAnchorY(sh);

                        if (elem.isGlobalMouseOver(ctx, event.getMouseX(), event.getMouseY())) {
                            dragging = d;
                            dragOffsetX = event.getMouseX() - anchorX;
                            dragOffsetY = event.getMouseY() - anchorY;
                            break;
                        }
                    }
                }
            }

            if (dragging != null) {
                DrawableElement elem = dragging.getDrawableElement();
                float sw = ctx.getScaledWindowWidth();
                float sh = ctx.getScaledWindowHeight();

                float targetAnchorX = event.getMouseX() - dragOffsetX;
                float targetAnchorY = event.getMouseY() - dragOffsetY;

                float newRelX = targetAnchorX / sw;
                float newRelY = targetAnchorY / sh;

                elem.setRelativePos(newRelX, newRelY);
            }
        } else {
            dragging = null;
        }
    }

    @Subscribe
    public void onMouseScroll(MouseScrollEvent event) {
        GuiManagerService guiManagerService = serviceContext.getGuiManagerService();

        for (DrawableModule<?> drawableModule : new ArrayList<>(guiManagerService.getDrawableModules())) {
            drawableModule.getDrawableElement().onMouseScroll(event.getDx(), event.getDy(), event.getX(), event.getY());
        }
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