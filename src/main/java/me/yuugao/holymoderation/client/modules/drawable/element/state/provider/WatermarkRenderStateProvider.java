package me.yuugao.holymoderation.client.modules.drawable.element.state.provider;

import me.yuugao.holymoderation.client.config.manager.ConfigManager;
import me.yuugao.holymoderation.client.modules.drawable.element.state.WatermarkRenderState;
import me.yuugao.holymoderation.client.modules.drawable.render.RenderMode;
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