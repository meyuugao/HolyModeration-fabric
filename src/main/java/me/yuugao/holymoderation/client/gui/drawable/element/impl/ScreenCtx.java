package me.yuugao.holymoderation.client.gui.drawable.element.impl;

/**
 * Immutable snapshot of a click's screen geometry, passed to {@link DrawableElement#handleClick}.
 * Decouples hit-testing from rendering (no DrawContext needed for input).
 */
public final class ScreenCtx {
    private final float screenWidth;
    private final float screenHeight;
    private final double mouseX;
    private final double mouseY;

    public ScreenCtx(float screenWidth, float screenHeight, double mouseX, double mouseY) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    public float screenWidth() {
        return screenWidth;
    }

    public float screenHeight() {
        return screenHeight;
    }

    public double mouseX() {
        return mouseX;
    }

    public double mouseY() {
        return mouseY;
    }
}
