package me.yuugao.holymoderation.client.gui.drawable.element.state.provider.impl;

import me.yuugao.holymoderation.client.gui.drawable.element.state.impl.CheckoutsRenderState;
import me.yuugao.holymoderation.client.gui.drawable.element.state.provider.RenderStateProvider;
import me.yuugao.holymoderation.client.gui.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.MinecraftService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.StateService;

import net.minecraft.client.network.ClientPlayerEntity;

import org.apache.commons.lang3.StringUtils;

public class CheckoutsRenderStateProvider extends RenderStateProvider<CheckoutsRenderState> {
    public CheckoutsRenderStateProvider(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Override
    public CheckoutsRenderState getState(RenderMode mode) {
        StateService stateService = serviceContext.getStateService();
        MinecraftService minecraftService = serviceContext.getMinecraftService();

        ClientPlayerEntity player = minecraftService.getPlayer();

        return switch (mode) {
            case LIVE -> new CheckoutsRenderState(stateService.getCheckoutPlayer());
            case CONFIG -> new CheckoutsRenderState(!stateService.getCheckoutPlayer().isEmpty() ?
                    stateService.getCheckoutPlayer() : player != null ? player.getName().getString() : StringUtils.EMPTY);
        };
    }
}