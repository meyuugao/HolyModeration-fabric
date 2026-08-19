package me.yuugao.holymoderation.client.modules.impl;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.DrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.ScreenCtx;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.StatefulDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.singleton.NotificationsDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.gui.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.gui.screen.impl.MainGuiScreen;
import me.yuugao.holymoderation.client.modules.DrawableModule;
import me.yuugao.holymoderation.client.util.service.Render2DService;
import me.yuugao.holymoderation.client.util.service.ThemePalette;
import me.yuugao.holymoderation.client.util.service.ThemeService;
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
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.text.Text;

import org.lwjgl.glfw.GLFW;

import java.awt.Color;
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
    private static final float DRAG_MARGIN = 4f;
    private final MinecraftService minecraftService;
    private final InputService inputService;
    private final GuiManagerService guiManagerService;
    private final ConfigManagerService configManagerService;
    private final Render2DService render2DService;
    private final ThemeService themeService;
    private DrawableModule<?> dragging;
    private float dragOffsetX;
    private float dragOffsetY;
    private final java.util.Map<String, float[]> badgePositions = new java.util.HashMap<>();

    @Subscribe
    public void onRender(RenderEvent event) {
        MinecraftClient mc = minecraftService.getClient();
        Screen currentScreen = mc.currentScreen;
        DrawContext ctx = event.getDrawContext();
        if (currentScreen != null && !shouldGuiRender(currentScreen)) return;
        for (DrawableModule<?> drawableModule : guiManagerService.getDrawableModules()) {
            DrawableElement elem = drawableModule.getDrawableElement();
            drawableModule.render(event.getDrawContext(), currentScreen instanceof MainGuiScreen ? RenderMode.CONFIG : RenderMode.LIVE);
            if (currentScreen instanceof MainGuiScreen && elem instanceof StatefulDrawableElement<?> stateful) {
                renderScaleBadge(event.getDrawContext(), stateful);
            }
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
            if (dragging != null && !(dragging.getDrawableElement() instanceof NotificationsDrawableElement)) {
                DrawableElement elem = dragging.getDrawableElement();
                float sw = ctx.getScaledWindowWidth();
                float sh = ctx.getScaledWindowHeight();
                float targetAnchorX = event.getMouseX() - dragOffsetX;
                float targetAnchorY = event.getMouseY() - dragOffsetY;
                targetAnchorX = clampAnchor(targetAnchorX, sw, elem.getScaledWidth(), elem.getPivotMode().getXFactor());
                targetAnchorY = clampAnchor(targetAnchorY, sh, elem.getScaledHeight(), elem.getPivotMode().getYFactor());

                if (isShiftHeld()) {
                    targetAnchorX = snapToCenter(targetAnchorX, sw);
                    targetAnchorY = snapToCenter(targetAnchorY, sh);
                }

                elem.setRelativePos(targetAnchorX / sw, targetAnchorY / sh);
            }
        } else {
            if (dragging != null && dragging.getDrawableElement() instanceof NotificationsDrawableElement notif) {
                snapNotificationToCorner(notif, event.getMouseX(), event.getMouseY(),
                        ctx.getScaledWindowWidth(), ctx.getScaledWindowHeight());
            }
            dragging = null;
        }
        if (dragging != null && dragging.getDrawableElement() instanceof NotificationsDrawableElement) {
            renderNotificationAnchors(ctx, event.getMouseX(), event.getMouseY());
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

    private boolean isShiftHeld() {
        return inputService.isKeyHeld(GLFW.GLFW_KEY_LEFT_SHIFT) || inputService.isKeyHeld(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    private float snapToCenter(float anchor, float size) {
        float center = size / 2f;
        return Math.abs(anchor - center) <= 20f ? center : anchor;
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

    private float clampAnchor(float anchor, float screenSize, float scaledSize, float pivotFactor) {
        float pivotOffset = scaledSize * pivotFactor;
        float minAnchor = DRAG_MARGIN + pivotOffset;
        float maxAnchor = screenSize - DRAG_MARGIN - scaledSize + pivotOffset;
        if (maxAnchor < minAnchor) {
            return screenSize / 2f;
        }
        return Math.max(minAnchor, Math.min(maxAnchor, anchor));
    }

    private void snapNotificationToCorner(NotificationsDrawableElement notif, double mouseX, double mouseY, float screenW, float screenH) {
        PivotMode corner = nearestCorner(mouseX, mouseY, screenW, screenH);
        notif.setPivotMode(corner);
        notif.setRelativePos(corner.getXFactor(), corner.getYFactor());

        GuiConfig guiConfig = configManagerService.getGuiConfig();
        guiConfig.setPivotMode(notif.getHudElementId(), corner);
        configManagerService.saveConfig(guiConfig);
    }

    private PivotMode nearestCorner(double mouseX, double mouseY, float screenW, float screenH) {
        double dLU = mouseX * mouseX + mouseY * mouseY;
        double dRU = (screenW - mouseX) * (screenW - mouseX) + mouseY * mouseY;
        double dLD = mouseX * mouseX + (screenH - mouseY) * (screenH - mouseY);
        double dRD = (screenW - mouseX) * (screenW - mouseX) + (screenH - mouseY) * (screenH - mouseY);
        double min = Math.min(Math.min(dLU, dRU), Math.min(dLD, dRD));
        if (min == dLU) return PivotMode.LEFT_UP;
        if (min == dRU) return PivotMode.RIGHT_UP;
        if (min == dLD) return PivotMode.LEFT_DOWN;
        return PivotMode.RIGHT_DOWN;
    }

    private void renderNotificationAnchors(DrawContext ctx, double mouseX, double mouseY) {
        float sw = ctx.getScaledWindowWidth();
        float sh = ctx.getScaledWindowHeight();
        float size = 22f;
        float margin = 8f;
        PivotMode nearest = nearestCorner(mouseX, mouseY, sw, sh);
        ThemePalette palette = themeService.getPalette();

        render2DService.setupRender();
        drawAnchor(ctx, margin, margin, size, nearest == PivotMode.LEFT_UP, palette);
        drawAnchor(ctx, sw - margin - size, margin, size, nearest == PivotMode.RIGHT_UP, palette);
        drawAnchor(ctx, margin, sh - margin - size, size, nearest == PivotMode.LEFT_DOWN, palette);
        drawAnchor(ctx, sw - margin - size, sh - margin - size, size, nearest == PivotMode.RIGHT_DOWN, palette);
        render2DService.endRender();
    }

    private void drawAnchor(DrawContext ctx, float x, float y, float size, boolean active, ThemePalette palette) {
        Color fill = active ? palette.primary : palette.surface;
        Color outline = active ? palette.primaryBright : palette.outline;
        render2DService.renderSoftRoundedRectOutline(ctx, x, y, size, size, 3001,
                size / 3f, fill, outline, active ? 2f : 1f, 2f);
    }

    private void renderScaleBadge(DrawContext ctx, StatefulDrawableElement<?> elem) {
        float scaledW = elem.getScaledWidth();
        float scaledH = elem.getScaledHeight();
        if (scaledW <= 0f || scaledH <= 0f) return;

        String id = elem.getHudElementId();
        float scale = configManagerService.getGuiConfig().getHudScale(id);
        String text = "%.2f\u00d7".formatted(scale);

        TextRenderer tr = minecraftService.getClient().textRenderer;
        ThemePalette palette = themeService.getPalette();

        float sw = ctx.getScaledWindowWidth();
        float sh = ctx.getScaledWindowHeight();
        float w = tr.getWidth(text) + 12f;
        float h = tr.fontHeight + 5f;

        float centerX = elem.getAbsoluteX(sw) + scaledW / 2f;
        float targetX = centerX - w / 2f;
        float gap = 7f;
        float targetY;
        if (elem.getPivotMode().getYFactor() >= 0.5f) {
            targetY = elem.getAbsoluteY(sh) + scaledH + gap;
        } else {
            targetY = elem.getAbsoluteY(sh) - h - gap;
        }

        targetX = Math.max(4f, Math.min(targetX, sw - w - 4f));
        targetY = Math.max(4f, Math.min(targetY, sh - h - 4f));

        final float fx = targetX;
        final float fy = targetY;
        float[] prev = badgePositions.computeIfAbsent(id, k -> new float[]{fx, fy});
        prev[0] += (targetX - prev[0]) * 0.25f;
        prev[1] += (targetY - prev[1]) * 0.25f;
        float x = prev[0];
        float y = prev[1];

        render2DService.setupRender();
        render2DService.renderSoftRoundedRectOutline(ctx, x, y, w, h, 3000,
                h / 2f, palette.surface, palette.outline, 1f, 2f);
        render2DService.renderText(tr, Text.literal(text).asOrderedText(),
                (int) (x + 6f), (int) (y + (h - tr.fontHeight) / 2f + 1f), 3000,
                palette.textMuted.getRGB(), false, ctx);
        render2DService.endRender();
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