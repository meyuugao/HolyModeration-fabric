package me.yuugao.holymoderation.client.modules.impl;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.DrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.ScreenCtx;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.StatefulDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.gui.screen.impl.MainGuiScreen;
import me.yuugao.holymoderation.client.modules.DrawableModule;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.GuiManagerService;
import me.yuugao.holymoderation.client.util.service.InputService;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.config.impl.GuiConfig;
import me.yuugao.holymoderation.client.util.service.eventbus.Subscribe;
import me.yuugao.holymoderation.client.util.service.eventbus.event.impl.input.MouseClickEvent;
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
    private static final float MIN_HUD_SCALE = 0.5f;
    private static final float MAX_HUD_SCALE = 2.0f;
    private static final float HUD_SCALE_STEP = 0.1f;
    private final MinecraftService minecraftService;
    private final InputService inputService;
    private final GuiManagerService guiManagerService;
    private final ConfigManagerService configManagerService;
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
        MinecraftClient mc = minecraftService.getClient();
        if (mc.currentScreen instanceof MainGuiScreen) {
            scaleHoveredElement(event.getDy(), event.getX(), event.getY());
            return;
        }
        for (DrawableModule<?> drawableModule : new ArrayList<>(guiManagerService.getDrawableModules())) {
            drawableModule.getDrawableElement().onMouseScroll(event.getDx(), event.getDy(), event.getX(), event.getY());
        }
    }

    @Subscribe
    public void onMouseClick(MouseClickEvent event) {

        MinecraftClient mc = minecraftService.getClient();
        if (!(mc.currentScreen instanceof MainGuiScreen)) return;

        if (event.getButton() == 1) {
            resetHoveredElementScale(event.getX(), event.getY());
            return;
        }

        if (dragging != null) return;

        ScreenCtx screen = new ScreenCtx(
                mc.getWindow().getScaledWidth(),
                mc.getWindow().getScaledHeight(),
                event.getX(),
                event.getY()
        );

        ArrayList<DrawableModule<?>> list = new ArrayList<>(guiManagerService.getDrawableModules());
        Collections.reverse(list);
        for (DrawableModule<?> d : list) {
            if (d.getDrawableElement().handleClick(screen)) return;
        }
    }

    private void scaleHoveredElement(double dy, int mouseX, int mouseY) {
        MinecraftClient mc = minecraftService.getClient();
        float windowW = mc.getWindow().getScaledWidth();
        float windowH = mc.getWindow().getScaledHeight();

        DrawableModule<?> hovered = findHoveredModule(windowW, windowH, mouseX, mouseY);
        if (hovered == null) return;

        StatefulDrawableElement<?> element = (StatefulDrawableElement<?>) hovered.getDrawableElement();
        String id = element.getHudElementId();
        GuiConfig guiConfig = configManagerService.getGuiConfig();

        float step = dy > 0 ? HUD_SCALE_STEP : -HUD_SCALE_STEP;

        float current = guiConfig.getHudScale(id);
        float next = Math.max(MIN_HUD_SCALE, Math.min(MAX_HUD_SCALE, current + step));
        if (Float.compare(next, current) != 0) {
            guiConfig.setHudScale(id, next);
            configManagerService.saveConfig(guiConfig);
        }
    }

    private void resetHoveredElementScale(int mouseX, int mouseY) {
        MinecraftClient mc = minecraftService.getClient();
        float windowW = mc.getWindow().getScaledWidth();
        float windowH = mc.getWindow().getScaledHeight();

        DrawableModule<?> hovered = findHoveredModule(windowW, windowH, mouseX, mouseY);
        if (hovered == null) return;

        StatefulDrawableElement<?> element = (StatefulDrawableElement<?>) hovered.getDrawableElement();
        String id = element.getHudElementId();
        GuiConfig guiConfig = configManagerService.getGuiConfig();

        if (Float.compare(guiConfig.getHudScale(id), 1f) != 0) {
            guiConfig.setHudScale(id, 1f);
            configManagerService.saveConfig(guiConfig);
        }
    }

    private DrawableModule<?> findHoveredModule(float windowW, float windowH, int mouseX, int mouseY) {
        ArrayList<DrawableModule<?>> list = new ArrayList<>(guiManagerService.getDrawableModules());
        Collections.reverse(list);
        for (DrawableModule<?> d : list) {
            DrawableElement elem = d.getDrawableElement();
            if (elem instanceof StatefulDrawableElement<?> && elem.isMouseOver(windowW, windowH, mouseX, mouseY)) {
                return d;
            }
        }
        return null;
    }

    private boolean shouldGuiRender(Screen screen) {
        for (Class<?> clazz : guiScreens) {
            if (clazz.isAssignableFrom(screen.getClass())) return true;
        }
        return false;
    }
}