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
import me.yuugao.holymoderation.client.util.service.NotificationsService;
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
    private final NotificationsService notificationsService;
    private DrawableModule<?> dragging;
    private float dragOffsetX;
    private float dragOffsetY;

    private static final float SCALE_STEP = 0.1f;
    private static final float MIN_SCALE = 0.3f;
    private static final float MAX_SCALE = 3.0f;

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

                targetAnchorX = clampAnchor(targetAnchorX, sw,
                        elem.getScaledWidth(), elem.getPivotMode().getXFactor());
                targetAnchorY = clampAnchor(targetAnchorY, sh,
                        elem.getScaledHeight(), elem.getPivotMode().getYFactor());

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
        if (dragging != null) {
            DrawableElement elem = dragging.getDrawableElement();
            float delta = (float) event.getDy() * SCALE_STEP;
            float newScale = elem.getUserScale() + delta;
            newScale = Math.max(MIN_SCALE, Math.min(newScale, MAX_SCALE));
            elem.setUserScale(newScale);
            return;
        }
        if (notificationsService.isMouseOver(event.getX(), event.getY())) {
            notificationsService.adjustNotifScale((float) event.getDy() * SCALE_STEP);
            return;
        }
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

    private static float clampAnchor(float anchor, float screenSize, float elemSize, float pivotFactor) {
        float minAnchor = pivotFactor * elemSize;
        float maxAnchor = screenSize - (1f - pivotFactor) * elemSize;
        if (maxAnchor < minAnchor) {
            // Элемент больше экрана — центрируем по доступной оси
            return screenSize * 0.5f;
        }
        return Math.max(minAnchor, Math.min(anchor, maxAnchor));
    }
}