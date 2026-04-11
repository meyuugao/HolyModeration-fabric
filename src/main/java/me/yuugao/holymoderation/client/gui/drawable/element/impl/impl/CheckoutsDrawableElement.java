package me.yuugao.holymoderation.client.gui.drawable.element.impl.impl;

import me.yuugao.holymoderation.client.config.impl.GuiConfig;
import me.yuugao.holymoderation.client.config.manager.ConfigManager;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.StatefulDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.state.impl.CheckoutsRenderState;
import me.yuugao.holymoderation.client.gui.drawable.element.state.provider.impl.CheckoutsRenderStateProvider;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.AnimationService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.MinecraftService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.Render2DService;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import org.apache.commons.lang3.StringUtils;

public class CheckoutsDrawableElement extends StatefulDrawableElement<CheckoutsRenderState> {
    private float animTarget = 0f;
    private final AnimationService.Value currentWidth;
    private final AnimationService.Value currentHeight;

    private long checkoutStartMillis = 0L;
    private String lastPlayer = StringUtils.EMPTY;

    public CheckoutsDrawableElement(ServiceContext serviceContext, PivotMode positionMode) {
        super(serviceContext, positionMode, new CheckoutsRenderStateProvider(serviceContext));
        this.currentWidth = animationService.createValue(1f);
        this.currentHeight = animationService.createValue(1f);
    }

    @Override
    protected void initPosition(DrawContext ctx) {
        setRelativePos(0.5f, 0.9f);
        this.scale.reset(0f);
    }

    @Override
    protected void render(DrawContext ctx, int z, CheckoutsRenderState renderState) {
        String player = renderState.checkoutPlayer();
        if (!lastPlayer.equals(player)) {
            if (!player.isEmpty()) {
                checkoutStartMillis = System.currentTimeMillis();
                animTarget = 1f;
            } else {
                checkoutStartMillis = 0L;
                animTarget = 0f;
            }
            lastPlayer = player;
        }

        this.scale.setTarget(animTarget);
        this.scale.update();

        if (scale.get() < 0.01f) return;

        long elapsed = checkoutStartMillis == 0L
                ? 0L
                : (System.currentTimeMillis() - checkoutStartMillis) / 1000L;

        String content = "Текущая проверка: %s | %s:%s".formatted(
                player.isEmpty() ? lastPlayer : player,
                elapsed / 60,
                String.format("%02d", elapsed % 60)
        );

        renderContent(content, ctx, z);
    }

    private void renderContent(String display, DrawContext ctx, int z) {
        MinecraftService minecraftService = serviceContext.getMinecraftService();
        Render2DService render2DService = serviceContext.getRender2DService();
        ConfigManager configManager = serviceContext.getConfigManager();

        TextRenderer tr = minecraftService.getClient().textRenderer;
        MatrixStack ms = ctx.getMatrices();
        GuiConfig guiConfig = configManager.getGuiConfig();

        float targetWidth = tr.getWidth(display) + 16f;
        float targetHeight = tr.fontHeight + 12f;

        this.currentWidth.setTarget(targetWidth).setSpeed(1.2f);
        this.currentHeight.setTarget(targetHeight).setSpeed(1.2f);
        this.currentWidth.update();
        this.currentHeight.update();

        setWidth(Math.max(1f, currentWidth.get()));
        setHeight(Math.max(1f, currentHeight.get()));

        render2DService.setupRender();

        ms.push();

        render2DService.renderSoftRoundedRectOutline(
                ms, 0f, 0f, getWidth(), getHeight(), z,
                10f, guiConfig.getMainColor(), guiConfig.getSecondColor(), 1.5f, 3);

        render2DService.renderText(
                tr,
                display,
                (int) (getWidth() / 2f - tr.getWidth(display) / 2f),
                (int) (getHeight() / 2f - tr.fontHeight / 2f + 0.5f),
                z,
                0xffffffff,
                false,
                ctx
        );

        ms.pop();

        render2DService.endRender();
    }

    public void coStartForLocal(String player) {
        checkoutStartMillis = System.currentTimeMillis();
        lastPlayer = player;
        animTarget = 1f;
    }
}