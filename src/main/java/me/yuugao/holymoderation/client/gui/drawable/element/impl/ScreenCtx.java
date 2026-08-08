package me.yuugao.holymoderation.client.gui.drawable.element.impl;

/**
 * Immutable snapshot of a click's screen geometry, passed to {@link DrawableElement#handleClick}.
 * Decouples hit-testing from rendering (no DrawContext needed for input).
 */
public record ScreenCtx(float screenWidth, float screenHeight, double mouseX, double mouseY) {
}
