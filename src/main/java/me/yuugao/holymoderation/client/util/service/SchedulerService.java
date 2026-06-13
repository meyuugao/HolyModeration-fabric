package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;

import java.util.concurrent.*;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class SchedulerService {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final LoggerService loggerService;

    public ScheduledFuture<?> schedule(String context, Runnable task, long delay, TimeUnit unit) {
        return scheduler.schedule(() -> {
            try {
                task.run();
            } catch (Exception e) {
                loggerService.exception("Исключение в SchedulerService/schedule (%s): %s".formatted(context, e));
            }
        }, delay, unit);
    }

    public ScheduledFuture<?> scheduleAtFixedRate(String context, Runnable task, long initialDelay, long period, TimeUnit unit) {
        return scheduler.scheduleAtFixedRate(() -> {
            try {
                task.run();
            } catch (Exception e) {
                loggerService.exception("Исключение в SchedulerService/scheduleAtFixedRate (%s): %s".formatted(context, e));
            }
        }, initialDelay, period, unit);
    }

    public void submit(String context, Runnable task) {
        scheduler.submit(() -> {
            try {
                task.run();
            } catch (Exception e) {
                loggerService.exception("Исключение в SchedulerService/submit (%s): %s".formatted(context, e));
            }
        });
    }
}