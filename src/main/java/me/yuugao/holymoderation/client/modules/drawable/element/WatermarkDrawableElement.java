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

    public WatermarkDrawableElement(ServiceContext serviceContext) {
        super(serviceContext);
        this.x = 25;
        this.y = 20;
    }

    @Override
    public void render(DrawContext ctx) {
        tickCounter++;
        if (tickCounter % 18 == 0) updateAnimText();

        String text = "HolyModeration v" + serviceContext.getConfigManager().getConfig().getCurrentVersion()
                + " | " + serviceContext.getStateService().getUserNickname()
                + " | " + new String(animBuffer);

        TextRenderer tr = serviceContext.getMinecraftService().getClient().textRenderer;
        MatrixStack ms = ctx.getMatrices();

        width = (tr.getWidth(text) + 12f) * widthScale;
        height = (tr.fontHeight + 8f) * heightScale;

        Color bg = new Color(10, 20, 40, 220);
        Color outline = new Color(60, 120, 220);

        ms.push();
        ms.translate(0, 0, 0);

        serviceContext.getRender2DService().renderSoftRoundedRectOutline(
                ms, x, y, width, height, 8f, bg, outline, 1.2f, 3
        );

        serviceContext.getRender2DService().renderText(
                tr, text, x, y - tr.fontHeight / 2f + 0.5f, 0xaaaaaaff, false, ctx
        );

        ms.pop();
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