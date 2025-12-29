package me.yuugao.holymoderation.client.util.serviceLocator.service;

import org.slf4j.Logger;

public record LoggerService(Logger logger) {
    public void printException(Object message) {
        logger.error(String.valueOf(message));
    }
}