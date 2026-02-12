package me.yuugao.holymoderation.client.modules.drawable.element;

import me.yuugao.holymoderation.client.modules.drawable.element.state.CheckoutsRenderState;
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
    private boolean clearDisplayWhenHidden = false;

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
            if (lastPlayer.isEmpty() && !player.isEmpty()) {
                checkoutStartMillis = System.currentTimeMillis();
                animTarget = 1f;
            } else if (!lastPlayer.isEmpty() && player.isEmpty()) {
                animTarget = 0f;
                clearDisplayWhenHidden = true;
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

        if (anim < 0.02f && animTarget == 0f && clearDisplayWhenHidden) {
            checkoutStartMillis = 0L;
            clearDisplayWhenHidden = false;
            lastPlayer = StringUtils.EMPTY;
        }
    }

    private void renderContent(String display, DrawContext ctx, int z) {
        Render2DService render2DService = serviceContext.getRender2DService();
        MinecraftService minecraftService = serviceContext.getMinecraftService();

        TextRenderer tr = minecraftService.getClient().textRenderer;
        MatrixStack ms = ctx.getMatrices();

        float targetWidth = tr.getWidth(display) + 16f;
        float targetHeight = tr.fontHeight + 12f;

        currentWidth = animate(currentWidth, targetWidth, 1.2f);
        currentHeight = animate(currentHeight, targetHeight, 1.2f);

        width = Math.max(1f, currentWidth);
        height = Math.max(1f, currentHeight);

        Color bg = new Color(10, 20, 40, 220);
        Color outline = new Color(60, 120, 220);

        float[] pv = scalePivotLocal();

        render2DService.setupRender();

        ms.push();

        ms.scale(anim, anim, 1f);
        ms.translate(-pv[0], -pv[1], 0f);

        render2DService.renderSoftRoundedRectOutline(
                ms, 0f, 0f, width, height, z,
                10f, bg, outline, 1.5f, 3
        );

        render2DService.renderText(
                tr,
                display,
                (int) (width / 2f - tr.getWidth(display) / 2f),
                (int) (height / 2f - tr.fontHeight / 2f + 0.5f),
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