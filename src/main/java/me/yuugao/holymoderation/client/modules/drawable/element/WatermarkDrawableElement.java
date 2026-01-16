package me.yuugao.holymoderation.client.modules.drawable.element;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

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
    protected void renderContent(DrawContext ctx, int z) {
        MatrixStack ms = ctx.getMatrices();
        tickCounter++;
        if (tickCounter % 18 == 0) updateAnimText();

        float animTarget = serviceContext.getConfigManager().getConfig().isWatermarkEnabled() ? 1f : 0f;
        anim += (animTarget - anim) * 0.15f;
        if (anim < 0.01f && !serviceContext.getConfigManager().getConfig().isWatermarkEnabled()) return;

        String text = "HolyModeration v" + serviceContext.getConfigManager().getConfig().getCurrentVersion() + " | " + serviceContext.getStateService().getUserNickname() + " | " + new String(animBuffer);

        TextRenderer tr = serviceContext.getMinecraftService().getClient().textRenderer;

        width = (tr.getWidth(text) + 12f);
        height = (tr.fontHeight + 8f);

        Color bg = new Color(10, 20, 40, 220);
        Color outline = new Color(60, 120, 220);

        serviceContext.getRender2DService().setupRender();

        ms.push();

        float[] tl = topLeftLocal();
        float[] pv = scalePivotLocal();

        ms.translate(pv[0], pv[1], 0f);
        ms.scale(anim, anim, 1f);
        ms.translate(-pv[0], -pv[1], 0f);

        ms.translate(tl[0], tl[1], 0f);

        serviceContext.getRender2DService().renderSoftRoundedRectOutline(
                ms, 0f, 0f, width, height, z, 8f, bg, outline, 1.2f, 3
        );

        serviceContext.getRender2DService().renderText(
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

        serviceContext.getRender2DService().endRender();
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