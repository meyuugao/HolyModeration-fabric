package me.yuugao.holymoderation.client.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SchedulerService {
    private static ScheduledExecutorService instance;

    public static ScheduledExecutorService getInstance() {
        if (instance == null || instance.isShutdown()) {
            instance = Executors.newScheduledThreadPool(1);
        }

        return instance;
    }

    public static void shutdown() {
        if (instance != null) {
            instance.shutdown();
        }
    }
}