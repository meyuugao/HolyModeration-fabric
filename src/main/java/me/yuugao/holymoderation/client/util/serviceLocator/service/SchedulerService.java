package me.yuugao.holymoderation.client.util.serviceLocator.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class SchedulerService extends Service {
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