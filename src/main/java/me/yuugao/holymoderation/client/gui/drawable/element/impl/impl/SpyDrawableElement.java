package me.yuugao.holymoderation.client.gui.drawable.element.impl.impl;

import me.yuugao.holymoderation.client.config.impl.GuiConfig;
import me.yuugao.holymoderation.client.config.manager.ConfigManager;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.StatefulDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.state.impl.SpyRenderState;
import me.yuugao.holymoderation.client.gui.drawable.element.state.provider.impl.SpyRenderStateProvider;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.AnimationService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.MinecraftService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.Render2DService;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import org.apache.commons.lang3.StringUtils;

public class SpyDrawableElement extends StatefulDrawableElement<SpyRenderState> {
    private final AnimationService.Value currentWidth;
    private final AnimationService.Value currentHeight;

    public SpyDrawableElement(ServiceContext serviceContext, PivotMode positionMode) {
        super(serviceContext, positionMode, new SpyRenderStateProvider(serviceContext));
        this.currentWidth = animationService.createValue(1f);
        this.currentHeight = animationService.createValue(1f);
    }

    @Override
    protected void initPosition(DrawContext ctx) {
        setRelativePos(0.5f, 0.02f);
        this.scale.reset(0f);
    }

    @Override
    protected void render(DrawContext ctx, int z, SpyRenderState renderState) {
        MinecraftService minecraftService = serviceContext.getMinecraftService();
        Render2DService render2DService = serviceContext.getRender2DService();
        ConfigManager configManager = serviceContext.getConfigManager();

        TextRenderer tr = minecraftService.getClient().textRenderer;
        MatrixStack ms = ctx.getMatrices();
        GuiConfig guiConfig = configManager.getGuiConfig();

        String[] current = renderState.stringsToRender();
        float animTarget;
        String display0, display1;
        if (!(current[0].isEmpty() && current[1].isEmpty())) {
            display0 = current[0];
            display1 = current[1];
            animTarget = 1f;
        } else {
            display0 = display1 = StringUtils.EMPTY;
            animTarget = 0f;
        }

        this.scale.setTarget(animTarget);
        this.scale.update();

        if (scale.get() < 0.01f) return;

        float targetWidth = Math.max(tr.getWidth(display0), tr.getWidth(display1)) + 16f;
        int lines = display1.isEmpty() ? 1 : 2;
        float textBlockHeight = lines * tr.fontHeight + (lines == 2 ? 4 : 0);
        float targetHeight = textBlockHeight + 12f;

        this.currentWidth.setTarget(targetWidth).setSpeed(1.2f);
        this.currentHeight.setTarget(targetHeight).setSpeed(1.2f);
        this.currentWidth.update();
        this.currentHeight.update();

        setWidth(Math.max(1f, currentWidth.get()));
        setHeight(Math.max(1f, currentHeight.get()));

        render2DService.setupRender();

        ms.push();

        float baseY = (getHeight() - textBlockHeight) / 2f + 0.5f;
        render2DService.renderSoftRoundedRectOutline(ms, 0f, 0f, getWidth(), getHeight(), z,
                10f, guiConfig.getMainColor(), guiConfig.getSecondColor(), 1.5f, 3);

        render2DService.renderText(tr, display0, (int) (getWidth() / 2f - tr.getWidth(display0) / 2f),
                (int) baseY, z, 0xffffffff, false, ctx);
        if (!display1.isEmpty()) {
            render2DService.renderText(tr, display1, (int) (getWidth() / 2f - tr.getWidth(display1) / 2f),
                    (int) (baseY + tr.fontHeight + 4), z, 0xffffffff, false, ctx);
        }

        ms.pop();

        render2DService.endRender();
    }
}