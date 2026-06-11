package me.yuugao.holymoderation.client.gui.drawable.element.state.provider.impl;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.drawable.element.state.impl.ReportsParserRenderState;
import me.yuugao.holymoderation.client.gui.drawable.element.state.provider.RenderStateProvider;
import me.yuugao.holymoderation.client.gui.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.util.service.MinecraftService;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerEntity;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class ReportsParserRenderStateProvider extends RenderStateProvider<ReportsParserRenderState> {
    private final MinecraftService minecraftService;

    @Override
    public ReportsParserRenderState getState(RenderMode mode) {
        return switch (mode) {
            case LIVE -> {
                ClientPlayerEntity player = minecraftService.getPlayer();
                if (player != null) {
                    Screen screen = minecraftService.getClient().currentScreen;
                    if (screen instanceof GenericContainerScreen && screen.getTitle().getString().equals("Жалобы на игроков")) {
                        yield new ReportsParserRenderState(1f);
                    }
                }

                yield new ReportsParserRenderState(0f);
            }
            case CONFIG -> new ReportsParserRenderState(1f);
        };
    }
}