package me.yuugao.holymoderation.client.modules.drawable.element;

import me.yuugao.holymoderation.client.modules.drawable.element.state.RenderState;
import me.yuugao.holymoderation.client.modules.drawable.element.state.provider.RenderStateProvider;
import me.yuugao.holymoderation.client.modules.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.modules.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import net.minecraft.client.gui.DrawContext;

public abstract class StatefulDrawableElement<T extends RenderState> extends DrawableElement {
    protected final RenderStateProvider<T> stateProvider;
    protected boolean positioned = false;

    private T activeState;

    protected StatefulDrawableElement(ServiceContext serviceContext, PivotMode positionMode, RenderStateProvider<T> stateProvider) {
        super(serviceContext, positionMode);
        this.stateProvider = stateProvider;
    }

    protected abstract void initPosition(DrawContext ctx);

    protected abstract void render(DrawContext ctx, int z, T state);

    @Override
    protected void render(DrawContext ctx, int z) {
        if (activeState != null) {
            render(ctx, z, activeState);
        }
    }

    public void updateRenderForScreen(DrawContext ctx, int z, RenderMode mode) {
        if (!positioned) {
            initPosition(ctx);
            positioned = true;
        }

        activeState = stateProvider.getState(mode);
        if (activeState == null) return;

        float screenScale = Math.min(ctx.getScaledWindowWidth() / 1920f, ctx.getScaledWindowHeight() / 1080f);
        super.updateRenderForScreen(ctx, z, screenScale);

        activeState = null;
    }

    public void updateRenderForParent(DrawContext ctx, float parW, float parH, int z, RenderMode mode) {
        if (!positioned) {
            initPosition(ctx);
            positioned = true;
        }

        activeState = stateProvider.getState(mode);
        if (activeState == null) return;

        super.updateRenderForParent(ctx, parW, parH, z);

        activeState = null;
    }
}