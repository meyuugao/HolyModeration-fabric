package me.yuugao.holymoderation.client.modules.drawable.element;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class CheckoutsDrawableElement extends DrawableElement {
    private float coAnim = 0f;
    private float coAnimTarget = 0f;
    private float coCurrentWidth = 1f;
    private float coCurrentHeight = 1f;
    private long checkoutStartMillis = 0L;
    private String coLastPlayer = "";
    private boolean coClearDisplayWhenHidden = false;

    public CheckoutsDrawableElement(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Override
    public void renderContent(DrawContext ctx, int z) {
        MatrixStack ms = ctx.getMatrices();
        coAnim += (coAnimTarget - coAnim) * 0.15f;

        String player = serviceContext.getStateService().getPlayer();
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

        if (coAnim < 0.01f && player.isEmpty()) return;

        TextRenderer tr = serviceContext.getMinecraftService().getClient().textRenderer;

        long elapsed = checkoutStartMillis == 0L ? 0L : (System.currentTimeMillis() - checkoutStartMillis) / 1000L;
        String display = "Текущая проверка: " + (player.isEmpty() ? coLastPlayer : player) + " | " +
                elapsed / 60 + ":" + String.format("%02d", elapsed % 60);

        float targetWidth = tr.getWidth(display) + 16f;
        float targetHeight = tr.fontHeight + 12f;

        coCurrentWidth += (targetWidth - coCurrentWidth) * 0.2f;
        coCurrentHeight += (targetHeight - coCurrentHeight) * 0.2f;

        width = Math.max(1f, coCurrentWidth * coAnim) * widthScale;
        height = Math.max(1f, coCurrentHeight * coAnim) * heightScale;

        Color bg = new Color(10, 20, 40, 220);
        Color outline = new Color(60, 120, 220);

        serviceContext.getRender2DService().setupRender();

        serviceContext.getRender2DService().renderSoftRoundedRectOutline(ms, 0f, 0f, width, height, z,
                10f, bg, outline, 1.5f, 3);

        ms.push();
        ms.scale(coAnim, coAnim, 1f);

        serviceContext.getRender2DService().renderText(tr, display, (int) (-tr.getWidth(display) / 2f),
                (int) (-tr.fontHeight / 2f + 0.5f), z, 0xff0000ff, false, ctx);

        ms.pop();

        serviceContext.getRender2DService().endRender();

        if (coAnim < 0.02f && coAnimTarget == 0f && coClearDisplayWhenHidden) {
            checkoutStartMillis = 0L;
            coClearDisplayWhenHidden = false;
            coLastPlayer = "";
        }
    }

    public void coStartForLocal(String player) {
        checkoutStartMillis = System.currentTimeMillis();
        coLastPlayer = player;
        coAnimTarget = 1f;
    }
}