package me.yuugao.holymoderation.client.modules.drawable.element;

import me.yuugao.holymoderation.client.modules.drawable.element.state.RenderState;
import me.yuugao.holymoderation.client.modules.drawable.element.state.provider.RenderStateProvider;
import me.yuugao.holymoderation.client.modules.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.modules.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

public abstract class StatefulDrawableElement<T extends RenderState> extends Drawable {
    protected final RenderStateProvider<T> stateProvider;

    protected boolean positioned = false;

    protected StatefulDrawableElement(ServiceContext serviceContext, PivotMode positionMode,
                                      RenderStateProvider<T> stateProvider) {
        super(serviceContext, positionMode);
        this.stateProvider = stateProvider;
    }

    protected abstract void initPosition(DrawContext ctx);

    protected abstract void render(DrawContext ctx, int z, T state);

    public void updateRenderForParent(DrawContext ctx, float parentWidth, float parentHeight, int z, RenderMode mode) {
        if (!positioned) {
            initPosition(ctx);
            positioned = true;
        }

        T state = stateProvider.getState(mode);
        if (state == null) return;

        MatrixStack ms = ctx.getMatrices();

        ms.push();

        prepareMatrixForParent(ctx, parentWidth, parentHeight);

        render(ctx, z, state);

        ms.pop();
    }

    public void updateRenderForScreen(DrawContext ctx, int z, RenderMode mode) {
        if (!positioned) {
            initPosition(ctx);
            positioned = true;
        }

        T state = stateProvider.getState(mode);
        if (state == null) return;

        MatrixStack ms = ctx.getMatrices();

        ms.push();

        prepareMatrixForScreen(ctx);

        render(ctx, z, state);

        ms.pop();
    }
}