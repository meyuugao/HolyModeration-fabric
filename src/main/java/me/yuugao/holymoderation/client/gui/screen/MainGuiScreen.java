package me.yuugao.holymoderation.client.gui.screen;

import me.yuugao.holymoderation.client.gui.tabs.main.GeneralTab;
import me.yuugao.holymoderation.client.modules.drawable.DrawableModule;
import me.yuugao.holymoderation.client.modules.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.GuiManagerService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.InputService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.Render2DService;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;

import lombok.Getter;
import obfuscator.DontObf;
import obfuscator.ObfRule;

public class MainGuiScreen extends AnimatedGuiScreen {
    @Getter
    private final int renderPriority = 2000;
    private final Color outlineColor = Color.WHITE;
    private DrawableModule<?> dragging;
    private float dragOffsetX;
    private float dragOffsetY;

    public MainGuiScreen(ServiceContext serviceContext) {
        super(Text.of("HolyModeration Main Gui Screen"), serviceContext);
        this.tabs.put("General", new GeneralTab(this, serviceContext));
    }

    @Override
    @DontObf(ObfRule.MAP_METHOD)
    public void render(DrawContext drawContext, int mouseX, int mouseY, float tickDelta) {
        super.render(drawContext, mouseX, mouseY, tickDelta);

        Render2DService render2DService = serviceContext.getRender2DService();
        InputService inputService = serviceContext.getInputService();
        GuiManagerService guiManagerService = serviceContext.getGuiManagerService();

        float targetW = (float) drawContext.getScaledWindowWidth() / 2.2f;
        float targetH = (float) drawContext.getScaledWindowHeight() / 1.8f;

        this.width = targetW * getAnimValue();
        this.height = targetH * getAnimValue();

        this.x = (float) drawContext.getScaledWindowWidth() / 2 - this.width / 2f;
        this.y = (float) drawContext.getScaledWindowHeight() / 2 - this.height / 2f;

        float baseOutline = 1f;
        float scaleFactor = Math.min(this.width, this.height) / 100f;
        float scaledOutline = baseOutline * scaleFactor;

        render2DService.setupRender();

        render2DService.renderSoftRoundedRectOutline(
                drawContext.getMatrices(),
                this.x, this.y, Math.max(1, this.width), Math.max(1, this.height),
                renderPriority,
                10f,
                new Color(0xB3002AFF, true),
                outlineColor,
                scaledOutline, 3
        );

        render2DService.endRender();

        if (inputService.isMouseButtonHeld(0)) {
            if (dragging == null && inputService.wasMouseButtonPressed(0)) {
                ArrayList<DrawableModule<?>> list = new ArrayList<>(guiManagerService.getDrawableModules());
                Collections.reverse(list);
                for (DrawableModule<?> d : list) {
                    if (d.isMouseOver(mouseX, mouseY, drawContext)) {
                        dragging = d;
                        dragOffsetX = mouseX - d.getDrawableElement().getX(drawContext);
                        dragOffsetY = mouseY - d.getDrawableElement().getY(drawContext);
                        break;
                    }
                }
            }

            if (dragging != null) {
                dragging.getDrawableElement().setX(mouseX - dragOffsetX, drawContext);
                dragging.getDrawableElement().setY(mouseY - dragOffsetY, drawContext);
            }
        } else {
            dragging = null;
        }

        renderTabs(drawContext, mouseX, mouseY, tickDelta);
    }
}