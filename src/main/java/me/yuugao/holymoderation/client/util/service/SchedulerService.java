package me.yuugao.holymoderation.client.util.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SchedulerService {
    private ScheduledExecutorService instance;

    public ScheduledExecutorService getInstance() {
        if (instance == null || instance.isShutdown()) {
            instance = Executors.newScheduledThreadPool(1);
        }

        return instance;
    }

    public void shutdown() {
        if (instance != null) {
            instance.shutdown();
        }
    }
}