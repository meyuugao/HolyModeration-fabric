package me.yuugao.holymoderation.client.gui.screen.impl;

import me.yuugao.holymoderation.client.config.GuiConfig;
import me.yuugao.holymoderation.client.config.manager.ConfigManager;
import me.yuugao.holymoderation.client.gui.screen.AnimatedGuiScreen;
import me.yuugao.holymoderation.client.gui.tabs.impl.main.GeneralTab;
import me.yuugao.holymoderation.client.modules.drawable.DrawableModule;
import me.yuugao.holymoderation.client.modules.drawable.element.DrawableElement;
import me.yuugao.holymoderation.client.modules.drawable.element.impl.ReportsParserDrawableElement;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.GuiManagerService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.InputService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.Render2DService;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
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
        super.render(ctx, mouseX, mouseY, tickDelta);

        Render2DService render2DService = serviceContext.getRender2DService();
        ConfigManager configManager = serviceContext.getConfigManager();
        InputService inputService = serviceContext.getInputService();
        GuiManagerService guiManagerService = serviceContext.getGuiManagerService();

        MatrixStack ms = ctx.getMatrices();
        GuiConfig guiConfig = configManager.getGuiConfig();

        float targetWidth = 435f;
        float targetHeight = 300f;

        this.width = targetWidth * getAnimValue();
        this.height = targetHeight * getAnimValue();

        float baseOutline = 1f;
        float scaleFactor = Math.min(width, height) / 100f;
        float scaledOutline = baseOutline * scaleFactor;

        this.screenScale = Math.min(ctx.getScaledWindowWidth() / 960f, ctx.getScaledWindowHeight() / 540f);

        render2DService.setupRender();

        ms.push();

        float windowWidth = ctx.getScaledWindowWidth();
        float windowHeight = ctx.getScaledWindowHeight();

        this.x = (windowWidth - width * screenScale) / 2f;
        this.y = (windowHeight - height * screenScale) / 2f;

        ms.translate(x, y, 0);
        ms.scale(screenScale, screenScale, 1f);

        render2DService.renderSoftRoundedRectOutline(
                ms, 0f, 0f,
                Math.max(1, this.width), Math.max(1, this.height),
                renderPriority, 10f,
                guiConfig.getSecondColor(), outlineColor,
                scaledOutline, 3
        );

        int relMouseX = (int) ((mouseX - x) / screenScale);
        int relMouseY = (int) ((mouseY - y) / screenScale);
        renderTabs(ctx, relMouseX, relMouseY, tickDelta);

        ms.pop();

        render2DService.endRender();

        if (inputService.isMouseButtonHeld(0)) {
            if (dragging == null && inputService.wasMouseButtonPressed(0)) {
                ArrayList<DrawableModule<?>> list = new ArrayList<>(guiManagerService.getDrawableModules());
                Collections.reverse(list);
                for (DrawableModule<?> d : list) {
                    DrawableElement elem = d.getDrawableElement();

                    float sw = ctx.getScaledWindowWidth();
                    float sh = ctx.getScaledWindowHeight();
                    float anchorX = elem.getAnchorX(sw);
                    float anchorY = elem.getAnchorY(sh);

                    float visW = elem.getScaledWidth() * screenScale;
                    float visH = elem.getScaledHeight() * screenScale;
                    float visPivotX = elem.getPivotOffsetX() * screenScale;
                    float visPivotY = elem.getPivotOffsetY() * screenScale;

                    float left = anchorX - visPivotX;
                    float top = anchorY - visPivotY;

                    if (mouseX >= left && mouseX <= left + visW && mouseY >= top && mouseY <= top + visH) {
                        dragging = d;
                        dragOffsetX = mouseX - anchorX;
                        dragOffsetY = mouseY - anchorY;
                        break;
                    }
                }
            }

            if (dragging != null) {
                DrawableElement elem = dragging.getDrawableElement();
                float sw = ctx.getScaledWindowWidth();
                float sh = ctx.getScaledWindowHeight();

                float targetAnchorX = mouseX - dragOffsetX;
                float targetAnchorY = mouseY - dragOffsetY;

                float newRelX = targetAnchorX / sw;
                float newRelY = targetAnchorY / sh;

                elem.setRelativePos(newRelX, newRelY);
            }
        } else {
            dragging = null;
        }
    }
}