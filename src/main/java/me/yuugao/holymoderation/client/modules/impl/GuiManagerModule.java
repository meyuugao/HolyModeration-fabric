package me.yuugao.holymoderation.client.modules.impl;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.DrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.gui.screen.impl.MainGuiScreen;
import me.yuugao.holymoderation.client.modules.DrawableModule;
import me.yuugao.holymoderation.client.util.service.GuiManagerService;
import me.yuugao.holymoderation.client.util.service.InputService;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.eventbus.Subscribe;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.input.MouseScrollEvent;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.render.RenderEvent;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;

import java.util.ArrayList;
import java.util.Collections;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class GuiManagerModule {
    private static final Class<?>[] guiScreens = {
            MainGuiScreen.class,
            ChatScreen.class,
            HandledScreen.class
    };
    private final MinecraftService minecraftService;
    private final InputService inputService;
    private final GuiManagerService guiManagerService;
    private DrawableModule<?> dragging;
    private float dragOffsetX;
    private float dragOffsetY;

    @Subscribe
    public void onRender(RenderEvent event) {
        MinecraftClient mc = minecraftService.getClient();
        Screen currentScreen = mc.currentScreen;
        DrawContext ctx = event.getDrawContext();
        if (currentScreen != null && !shouldGuiRender(currentScreen)) return;
        for (DrawableModule<?> drawableModule : guiManagerService.getDrawableModules()) {
            drawableModule.render(event.getDrawContext(), currentScreen instanceof MainGuiScreen ? RenderMode.CONFIG : RenderMode.LIVE);
        }
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
        for (DrawableModule<?> drawableModule : new ArrayList<>(guiManagerService.getDrawableModules())) {
            drawableModule.getDrawableElement().onMouseScroll(event.getDx(), event.getDy(), event.getX(), event.getY());
        }
    }

    private boolean shouldGuiRender(Screen screen) {
        for (Class<?> clazz : guiScreens) {
            if (clazz.isAssignableFrom(screen.getClass())) return true;
        }
        return false;
    }
}