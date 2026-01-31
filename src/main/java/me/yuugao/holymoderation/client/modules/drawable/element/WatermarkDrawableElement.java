package me.yuugao.holymoderation.client.modules.drawable.element;

import me.yuugao.holymoderation.client.config.ConfigManager;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.MinecraftService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.Render2DService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.StateService;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.util.Random;

public class WatermarkDrawableElement extends DrawableElement {
    private static final char[][] LEET = {
            {'a', '4'}, {'e', '3'}, {'i', '1'}, {'o', '0'}, {'s', '5'}, {'l', '1'}
    };

    private final Random random = new Random();
    private final char[] baseAnimText = "made for holyworld".toCharArray();
    private final char[] animBuffer = baseAnimText.clone();
    private int tickCounter;
    private float anim = 0f;

    public WatermarkDrawableElement(ServiceContext serviceContext, PositionMode positionMode) {
        super(serviceContext, positionMode);
    }

    @Override
    protected void initPosition(DrawContext ctx) {
        float rel = 0.01f;
        float minSide = Math.min(ctx.getScaledWindowWidth(), ctx.getScaledWindowHeight());
        this.relX = rel * minSide / ctx.getScaledWindowWidth();
        this.relY = rel * minSide / ctx.getScaledWindowHeight();
    }

    @Override
    protected boolean shouldRender() {
        ConfigManager configManager = serviceContext.getConfigManager();

        return configManager.getConfig().isWatermarkEnabled();
    }

    @Override
    protected void renderContent(DrawContext ctx, int z) {
        ConfigManager configManager = serviceContext.getConfigManager();
        StateService stateService = serviceContext.getStateService();
        MinecraftService minecraftService = serviceContext.getMinecraftService();
        Render2DService render2DService = serviceContext.getRender2DService();

        MatrixStack ms = ctx.getMatrices();
        tickCounter++;
        if (tickCounter % 18 == 0) updateAnimText();

        float animTarget = shouldRender() ? 1f : 0f;
        anim += (animTarget - anim) * 0.15f;
        if (anim < 0.01f) return;

        String text = "HolyModeration v%s | %s | %s".formatted(configManager.getConfig().getCurrentVersion(),
                stateService.getUserNickname(), new String(animBuffer));

        TextRenderer tr = minecraftService.getClient().textRenderer;

        width = (tr.getWidth(text) + 12f);
        height = (tr.fontHeight + 8f);

        Color bg = new Color(10, 20, 40, 220);
        Color outline = new Color(60, 120, 220);

        render2DService.setupRender();

        ms.push();

        float[] tl = topLeftLocal();
        float[] pv = scalePivotLocal();

        ms.translate(pv[0], pv[1], 0f);
        ms.scale(anim, anim, 1f);
        ms.translate(-pv[0], -pv[1], 0f);

        ms.translate(tl[0], tl[1], 0f);

        render2DService.renderSoftRoundedRectOutline(
                ms, 0f, 0f, width, height, z, 8f, bg, outline, 1.2f, 3
        );

        render2DService.renderText(
                tr,
                text,
                (int) (width / 2f - tr.getWidth(text) / 2f),
                (int) (height / 2f - tr.fontHeight / 2f + 0.5f),
                z,
                0xffffffff,
                false,
                ctx
        );

        ms.pop();

        render2DService.endRender();
    }

    private void updateAnimText() {
        int i = random.nextInt(animBuffer.length);
        char base = baseAnimText[i];
        if (base == ' ') {
            animBuffer[i] = base;
            return;
        }
        for (char[] map : LEET) {
            if (map[0] == base) {
                animBuffer[i] = animBuffer[i] == base && random.nextInt(4) == 0 ? map[1] : base;
                return;
            }
        }
        animBuffer[i] = base;
    }
}