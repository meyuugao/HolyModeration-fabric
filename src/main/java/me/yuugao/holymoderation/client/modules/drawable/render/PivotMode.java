package me.yuugao.holymoderation.client.modules.drawable.render;

import lombok.Getter;

@Getter
public enum PivotMode {
    LEFT_UP(0f, 0f),
    LEFT_DOWN(0f, 1f),
    RIGHT_UP(1f, 0f),
    RIGHT_DOWN(1f, 1f),
    UP(0.5f, 0f),
    DOWN(0.5f, 1f),
    LEFT(0f, 0.5f),
    RIGHT(1f, 0.5f),
    CENTER(0.5f, 0.5f);

    private final float xFactor;
    private final float yFactor;

    PivotMode(float xFactor, float yFactor) {
        this.xFactor = xFactor;
        this.yFactor = yFactor;
    }
}