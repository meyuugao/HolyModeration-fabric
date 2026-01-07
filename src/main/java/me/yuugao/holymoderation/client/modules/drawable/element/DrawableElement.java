package me.yuugao.holymoderation.client.modules.drawable.element;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import net.minecraft.client.gui.DrawContext;

import lombok.Getter;
import lombok.Setter;

public abstract class DrawableElement {
    protected final ServiceContext serviceContext;
    @Getter @Setter
    protected float x, y;
    @Getter
    protected float width, height;
    @Getter @Setter
    protected float widthScale = 1, heightScale = 1;

    public DrawableElement(ServiceContext serviceContext) {
        this.serviceContext = serviceContext;
    }

    abstract public void render(DrawContext ctx);
}