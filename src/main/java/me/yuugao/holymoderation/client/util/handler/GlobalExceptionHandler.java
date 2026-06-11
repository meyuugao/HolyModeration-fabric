package me.yuugao.holymoderation.client.util.handler;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.service.LoggerService;

@Singleton
public class GlobalExceptionHandler {
    private final LoggerService logger;

    @Inject
    public GlobalExceptionHandler(LoggerService logger) {
        this.logger = logger;
    }

    public void register() {
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) ->
                logger.exception("Uncaught exception in thread " + thread.getName() + ": " + throwable)
        );
    }
}