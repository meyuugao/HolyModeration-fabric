package me.yuugao.holymoderation.client.eventbus.event;

import net.minecraft.client.gui.DrawContext;

import lombok.Getter;

@Getter
public class RenderEvent extends Event {
    private final DrawContext drawContext;
    private final float tickDelta;

    public RenderEvent(DrawContext drawContext, float tickDelta) {
        this.drawContext = drawContext;
        this.tickDelta = tickDelta;
    }
}