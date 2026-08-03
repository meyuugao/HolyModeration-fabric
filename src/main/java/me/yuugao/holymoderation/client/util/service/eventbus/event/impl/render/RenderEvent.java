package me.yuugao.holymoderation.client.util.service.eventbus.event.impl.render;

import me.yuugao.holymoderation.client.util.service.eventbus.event.Event;

import net.minecraft.client.gui.DrawContext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class RenderEvent extends Event {
    private final DrawContext drawContext;
    private final int mouseX;
    private final int mouseY;
    private final float tickDelta;
}