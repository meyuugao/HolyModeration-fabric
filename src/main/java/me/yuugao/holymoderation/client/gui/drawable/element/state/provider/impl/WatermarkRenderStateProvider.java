package me.yuugao.holymoderation.client.gui.drawable.element.state.provider.impl;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.drawable.element.state.impl.WatermarkRenderState;
import me.yuugao.holymoderation.client.gui.drawable.element.state.provider.RenderStateProvider;
import me.yuugao.holymoderation.client.gui.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class WatermarkRenderStateProvider extends RenderStateProvider<WatermarkRenderState> {
    private final ConfigManagerService configManagerService;

    @Override
    public WatermarkRenderState getState(RenderMode mode) {
        return new WatermarkRenderState(switch (mode) {
            case LIVE -> configManagerService.getGuiConfig().isWatermarkEnabled() ? 1f : 0f;
            case CONFIG -> 1f;
        });
    }
}