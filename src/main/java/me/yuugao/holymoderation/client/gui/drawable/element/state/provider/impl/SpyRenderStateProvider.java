package me.yuugao.holymoderation.client.gui.drawable.element.state.provider.impl;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.drawable.element.state.impl.SpyRenderState;
import me.yuugao.holymoderation.client.gui.drawable.element.state.provider.RenderStateProvider;
import me.yuugao.holymoderation.client.gui.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.util.service.state.PlayerStateService;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class SpyRenderStateProvider extends RenderStateProvider<SpyRenderState> {
    private final PlayerStateService playerStateService;

    @Override
    public SpyRenderState getState(RenderMode mode) {
        return switch (mode) {
            case LIVE -> new SpyRenderState(getStringsToRender());
            case CONFIG ->
                    new SpyRenderState(Arrays.equals(getStringsToRender(), new String[]{StringUtils.EMPTY, StringUtils.EMPTY}) ?
                            new String[]{"Модуль слежки", "Режим настройки"} : getStringsToRender());
        };
    }

    private String @NotNull [] getStringsToRender() {
        String spyPlayer = playerStateService.getSpyPlayer();
        if (spyPlayer.isEmpty()) return new String[]{StringUtils.EMPTY, StringUtils.EMPTY};

        String spyPlayerStatus = playerStateService.getSpyPlayerStatus();
        String spyPlayerActivity = playerStateService.getSpyPlayerActivity();

        if (spyPlayerStatus.isEmpty())
            return new String[]{spyPlayer, StringUtils.EMPTY};

        return switch (spyPlayerStatus) {
            case "stop" -> new String[]{"Слежка приостановлена", StringUtils.EMPTY};
            case "offline" -> new String[]{"Игрок %s оффлайн".formatted(spyPlayer), StringUtils.EMPTY};
            case "lobby" -> new String[]{"Игрок %s в лобби".formatted(spyPlayer), StringUtils.EMPTY};
            default -> new String[]{
                    "Игрок %s находится на %s".formatted(spyPlayer, spyPlayerStatus),
                    spyPlayerActivity == null ? StringUtils.EMPTY : "Активность: %s".formatted(spyPlayerActivity)
            };
        };
    }
}