package me.yuugao.holymoderation.client.modules.drawable.element.state.provider;

import me.yuugao.holymoderation.client.modules.drawable.element.state.CheckoutsRenderState;
import me.yuugao.holymoderation.client.modules.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.MinecraftService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.StateService;

import net.minecraft.client.network.ClientPlayerEntity;

import org.apache.commons.lang3.StringUtils;

public class CheckoutsRenderStateProvider extends RenderStateProvider<CheckoutsRenderState> {
    public CheckoutsRenderStateProvider(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Override
    public CheckoutsRenderState getState(RenderMode mode) {
        StateService stateService = serviceContext.getStateService();

        return switch (mode) {
            case LIVE -> new CheckoutsRenderState(stateService.getCheckoutPlayer());
            case CONFIG -> buildConfigState();
        };
    }

    private CheckoutsRenderState buildConfigState() {
        MinecraftService minecraftService = serviceContext.getMinecraftService();

        ClientPlayerEntity player = minecraftService.getPlayer();

        return new CheckoutsRenderState(player != null ? player.getName().getString() : StringUtils.EMPTY);
    }
}