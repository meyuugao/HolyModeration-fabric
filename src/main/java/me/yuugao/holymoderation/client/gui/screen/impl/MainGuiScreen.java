package me.yuugao.holymoderation.client.gui.screen.impl;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.screen.AnimatedGuiScreen;
import me.yuugao.holymoderation.client.gui.tabs.impl.main.GeneralTab;
import me.yuugao.holymoderation.client.util.factory.DrawableElementFactory;
import me.yuugao.holymoderation.client.util.service.AnimationService;
import me.yuugao.holymoderation.client.util.service.Render2DService;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.config.impl.GuiConfig;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.awt.Color;

import lombok.Getter;

@Singleton
public class MainGuiScreen extends AnimatedGuiScreen {
    @Getter
    private final int renderPriority = 2000;
    private final Color outlineColor = Color.WHITE;

    private final Render2DService render2DService;
    private final ConfigManagerService configManagerService;

    @Inject
    public MainGuiScreen(AnimationService animationService, Render2DService render2DService,
                         ConfigManagerService configManagerService, DrawableElementFactory drawableElementFactory) {
        super(Text.of("HolyModeration Main Gui Screen"), animationService);
        this.tabs.put("General", new GeneralTab(this, drawableElementFactory));

        this.render2DService = render2DService;
        this.configManagerService = configManagerService;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float tickDelta) {
        super.render(ctx, mouseX, mouseY, tickDelta);

        MatrixStack ms = ctx.getMatrices();
        GuiConfig guiConfig = configManagerService.getGuiConfig();

        float targetWidth = 435f;
        float targetHeight = 300f;

        this.width = targetWidth * getAnimValue();
        this.height = targetHeight * getAnimValue();

        float baseOutline = 1f;
        float scaleFactor = Math.min(width, height) / 100f;
        float scaledOutline = baseOutline * scaleFactor;

        this.screenScale = Math.min(ctx.getScaledWindowWidth() / 1280f, ctx.getScaledWindowHeight() / 720f);

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
    }
}