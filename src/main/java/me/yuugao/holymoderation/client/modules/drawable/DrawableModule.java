package me.yuugao.holymoderation.client.modules.drawable;

import me.yuugao.holymoderation.client.modules.Module;
import me.yuugao.holymoderation.client.modules.drawable.element.DrawableElement;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import net.minecraft.client.gui.DrawContext;

import lombok.Getter;

@Getter
public abstract class DrawableModule<T extends DrawableElement> extends Module {
    protected final T drawableElement;

    public DrawableModule(ServiceContext serviceContext, T drawableElement) {
        super(serviceContext);
        this.drawableElement = drawableElement;
        serviceContext.getGuiManagerService().addDrawableModule(this);
    }

    public boolean isMouseOver(int mouseX, int mouseY, DrawContext ctx) {
        float[] tl = drawableElement.topLeftLocal();

        float x = drawableElement.getX(ctx);
        float y = drawableElement.getY(ctx);

        float left = x + tl[0];
        float top = y + tl[1];
        float right = left + drawableElement.getWidth() * drawableElement.getWidthScale();
        float bottom = top + drawableElement.getHeight() * drawableElement.getHeightScale();

        return mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom;
    }

    public abstract int getRenderPriority();

    public void render(DrawContext ctx) {
        drawableElement.render(ctx, getRenderPriority());
    }
}