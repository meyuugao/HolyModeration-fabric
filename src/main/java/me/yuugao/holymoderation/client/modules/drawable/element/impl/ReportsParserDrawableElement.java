package me.yuugao.holymoderation.client.modules.drawable.element.impl;

import me.yuugao.holymoderation.client.config.GuiConfig;
import me.yuugao.holymoderation.client.config.manager.ConfigManager;
import me.yuugao.holymoderation.client.modules.drawable.element.StatefulDrawableElement;
import me.yuugao.holymoderation.client.modules.drawable.element.impl.button.ButtonDrawableElement;
import me.yuugao.holymoderation.client.modules.drawable.element.state.impl.ReportsParserRenderState;
import me.yuugao.holymoderation.client.modules.drawable.element.state.provider.impl.ReportsParserRenderStateProvider;
import me.yuugao.holymoderation.client.modules.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.modules.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.Render2DService;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

public class ReportsParserDrawableElement extends StatefulDrawableElement<ReportsParserRenderState> {
    public final ButtonDrawableElement startButton;

    public ReportsParserDrawableElement(ServiceContext serviceContext, PivotMode positionMode) {
        super(serviceContext, positionMode, new ReportsParserRenderStateProvider(serviceContext));
        this.startButton = new ButtonDrawableElement(serviceContext, PivotMode.DOWN,
                () -> System.out.println("button clicked"), true);
    }

    @Override
    protected void initPosition(DrawContext ctx) {
        setRelativePos(0.3f, 0.4f);
        this.scale = 0f;
    }

    @Override
    protected void render(DrawContext ctx, int z, ReportsParserRenderState state) {
        ConfigManager configManager = serviceContext.getConfigManager();
        Render2DService render2DService = serviceContext.getRender2DService();

        MatrixStack ms = ctx.getMatrices();
        GuiConfig guiConfig = configManager.getGuiConfig();

        float animTarget = state.animTarget();

        this.scale = animate(scale, animTarget, 1f);
        if (scale < 0.01f) return;

        setWidth(150f);
        setHeight(180f);

        render2DService.setupRender();

        ms.push();

        render2DService.renderSoftRoundedRectOutline(ms, 0f, 0f, getWidth(), getHeight(), z, getHeight() / 8f,
                guiConfig.getMainColor(), guiConfig.getSecondColor(), 2f, 3f);

        float buttonWidth = 110f;

        startButton.updateRenderForParent(ctx, Text.literal("пропарсить"), 0.5f, 0.95f,
                buttonWidth, getWidth(), getHeight(), z, 6f, //tip: height / 8f
                guiConfig.getMainColor().brighter(), guiConfig.getSecondColor().brighter(), 2f, 3f);

        ms.pop();

        render2DService.endRender();
    }
}