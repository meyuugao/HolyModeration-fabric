package me.yuugao.holymoderation.client.modules.drawable.element;

import me.yuugao.holymoderation.client.config.GuiConfig;
import me.yuugao.holymoderation.client.config.manager.ConfigManager;
import me.yuugao.holymoderation.client.gui.modules.single.ButtonModule;
import me.yuugao.holymoderation.client.modules.drawable.element.state.ReportsParserRenderState;
import me.yuugao.holymoderation.client.modules.drawable.element.state.provider.ReportsParserRenderStateProvider;
import me.yuugao.holymoderation.client.modules.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.Render2DService;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public class ReportsParserDrawableElement extends DrawableElement<ReportsParserRenderState> {
    public ReportsParserDrawableElement(ServiceContext serviceContext, PivotMode positionMode) {
        super(serviceContext, positionMode, new ReportsParserRenderStateProvider(serviceContext));
    }

    @Override
    protected void initPosition(DrawContext ctx) {
        this.relX = 0.3f;
        this.relY = 0.4f;
    }

    @Override
    protected void render(DrawContext ctx, int z, ReportsParserRenderState state) {
        ConfigManager configManager = serviceContext.getConfigManager();
        Render2DService render2DService = serviceContext.getRender2DService();

        MatrixStack ms = ctx.getMatrices();
        GuiConfig guiConfig = configManager.getGuiConfig();

        float animTarget = state.animTarget();

        ButtonModule startButton = new ButtonModule(this.relX, this.relY, () -> {
            System.out.println("button clicked");
        }, true, serviceContext);

        globalScale = animate(globalScale, animTarget, 1f);
        if (globalScale < 0.01f) return;

        setWidth(Math.min(ctx.getScaledWindowWidth(), ctx.getScaledWindowHeight()) / 15f);
        setHeight(Math.min(ctx.getScaledWindowWidth(), ctx.getScaledWindowHeight()) / 15f);

        render2DService.setupRender();

        ms.push();

        startButton.render(ctx.getMatrices(), getWidth(), getHeight(), ctx.getScaledWindowWidth(), ctx.getScaledWindowHeight(),
                z, guiConfig.getMainColor(), guiConfig.getSecondColor());

        ms.pop();

        render2DService.endRender();
    }
}