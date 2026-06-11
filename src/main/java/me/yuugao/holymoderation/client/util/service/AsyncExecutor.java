package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AsyncExecutor {
    private final LoggerService loggerService;

    public CompletableFuture<Void> runAsync(String context, Runnable action) {
        return CompletableFuture.runAsync(() -> {
            try {
                action.run();
            } catch (Exception e) {
                loggerService.exception(
                        "Исключение в AsyncExecutor/runAsync (%s): %s"
                                .formatted(context, e)
                );
                throw new RuntimeException(e);
            }
        });
    }

    public <T> CompletableFuture<T> supplyAsync(String context, Supplier<T> action) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return action.get();
            } catch (Exception e) {
                loggerService.exception("Исключение в AsyncExecutor/supplyAsync (" + context + "): %s".formatted(e));
                throw new RuntimeException(e);
            }
        });
    }
}