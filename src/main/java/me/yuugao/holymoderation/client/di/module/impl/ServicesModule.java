package me.yuugao.holymoderation.client.di.module.impl;

import me.yuugao.holymoderation.client.di.DIContainer;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.di.module.DIModule;
import me.yuugao.holymoderation.client.util.service.*;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.eventbus.EventBusService;
import me.yuugao.holymoderation.client.util.service.state.ModStateService;
import me.yuugao.holymoderation.client.util.service.state.PlayerStateService;
import me.yuugao.holymoderation.client.util.service.state.UserStateService;

@Singleton
public class ServicesModule implements DIModule {
    @Override
    public void configure(DIContainer container) {
        container.register(LoggerService.class, LoggerService.class);
        container.register(ConfigManagerService.class, ConfigManagerService.class);
        container.register(EventBusService.class, EventBusService.class);
        container.register(MinecraftService.class, MinecraftService.class);
        container.register(AnimationService.class, AnimationService.class);
        container.register(SoundService.class, SoundService.class);
        container.register(Render2DService.class, Render2DService.class);
        container.register(NotificationsService.class, NotificationsService.class);
        container.register(ChatService.class, ChatService.class);
        container.register(ModStateService.class, ModStateService.class);
        container.register(PlayerStateService.class, PlayerStateService.class);
        container.register(UserStateService.class, UserStateService.class);
        container.register(SchedulerService.class, SchedulerService.class);
        container.register(SpyService.class, SpyService.class);
        container.register(CheckoutsService.class, CheckoutsService.class);
        container.register(GoogleSheetsService.class, GoogleSheetsService.class);
        container.register(GuiManagerService.class, GuiManagerService.class);
        container.register(InputService.class, InputService.class);
        container.register(NetService.class, NetService.class);
        container.register(PunishmentsService.class, PunishmentsService.class);
        container.register(ScreenHandlerService.class, ScreenHandlerService.class);
        container.register(ModuleManagerService.class, ModuleManagerService.class);
        container.register(AsyncExecutor.class, AsyncExecutor.class);
        container.register(ModBlackListService.class, ModBlackListService.class);
        container.register(UserValidationService.class, UserValidationService.class);
    }
}