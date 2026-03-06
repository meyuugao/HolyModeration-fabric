package me.yuugao.holymoderation.client.modules.drawable;

import me.yuugao.holymoderation.client.modules.Module;
import me.yuugao.holymoderation.client.modules.drawable.element.DrawableElement;
import me.yuugao.holymoderation.client.modules.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.GuiManagerService;

import net.minecraft.client.gui.DrawContext;

import lombok.Getter;

@Getter
public abstract class DrawableModule<T extends DrawableElement<?>> extends Module {
    protected final T drawableElement;

    public DrawableModule(ServiceContext serviceContext, T drawableElement) {
        super(serviceContext);

        GuiManagerService guiManagerService = serviceContext.getGuiManagerService();

        this.drawableElement = drawableElement;
        guiManagerService.addDrawableModule(this);
    }

    public boolean isMouseOver(int mouseX, int mouseY, DrawContext ctx) {
        float[] pv = drawableElement.getScalePivot();

        float left = drawableElement.getX(ctx) - pv[0];
        float top = drawableElement.getY(ctx) - pv[1];

        float right = left + drawableElement.getScaledWidth();
        float bottom = top + drawableElement.getScaledHeight();

        return mouseX >= left && mouseX <= right && mouseY >= top && mouseY <= bottom;
    }

    public abstract int getRenderPriority();

    public void render(DrawContext ctx, RenderMode mode) {
        drawableElement.updateRender(ctx, getRenderPriority(), mode);
    }
}