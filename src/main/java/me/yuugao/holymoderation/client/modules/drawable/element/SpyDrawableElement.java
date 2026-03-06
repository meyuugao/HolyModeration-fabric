package me.yuugao.holymoderation.client.modules.drawable.element;

import me.yuugao.holymoderation.client.config.GuiConfig;
import me.yuugao.holymoderation.client.config.manager.ConfigManager;
import me.yuugao.holymoderation.client.modules.drawable.element.state.SpyRenderState;
import me.yuugao.holymoderation.client.modules.drawable.element.state.provider.SpyRenderStateProvider;
import me.yuugao.holymoderation.client.modules.drawable.render.PositionMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.MinecraftService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.Render2DService;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import org.apache.commons.lang3.StringUtils;

import java.awt.Color;

public class SpyDrawableElement extends DrawableElement<SpyRenderState> {
    private float anim = 0f;
    private float currentWidth = 1f;
    private float currentHeight = 1f;

    public SpyDrawableElement(ServiceContext serviceContext, PositionMode positionMode) {
        super(serviceContext, positionMode, new SpyRenderStateProvider(serviceContext));
    }

    @Override
    protected void initPosition(DrawContext ctx) {
        this.relX = 0.5f;
        this.relY = 0.02f;
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

        anim = animate(anim, animTarget, 1f);
        if (anim < 0.01f) return;

        float targetWidth = Math.max(tr.getWidth(display0), tr.getWidth(display1)) + 16f;
        int lines = display1.isEmpty() ? 1 : 2;
        float textBlockHeight = lines * tr.fontHeight + (lines == 2 ? 4 : 0);
        float targetHeight = textBlockHeight + 12f;

        currentWidth = animate(currentWidth, targetWidth, 1.2f);
        currentHeight = animate(currentHeight, targetHeight, 1.2f);

        setWidth(Math.max(1f, currentWidth));
        setHeight(Math.max(1f, currentHeight));

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

    @Override
    protected float getCurrentScale() {
        return anim;
    }
}