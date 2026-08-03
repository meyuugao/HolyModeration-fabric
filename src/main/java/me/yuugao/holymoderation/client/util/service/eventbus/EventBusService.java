package me.yuugao.holymoderation.client.util.service.eventbus;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.service.LoggerService;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class EventBusService {
    private final LoggerService loggerService;
    @Getter
    private EventBus eventBus;

    public void initializeEventBus() {
        eventBus = new EventBus();
        eventBus.setLoggerService(loggerService);
        loggerService.info("EventBus has been initialized.");
    }
}