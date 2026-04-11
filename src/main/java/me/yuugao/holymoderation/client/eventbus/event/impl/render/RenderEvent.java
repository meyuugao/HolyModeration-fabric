package me.yuugao.holymoderation.client.eventbus.event.impl.render;

import me.yuugao.holymoderation.client.eventbus.event.Event;

import net.minecraft.client.gui.DrawContext;

import lombok.Getter;

@Getter
public class RenderEvent extends Event {
    private final DrawContext drawContext;
    private final int mouseX, mouseY;
    private final float tickDelta;

    public RenderEvent(DrawContext drawContext, int mouseX, int mouseY, float tickDelta) {
        this.drawContext = drawContext;
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        this.tickDelta = tickDelta;
    }
}