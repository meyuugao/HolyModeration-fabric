package me.yuugao.holymoderation.client.modules.drawable;

import me.yuugao.holymoderation.client.eventbus.event.HudRenderEvent;
import me.yuugao.holymoderation.client.modules.Module;
import me.yuugao.holymoderation.client.modules.drawable.element.DrawableElement;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import lombok.Getter;

@Getter
public abstract class DrawableModule<T extends DrawableElement> extends Module {
    protected final T drawableElement;

    public DrawableModule(ServiceContext serviceContext, T drawableElement) {
        super(serviceContext);
        this.drawableElement = drawableElement;
        serviceContext.getGuiManagerService().addDrawableModule(this);
    }

    public boolean isMouseOver(int mouseX, int mouseY) {
        float halfW = drawableElement.getWidth() / 2f, halfH = drawableElement.getHeight() / 2f;
        return mouseX >= drawableElement.getX() - halfW && mouseX <= drawableElement.getX()
                + halfW && mouseY >= drawableElement.getY() - halfH && mouseY <= drawableElement.getY() + halfH;
    }

    public abstract int getRenderPriority();

    public void render(HudRenderEvent event) {
        this.drawableElement.render(event.getDrawContext(), getRenderPriority());
    }
}