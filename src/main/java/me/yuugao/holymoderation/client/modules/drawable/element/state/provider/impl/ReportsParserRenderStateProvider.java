package me.yuugao.holymoderation.client.modules.drawable.element.state.provider.impl;

import me.yuugao.holymoderation.client.modules.drawable.element.state.impl.ReportsParserRenderState;
import me.yuugao.holymoderation.client.modules.drawable.element.state.provider.RenderStateProvider;
import me.yuugao.holymoderation.client.modules.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.GenericContainerScreen;
import net.minecraft.client.network.ClientPlayerEntity;

public class ReportsParserRenderStateProvider extends RenderStateProvider<ReportsParserRenderState> {
    public ReportsParserRenderStateProvider(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Override
    public ReportsParserRenderState getState(RenderMode mode) {
        return switch (mode) {
            case LIVE -> {
                ClientPlayerEntity player = ServiceLocator.getMinecraftService().getPlayer();
                if (player != null) {
                    Screen screen = ServiceLocator.getMinecraftService().getClient().currentScreen;
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