package me.yuugao.holymoderation.client.util.serviceLocator.service;

import static me.yuugao.holymoderation.client.util.Colors.*;


import org.slf4j.Logger;

import lombok.Getter;

public class LoggerService {
    @Getter
    private final Logger logger;

    private final ChatService chatService;
    private final SoundService soundService;

    public LoggerService(Logger logger, ChatService chatService, SoundService soundService) {
        this.logger = logger;
        this.chatService = chatService;
        this.soundService = soundService;
    }

    public void printException(Object message) {
        logger.error(String.valueOf(message));
        chatService.clientMessage(RED + BOLD + message);
        soundService.playSound("exception.wav", 70);
    }

    public void printError(Object message) {
        chatService.clientMessage(RED + BOLD + message);
        soundService.playSound("error.wav", 70);
    }

    public void printSuccess(Object message) {
        chatService.clientMessage(AQUA + BOLD + message);
        soundService.playSound("success.wav", 70);
    }
}