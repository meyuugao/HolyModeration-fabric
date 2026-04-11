package me.yuugao.holymoderation.client.util.serviceLocator.service.impl;

import me.yuugao.holymoderation.client.util.serviceLocator.service.Service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import lombok.Getter;

@Getter
public class SchedulerService extends Service {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
}