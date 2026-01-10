package me.yuugao.holymoderation.client.modules.drawable.element;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import java.awt.Color;

public class SpyDrawableElement extends DrawableElement {
    private float anim = 0f;
    private float animTarget = 0f;
    private float currentWidth = 1f;
    private float currentHeight = 1f;
    private String display0 = StringUtils.EMPTY;
    private String display1 = StringUtils.EMPTY;
    private boolean clearDisplayWhenHidden = false;

    public SpyDrawableElement(ServiceContext serviceContext, PositionMode positionMode) {
        super(serviceContext, positionMode);
    }

    @Override
    protected void initPosition(DrawContext ctx) {
        this.relX = 0.5f;
        this.relY = 0.02f;
    }

    @Override
    protected void renderContent(DrawContext ctx, int z) {
        MatrixStack ms = ctx.getMatrices();
        anim += (animTarget - anim) * 0.15f;
        if (anim < 0.01f && display0.isEmpty() && display1.isEmpty()) return;

        String[] current = getStringsToRender();
        if (!(current[0].isEmpty() && current[1].isEmpty())) {
            display0 = current[0];
            display1 = current[1];
        }

        TextRenderer tr = serviceContext.getMinecraftService().getClient().textRenderer;

        float targetWidth = Math.max(tr.getWidth(display0), tr.getWidth(display1)) + 16f;
        int lines = display1.isEmpty() ? 1 : 2;
        float textBlockHeight = lines * tr.fontHeight + (lines == 2 ? 4 : 0);
        float targetHeight = textBlockHeight + 12f;

        currentWidth += (targetWidth - currentWidth) * 0.2f;
        currentHeight += (targetHeight - currentHeight) * 0.2f;

        width = Math.max(1f, currentWidth);
        height = Math.max(1f, currentHeight);

        Color bg = new Color(10, 20, 40, 220);
        Color outline = new Color(60, 120, 220);

        serviceContext.getRender2DService().setupRender();

        float[] tl = topLeftLocal();
        float[] pv = scalePivotLocal();
        float tlx = tl[0];
        float tly = tl[1];
        float px = pv[0];
        float py = pv[1];

        ms.push();
        ms.translate(tlx, tly, 0f);
        ms.translate(px, py, 0f);
        ms.scale(anim, anim, 1f);
        ms.translate(-px, -py, 0f);

        float baseY = (height - textBlockHeight) / 2f + 0.5f;
        serviceContext.getRender2DService().renderSoftRoundedRectOutline(ms, 0f, 0f, width, height, z, 10f, bg, outline, 1.5f, 3);

        serviceContext.getRender2DService().renderText(tr, display0, (int) (width / 2f - tr.getWidth(display0) / 2f), (int) baseY, z, 0xffffffff, false, ctx);
        if (!display1.isEmpty()) {
            serviceContext.getRender2DService().renderText(tr, display1, (int) (width / 2f - tr.getWidth(display1) / 2f), (int) (baseY + tr.fontHeight + 4), z, 0xffffffff, false, ctx);
        }

        ms.pop();

        serviceContext.getRender2DService().endRender();

        if (anim < 0.02f && animTarget == 0f && clearDisplayWhenHidden) {
            display0 = display1 = "";
            clearDisplayWhenHidden = false;
        }
    }

    public void onStartSpy() {
        animTarget = 1f;
        String[] s = getStringsToRender();
        display0 = s[0];
        display1 = s[1];
        clearDisplayWhenHidden = true;
    }

    public void onResetSpy() {
        animTarget = 0f;
    }

    private String @NotNull [] getStringsToRender() {
        String spyPlayer = serviceContext.getStateService().getSpyPlayer();
        if (spyPlayer.isEmpty()) return new String[]{"", ""};

        if (serviceContext.getStateService().getSpyPlayerStatus().isEmpty())
            return new String[]{spyPlayer, ""};

        return switch (serviceContext.getStateService().getSpyPlayerStatus()) {
            case "stop" -> new String[]{"Слежка приостановлена", ""};
            case "offline" -> new String[]{"Игрок " + spyPlayer + " оффлайн", ""};
            case "lobby" -> new String[]{"Игрок " + spyPlayer + " в лобби", ""};
            default -> new String[]{
                    "Игрок " + spyPlayer + " находится на " + serviceContext.getStateService().getSpyPlayerStatus(),
                    serviceContext.getStateService().getSpyPlayerActivity() == null ? "" : "Активность: " + serviceContext.getStateService().getSpyPlayerActivity()
            };
        };
    }
}