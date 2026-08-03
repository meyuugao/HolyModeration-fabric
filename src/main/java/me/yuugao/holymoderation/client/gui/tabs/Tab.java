package me.yuugao.holymoderation.client.gui.tabs;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.DrawableElement;
import me.yuugao.holymoderation.client.gui.screen.GuiScreen;

import net.minecraft.client.gui.DrawContext;

import java.util.HashMap;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class Tab<T extends GuiScreen> {
    protected final T parent;
    protected final HashMap<String, DrawableElement> drawableElements = new HashMap<>();

    public abstract void onRender(DrawContext ctx, int mouseX, int mouseY, float tickDelta);
}