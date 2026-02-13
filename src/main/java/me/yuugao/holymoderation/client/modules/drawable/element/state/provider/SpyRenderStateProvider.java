package me.yuugao.holymoderation.client.modules.drawable.element.state.provider;

import me.yuugao.holymoderation.client.modules.drawable.element.state.SpyRenderState;
import me.yuugao.holymoderation.client.modules.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.StateService;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

public class SpyRenderStateProvider extends RenderStateProvider<SpyRenderState> {
    public SpyRenderStateProvider(ServiceContext serviceContext) {
        super(serviceContext);
    }

    @Override
    public SpyRenderState getState(RenderMode mode) {
        return switch (mode) {
            case LIVE -> new SpyRenderState(getStringsToRender());
            case CONFIG -> new SpyRenderState(new String[]{"Модуль слежки", "Режим настройки"});
        };
    }

    private String @NotNull [] getStringsToRender() {
        StateService stateService = serviceContext.getStateService();

        String spyPlayer = stateService.getSpyPlayer();
        if (spyPlayer.isEmpty()) return new String[]{StringUtils.EMPTY, StringUtils.EMPTY};

        String spyPlayerStatus = stateService.getSpyPlayerStatus();
        String spyPlayerActivity = stateService.getSpyPlayerActivity();

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