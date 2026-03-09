package me.yuugao.holymoderation.client.modules.drawable;

import me.yuugao.holymoderation.client.modules.Module;
import me.yuugao.holymoderation.client.modules.drawable.element.StatefulDrawableElement;
import me.yuugao.holymoderation.client.modules.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.GuiManagerService;

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
        float sw = ctx.getScaledWindowWidth();
        float sh = ctx.getScaledWindowHeight();
        float screenScale = Math.min(sw / 1920f, sh / 1080f);

        drawableElement.updateRenderForScreen(ctx, getRenderPriority(), mode);
    }
}