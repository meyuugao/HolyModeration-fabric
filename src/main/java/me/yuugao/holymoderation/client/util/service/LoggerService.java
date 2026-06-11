package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.HolyModerationClient;
import me.yuugao.holymoderation.client.di.annotations.Singleton;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import lombok.Setter;

@Singleton
public class LoggerService {
    private final Logger logger;
    @Setter
    private Level level;

    public LoggerService() {
        this.logger = LogManager.getLogger(HolyModerationClient.class);
        this.level = Level.INFO;
    }

    public void exception(Object message) {
        if (level.intLevel() >= Level.ERROR.intLevel()) {
            logger.error("[HM EXCEPTION] {}", message);
        }
    }

    public void info(Object message) {
        if (level.intLevel() >= Level.INFO.intLevel()) {
            logger.info("[HM INFO] {}", message);
        }
    }

    public void debug(Object message) {
        if (level.intLevel() >= Level.DEBUG.intLevel()) {
            logger.warn("[HM DEBUG] {}", message);
        }
    }
}