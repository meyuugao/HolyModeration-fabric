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
    private float coAnim = 0f;
    private float coAnimTarget = 0f;
    private float coCurrentWidth = 1f;
    private float coCurrentHeight = 1f;
    private long checkoutStartMillis = 0L;
    private String coLastPlayer = StringUtils.EMPTY;
    private boolean coClearDisplayWhenHidden = false;

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
        coAnim += (coAnimTarget - coAnim) * 0.15f;

        String player = renderState.checkoutPlayer();
        if (!coLastPlayer.equals(player)) {
            if (coLastPlayer.isEmpty() && !player.isEmpty()) {
                checkoutStartMillis = System.currentTimeMillis();
                coAnimTarget = 1f;
            } else if (!coLastPlayer.isEmpty() && player.isEmpty()) {
                coAnimTarget = 0f;
                coClearDisplayWhenHidden = true;
            }
            coLastPlayer = player;
        }

        if (coAnim < 0.01f) return;

        long elapsed = checkoutStartMillis == 0L
                ? 0L
                : (System.currentTimeMillis() - checkoutStartMillis) / 1000L;

        String content = "Текущая проверка: %s | %s:%s".formatted(
                player.isEmpty() ? coLastPlayer : player,
                elapsed / 60,
                String.format("%02d", elapsed % 60)
        );

        renderContent(content, ctx, z);

        if (coAnim < 0.02f && coAnimTarget == 0f && coClearDisplayWhenHidden) {
            checkoutStartMillis = 0L;
            coClearDisplayWhenHidden = false;
            coLastPlayer = StringUtils.EMPTY;
        }
    }

    private void renderContent(String display, DrawContext ctx, int z) {
        Render2DService render2DService = serviceContext.getRender2DService();
        MinecraftService minecraftService = serviceContext.getMinecraftService();

        TextRenderer tr = minecraftService.getClient().textRenderer;
        MatrixStack ms = ctx.getMatrices();

        float targetWidth = tr.getWidth(display) + 16f;
        float targetHeight = tr.fontHeight + 12f;

        coCurrentWidth += (targetWidth - coCurrentWidth) * 0.2f;
        coCurrentHeight += (targetHeight - coCurrentHeight) * 0.2f;

        width = Math.max(1f, coCurrentWidth);
        height = Math.max(1f, coCurrentHeight);

        Color bg = new Color(10, 20, 40, 220);
        Color outline = new Color(60, 120, 220);

        float[] tl = topLeftLocal();
        float[] pv = scalePivotLocal();

        render2DService.setupRender();

        ms.push();
        ms.translate(tl[0] + pv[0], tl[1] + pv[1], 0f);
        ms.scale(coAnim, coAnim, 1f);
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
        coLastPlayer = player;
        coAnimTarget = 1f;
    }
}