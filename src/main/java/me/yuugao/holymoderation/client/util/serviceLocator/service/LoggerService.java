package me.yuugao.holymoderation.client.util.serviceLocator.service;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Logger;

import lombok.Setter;

public class LoggerService {
    private final Logger logger;
    @Setter
    private Level level;

    public LoggerService(Logger logger) {
        this.logger = logger;
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