package me.yuugao.holymoderation.client.gui.drawable.element.impl.impl;

import me.yuugao.holymoderation.client.config.impl.GuiConfig;
import me.yuugao.holymoderation.client.config.manager.ConfigManager;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.StatefulDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.state.impl.WatermarkRenderState;
import me.yuugao.holymoderation.client.gui.drawable.element.state.provider.impl.WatermarkRenderStateProvider;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.MinecraftService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.Render2DService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.StateService;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.util.Random;

public class WatermarkDrawableElement extends StatefulDrawableElement<WatermarkRenderState> {
    private static final char[][] LEET = {
            {'a', '4'}, {'e', '3'}, {'i', '1'}, {'o', '0'}, {'s', '5'}, {'l', '1'}
    };

    private final Random random = new Random();
    private final char[] baseAnimText = "made for holyworld".toCharArray();
    private final char[] animBuffer = baseAnimText.clone();
    private int tickCounter;

    public WatermarkDrawableElement(ServiceContext serviceContext, PivotMode positionMode) {
        super(serviceContext, positionMode, new WatermarkRenderStateProvider(serviceContext));
    }

    @Override
    protected void initPosition(DrawContext ctx) {
        float rel = 0.01f;
        float minSide = Math.min(ctx.getScaledWindowWidth(), ctx.getScaledWindowHeight());
        float relX = rel * minSide / ctx.getScaledWindowWidth();
        float relY = rel * minSide / ctx.getScaledWindowHeight();
        setRelativePos(relX, relY);
        this.scale.reset(0f);
    }

    @Override
    protected void render(DrawContext ctx, int z, WatermarkRenderState state) {
        StateService stateService = serviceContext.getStateService();
        MinecraftService minecraftService = serviceContext.getMinecraftService();
        Render2DService render2DService = serviceContext.getRender2DService();
        ConfigManager configManager = serviceContext.getConfigManager();

        TextRenderer tr = minecraftService.getClient().textRenderer;
        MatrixStack ms = ctx.getMatrices();
        GuiConfig guiConfig = configManager.getGuiConfig();

        this.tickCounter++;
        if (tickCounter % 18 == 0) updateAnimText();

        float animTarget = state.animTarget();
        this.scale.setTarget(animTarget);
        this.scale.update();

        if (scale.get() < 0.01f) return;

        String text = "HolyModeration v%s | %s | %s".formatted(
                serviceContext.getConfigManager().getApiConfig().getCurrentVersion(),
                stateService.getUserNickname(),
                new String(animBuffer)
        );

        setWidth(tr.getWidth(text) + 12f);
        setHeight(tr.fontHeight + 8f);

        render2DService.setupRender();

        ms.push();

        render2DService.renderSoftRoundedRectOutline(ms, 0f, 0f, getWidth(),
                getHeight(), z, 8f, guiConfig.getMainColor(), guiConfig.getSecondColor(), 1.2f, 3);

        render2DService.renderText(tr, text, (int) (getWidth() / 2f - tr.getWidth(text) / 2f),
                (int) (getHeight() / 2f - tr.fontHeight / 2f + 0.5f), z, 0xffffffff, false, ctx);

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