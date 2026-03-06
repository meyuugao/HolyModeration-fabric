package me.yuugao.holymoderation.client.modules.drawable.element;

import me.yuugao.holymoderation.client.config.GuiConfig;
import me.yuugao.holymoderation.client.config.manager.ConfigManager;
import me.yuugao.holymoderation.client.modules.drawable.element.state.SpyRenderState;
import me.yuugao.holymoderation.client.modules.drawable.element.state.WatermarkRenderState;
import me.yuugao.holymoderation.client.modules.drawable.element.state.provider.WatermarkRenderStateProvider;
import me.yuugao.holymoderation.client.modules.drawable.render.PositionMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.MinecraftService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.Render2DService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.StateService;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;
import java.util.Random;

public class WatermarkDrawableElement extends DrawableElement<WatermarkRenderState> {
    private static final char[][] LEET = {
            {'a', '4'}, {'e', '3'}, {'i', '1'}, {'o', '0'}, {'s', '5'}, {'l', '1'}
    };

    private final Random random = new Random();
    private final char[] baseAnimText = "made for holyworld".toCharArray();
    private final char[] animBuffer = baseAnimText.clone();
    private int tickCounter;
    private float anim = 0f;

    public WatermarkDrawableElement(ServiceContext serviceContext, PositionMode positionMode) {
        super(serviceContext, positionMode, new WatermarkRenderStateProvider(serviceContext));
    }

    @Override
    protected void initPosition(DrawContext ctx) {
        float rel = 0.01f;
        float minSide = Math.min(ctx.getScaledWindowWidth(), ctx.getScaledWindowHeight());
        this.relX = rel * minSide / ctx.getScaledWindowWidth();
        this.relY = rel * minSide / ctx.getScaledWindowHeight();
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

        tickCounter++;
        if (tickCounter % 18 == 0) updateAnimText();

        float animTarget = state.animTarget();
        anim = animate(anim, animTarget, 1f);
        if (anim < 0.01f) return;

        String text = "HolyModeration v%s | %s | %s".formatted(
                serviceContext.getConfigManager().getApiConfig().getCurrentVersion(),
                stateService.getUserNickname(),
                new String(animBuffer)
        );

        setWidth(tr.getWidth(text) + 12f);
        setHeight(tr.fontHeight + 8f);

        render2DService.setupRender();

        ms.push();

        ms.scale(anim, anim, 1f);

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

    @Override
    protected float getCurrentScale() {
        return anim;
    }
}