package me.yuugao.holymoderation.client.util.serviceLocator.service;

import java.awt.Color;

public enum NotificationType {
    SUCCESS(
            new Color(30, 60, 45, 210),
            new Color(80, 220, 150, 255)
    ),
    WARNING(
            new Color(70, 55, 25, 210),
            new Color(255, 200, 80, 255)
    ),
    ERROR(
            new Color(65, 25, 25, 210),
            new Color(255, 90, 90, 255)
    ),
    EXCEPTION(
            new Color(45, 15, 25, 220),
            new Color(255, 40, 120, 255)
    );

    private final Color bg;
    private final Color outline;

    NotificationType(Color bg, Color outline) {
        this.bg = bg;
        this.outline = outline;
    }

    public Color bg() {
        return bg;
    }

    public Color outline() {
        return outline;
    }
}