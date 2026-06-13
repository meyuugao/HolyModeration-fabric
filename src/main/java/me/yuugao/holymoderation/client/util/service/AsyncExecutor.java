package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;

@Singleton
@RequiredArgsConstructor(onConstructor_ = @Inject)
public class AsyncExecutor {
    private final LoggerService loggerService;
    private final Executor executor = Executors.newCachedThreadPool();

    public CompletableFuture<Void> runAsync(String context, Runnable action) {
        return CompletableFuture.runAsync(action, executor).exceptionally(e -> {
            loggerService.exception(
                    "Исключение в AsyncExecutor/runAsync (%s): %s"
                            .formatted(context, e)
            );
            throw new RuntimeException(e);
        });
    }

    public <T> CompletableFuture<T> supplyAsync(String context, Supplier<T> action) {
        return CompletableFuture.supplyAsync(action, executor).exceptionally(e -> {
            loggerService.exception(
                    "Исключение в AsyncExecutor/supplyAsync (%s): %s"
                            .formatted(context, e)
            );
            throw new RuntimeException(e);
        });
    }
}