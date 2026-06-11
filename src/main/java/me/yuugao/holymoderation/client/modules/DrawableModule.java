package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.StatefulDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.render.RenderMode;

import net.minecraft.client.gui.DrawContext;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public abstract class DrawableModule<T extends StatefulDrawableElement<?>> {
    protected final T drawableElement;

    public abstract int getRenderPriority();

    public void render(DrawContext ctx, RenderMode mode) {
        drawableElement.updateRenderForScreen(ctx, getRenderPriority(), mode);
    }
}