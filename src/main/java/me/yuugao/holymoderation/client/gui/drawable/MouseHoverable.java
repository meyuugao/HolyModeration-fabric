package me.yuugao.holymoderation.client.gui.drawable;

import net.minecraft.client.gui.DrawContext;

public interface MouseHoverable {
    boolean isMouseOver(float parentWidth, float parentHeight, double mouseX, double mouseY);

    default boolean isGlobalMouseOver(float parX, float parY, float parW, float parH, double mouseX, double mouseY) {
        return isMouseOver(parW, parH, mouseX, mouseY);
    }

    default boolean isGlobalMouseOver(DrawContext ctx, double mouseX, double mouseY) {
        return isMouseOver(ctx.getScaledWindowWidth(), ctx.getScaledWindowHeight(), mouseX, mouseY);
    }

    default boolean isRelMouseOver(float parW, float parH, double mouseX, double mouseY) {
        return isMouseOver(parW, parH, mouseX, mouseY);
    }
}