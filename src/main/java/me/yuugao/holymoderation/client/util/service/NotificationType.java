package me.yuugao.holymoderation.client.util.service;

import java.awt.Color;

import lombok.Getter;

@Getter
public enum NotificationType {
    SUCCESS(
            new Color(30, 60, 45, 210),
            new Color(80, 220, 150, 255),
            "success.wav"
    ),
    WARNING(
            new Color(70, 55, 25, 210),
            new Color(255, 200, 80, 255),
            "warning.wav"
    ),
    ERROR(
            new Color(65, 25, 25, 210),
            new Color(255, 90, 90, 255),
            "error.wav"
    ),
    EXCEPTION(
            new Color(45, 15, 25, 220),
            new Color(255, 40, 120, 255),
            "exception.wav"
    );

    private final Color bg;
    private final Color outline;
    private final String soundName;

    NotificationType(Color bg, Color outline, String soundName) {
        this.bg = bg;
        this.outline = outline;
        this.soundName = soundName;
    }
}