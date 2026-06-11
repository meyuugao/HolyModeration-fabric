package me.yuugao.holymoderation.client.gui.drawable.element.state.provider.impl;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.drawable.element.state.impl.CheckoutsRenderState;
import me.yuugao.holymoderation.client.gui.drawable.element.state.provider.RenderStateProvider;
import me.yuugao.holymoderation.client.gui.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.state.PlayerStateService;

import net.minecraft.client.network.ClientPlayerEntity;

import org.apache.commons.lang3.StringUtils;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class CheckoutsRenderStateProvider extends RenderStateProvider<CheckoutsRenderState> {
    private final PlayerStateService playerStateService;
    private final MinecraftService minecraftService;

    @Override
    public CheckoutsRenderState getState(RenderMode mode) {
        ClientPlayerEntity player = minecraftService.getPlayer();

        return switch (mode) {
            case LIVE -> new CheckoutsRenderState(playerStateService.getCheckoutPlayer());
            case CONFIG -> new CheckoutsRenderState(!playerStateService.getCheckoutPlayer().isEmpty() ?
                    playerStateService.getCheckoutPlayer() : player != null ? player.getName().getString() : StringUtils.EMPTY);
        };
    }
}