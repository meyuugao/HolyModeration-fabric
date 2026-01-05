package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.HudRenderEvent;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.util.Random;

public class WaterMarkModule extends Module {
    private static final char[][] LEET = {
            {'a','4'},
            {'e','3'},
            {'i','1'},
            {'o','0'},
            {'s','5'},
            {'l','1'}
    };

    private final Random random = new Random();
    private final char[] baseAnimText = "made for holyworld".toCharArray();
    private final char[] animBuffer = baseAnimText.clone();

    private int tickCounter;

    public WaterMarkModule(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Subscribe(priority = 100)
    public void onHudRender(HudRenderEvent event) {
        tickCounter++;
        if (tickCounter % 18 == 0) updateAnimText();

        String titleText =
                "HolyModeration v" + serviceContext.getConfigManager().getConfig().getCurrentVersion()
                        + " | " + serviceContext.getStateService().getUserNickname()
                        + " | " + new String(animBuffer);

        DrawContext ctx = event.getDrawContext();
        TextRenderer tr = serviceContext.getMinecraftService().getClient().textRenderer;
        MatrixStack ms = ctx.getMatrices();

        float padding = 6f;
        float widthPadding = 6f;
        float heightPadding = 4f;

        int textWidth = tr.getWidth(titleText);
        int textHeight = tr.fontHeight;

        float width = textWidth + widthPadding * 2;
        float height = textHeight + heightPadding * 2;

        Color bg = new Color(10, 20, 40, 220);
        Color outline = new Color(60, 120, 220);

        serviceContext.getRender2DService().renderSoftRoundedRectOutline(
                ms,
                padding,
                padding - 0.5f,
                width,
                height,
                8f,
                bg,
                outline,
                1.2f,
                3
        );

        serviceContext.getRender2DService().renderText(
                tr,
                titleText,
                padding + widthPadding,
                padding + heightPadding,
                0xffffffff,
                false,
                ctx
        );
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
                if (animBuffer[i] == base && random.nextInt(4) == 0) {
                    animBuffer[i] = map[1];
                } else {
                    animBuffer[i] = base;
                }
                return;
            }
        }

        animBuffer[i] = base;
    }
}