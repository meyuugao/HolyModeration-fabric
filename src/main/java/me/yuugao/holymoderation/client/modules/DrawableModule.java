package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.StatefulDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.GuiManagerService;

import net.minecraft.client.gui.DrawContext;

import lombok.Getter;

@Getter
public abstract class DrawableModule<T extends StatefulDrawableElement<?>> extends Module {
    protected final T drawableElement;

    public DrawableModule(ServiceContext serviceContext, T drawableElement) {
        super(serviceContext);
        GuiManagerService guiManagerService = serviceContext.getGuiManagerService();
        this.drawableElement = drawableElement;
        guiManagerService.addDrawableModule(this);
    }

    public abstract int getRenderPriority();

    public void render(DrawContext ctx, RenderMode mode) {
        drawableElement.updateRenderForScreen(ctx, getRenderPriority(), mode);
    }
}