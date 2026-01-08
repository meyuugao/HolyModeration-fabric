package me.yuugao.holymoderation.client.gui.screen;

import me.yuugao.holymoderation.client.gui.tabs.main.GeneralTab;
import me.yuugao.holymoderation.client.modules.drawable.DrawableModule;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.awt.Color;

import lombok.Getter;
import obfuscator.DontObf;
import obfuscator.ObfRule;

public class MainGuiScreen extends AnimatedGuiScreen {
    @Getter
    private final int renderPriority = 200;
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
    public void render(DrawContext context, int mouseX, int mouseY, float tickDelta) {
        float targetW = (float) context.getScaledWindowWidth() / 2.2f;
        float targetH = (float) context.getScaledWindowHeight() / 1.8f;

        this.width = targetW * getAnimValue();
        this.height = targetH * getAnimValue();

        this.x = (float) context.getScaledWindowWidth() / 2;
        this.y = (float) context.getScaledWindowHeight() / 2;

        float baseOutline = 1f;
        float scaleFactor = Math.min(this.width, this.height) / 100f;
        float scaledOutline = baseOutline * scaleFactor;

        serviceContext.getRender2DService().renderSoftRoundedRectOutline(
                context.getMatrices(),
                this.x, this.y, Math.max(1, this.width), Math.max(1, this.height), renderPriority,
                10f,
                new Color(0x002AFF),
                outlineColor,
                scaledOutline, 3
        );

        boolean mouseHeld = serviceContext.getInputService().isMouseButtonHeld(0);

        if (mouseHeld) {
            if (dragging == null) {
                for (DrawableModule<?> d : serviceContext.getGuiManagerService().getDrawableModules()) {
                    if (d.isMouseOver(mouseX, mouseY)) {
                        dragging = d;
                        dragOffsetX = mouseX - d.getDrawableElement().getX();
                        dragOffsetY = mouseY - d.getDrawableElement().getY();
                        break;
                    }
                }
            }

            if (dragging != null) {
                dragging.getDrawableElement().setX(mouseX - dragOffsetX);
                dragging.getDrawableElement().setY(mouseY - dragOffsetY);
            }
        } else {
            dragging = null;
        }

        super.render(context, mouseX, mouseY, tickDelta);
    }
}