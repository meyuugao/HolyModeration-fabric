package me.yuugao.holymoderation.client.modules.drawable.element;

import me.yuugao.holymoderation.client.config.GuiConfig;
import me.yuugao.holymoderation.client.config.manager.ConfigManager;
import me.yuugao.holymoderation.client.modules.drawable.element.state.CheckoutsRenderState;
import me.yuugao.holymoderation.client.modules.drawable.element.state.SpyRenderState;
import me.yuugao.holymoderation.client.modules.drawable.element.state.provider.CheckoutsRenderStateProvider;
import me.yuugao.holymoderation.client.modules.drawable.render.PositionMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.MinecraftService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.Render2DService;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import org.apache.commons.lang3.StringUtils;

import java.awt.Color;

public class CheckoutsDrawableElement extends DrawableElement<CheckoutsRenderState> {
    private float anim = 0f;
    private float animTarget = 0f;
    private float currentWidth = 1f;
    private float currentHeight = 1f;
    private long checkoutStartMillis = 0L;
    private String lastPlayer = StringUtils.EMPTY;

    public CheckoutsDrawableElement(ServiceContext serviceContext, PositionMode positionMode) {
        super(serviceContext, positionMode, new CheckoutsRenderStateProvider(serviceContext));
    }

    @Override
    protected void initPosition(DrawContext ctx) {
        this.relX = 0.5f;
        this.relY = 0.9f;
    }

    @Override
    protected void render(DrawContext ctx, int z, CheckoutsRenderState renderState) {
        anim = animate(anim, animTarget, 1f);

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

        if (anim < 0.01f) return;

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

        currentWidth = animate(currentWidth, targetWidth, 1.2f);
        currentHeight = animate(currentHeight, targetHeight, 1.2f);

        setWidth(Math.max(1f, currentWidth));
        setHeight(Math.max(1f, currentHeight));

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

    @Override
    protected float getCurrentScale() {
        return anim;
    }
}