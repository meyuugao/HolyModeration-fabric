package me.yuugao.holymoderation.client.eventbus.event;

import net.minecraft.client.gui.DrawContext;

import lombok.Getter;

@Getter
public class HudRenderEvent extends Event {
    private final DrawContext drawContext;
    private final float tickDelta;

    public HudRenderEvent(DrawContext drawContext, float tickDelta) {
        this.drawContext = drawContext;
        this.tickDelta = tickDelta;
    }
}