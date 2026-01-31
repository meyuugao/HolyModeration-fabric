package me.yuugao.holymoderation.client.modules.drawable.element;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.MinecraftService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.Render2DService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.StateService;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

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
    protected boolean shouldRender() {
        return !(display0.isEmpty() && display1.isEmpty());
    }

    @Override
    protected void renderContent(DrawContext ctx, int z) {
        MinecraftService minecraftService = serviceContext.getMinecraftService();
        Render2DService render2DService = serviceContext.getRender2DService();

        MatrixStack ms = ctx.getMatrices();
        anim += (animTarget - anim) * 0.15f;
        if (anim < 0.01f) return;

        String[] current = getStringsToRender();
        if (!(current[0].isEmpty() && current[1].isEmpty())) {
            display0 = current[0];
            display1 = current[1];
        }

        TextRenderer tr = minecraftService.getClient().textRenderer;

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

        render2DService.setupRender();

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
        render2DService.renderSoftRoundedRectOutline(ms, 0f, 0f, width, height, z,
                10f, bg, outline, 1.5f, 3);

        render2DService.renderText(tr, display0, (int) (width / 2f - tr.getWidth(display0) / 2f),
                (int) baseY, z, 0xffffffff, false, ctx);
        if (!display1.isEmpty()) {
            render2DService.renderText(tr, display1, (int) (width / 2f - tr.getWidth(display1) / 2f),
                    (int) (baseY + tr.fontHeight + 4), z, 0xffffffff, false, ctx);
        }

        ms.pop();

        render2DService.endRender();

        if (anim < 0.02f && animTarget == 0f && clearDisplayWhenHidden) {
            display0 = display1 = StringUtils.EMPTY;
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
        StateService stateService = serviceContext.getStateService();

        String spyPlayer = stateService.getSpyPlayer();
        if (spyPlayer.isEmpty()) return new String[]{StringUtils.EMPTY, StringUtils.EMPTY};

        String spyPlayerStatus = stateService.getSpyPlayerStatus();
        String spyPlayerActivity = stateService.getSpyPlayerActivity();

        if (spyPlayerStatus.isEmpty())
            return new String[]{spyPlayer, StringUtils.EMPTY};

        return switch (spyPlayerStatus) {
            case "stop" -> new String[]{"Слежка приостановлена", StringUtils.EMPTY};
            case "offline" -> new String[]{"Игрок %s оффлайн".formatted(spyPlayer), StringUtils.EMPTY};
            case "lobby" -> new String[]{"Игрок %s в лобби".formatted(spyPlayer), StringUtils.EMPTY};
            default -> new String[]{
                    "Игрок %s находится на %s".formatted(spyPlayer, spyPlayerStatus),
                    spyPlayerActivity == null ? StringUtils.EMPTY : "Активность: %s".formatted(spyPlayerActivity)
            };
        };
    }
}