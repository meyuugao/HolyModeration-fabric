package me.yuugao.holymoderation.client.gui.screen;

import me.yuugao.holymoderation.client.gui.tabs.main.GeneralTab;
import me.yuugao.holymoderation.client.modules.drawable.DrawableModule;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
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
    public void render(DrawContext ctx, int mouseX, int mouseY, float tickDelta) {
        float targetW = (float) ctx.getScaledWindowWidth() / 2.2f;
        float targetH = (float) ctx.getScaledWindowHeight() / 1.8f;

        this.width = targetW * getAnimValue();
        this.height = targetH * getAnimValue();

        this.x = (float) ctx.getScaledWindowWidth() / 2 - this.width / 2f;
        this.y = (float) ctx.getScaledWindowHeight() / 2 - this.height / 2f;

        float baseOutline = 1f;
        float scaleFactor = Math.min(this.width, this.height) / 100f;
        float scaledOutline = baseOutline * scaleFactor;

        serviceContext.getRender2DService().setupRender();

        serviceContext.getRender2DService().renderSoftRoundedRectOutline(
                ctx.getMatrices(),
                this.x, this.y, Math.max(1, this.width), Math.max(1, this.height),
                renderPriority,
                10f,
                new Color(0xB3002AFF, true),
                outlineColor,
                scaledOutline, 3
        );

        serviceContext.getRender2DService().endRender();

        boolean mouseHeld = serviceContext.getInputService().isMouseButtonHeld(0);

        if (mouseHeld) {
            if (dragging == null) {
                ArrayList<DrawableModule<?>> list = new ArrayList<>(serviceContext.getGuiManagerService().getDrawableModules());
                Collections.reverse(list);
                for (DrawableModule<?> d : list) {
                    if (d.isMouseOver(mouseX, mouseY, ctx)) {
                        dragging = d;
                        dragOffsetX = mouseX - d.getDrawableElement().getX(ctx);
                        dragOffsetY = mouseY - d.getDrawableElement().getY(ctx);
                        break;
                    }
                }
            }

            if (dragging != null) {
                dragging.getDrawableElement().setX(mouseX - dragOffsetX, ctx);
                dragging.getDrawableElement().setY(mouseY - dragOffsetY, ctx);
            }
        } else {
            dragging = null;
        }


        super.render(ctx, mouseX, mouseY, tickDelta);
    }
}