package me.yuugao.holymoderation.client.gui.drawable.element.state.provider.impl;

import me.yuugao.holymoderation.client.config.manager.ConfigManager;
import me.yuugao.holymoderation.client.gui.drawable.element.state.impl.WatermarkRenderState;
import me.yuugao.holymoderation.client.gui.drawable.element.state.provider.RenderStateProvider;
import me.yuugao.holymoderation.client.gui.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

public class WatermarkRenderStateProvider extends RenderStateProvider<WatermarkRenderState> {
    public WatermarkRenderStateProvider(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Override
    public WatermarkRenderState getState(RenderMode mode) {
        ConfigManager configManager = serviceContext.getConfigManager();

        return new WatermarkRenderState(switch (mode) {
            case LIVE -> configManager.getGuiConfig().isWatermarkEnabled() ? 1f : 0f;
            case CONFIG -> 1f;
        });
    }
}