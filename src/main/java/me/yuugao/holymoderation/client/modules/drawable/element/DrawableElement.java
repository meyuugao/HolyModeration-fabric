package me.yuugao.holymoderation.client.modules.drawable.element;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

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

    protected abstract void renderContent(DrawContext ctx, int z);

    public final void render(DrawContext ctx, int z) {
        MatrixStack ms = ctx.getMatrices();
        ms.push();
        ms.translate(x, y, 0f);
        try {
            renderContent(ctx, z);
        } finally {
            ms.pop();
        }
    }
}