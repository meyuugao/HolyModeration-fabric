package me.yuugao.holymoderation.client.gui.drawable.element.impl;

import me.yuugao.holymoderation.client.gui.drawable.element.state.RenderState;
import me.yuugao.holymoderation.client.gui.drawable.element.state.provider.RenderStateProvider;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.gui.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.util.service.AnimationService;

import net.minecraft.client.gui.DrawContext;

public abstract class StatefulDrawableElement<T extends RenderState> extends DrawableElement {
    protected final RenderStateProvider<T> stateProvider;
    protected boolean positioned = false;

    private T activeState;

    protected StatefulDrawableElement(AnimationService animationService, PivotMode pivotMode, RenderStateProvider<T> stateProvider) {
        super(animationService, pivotMode);

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
        updateStatefulRender(ctx, mode);
        if (activeState == null) return;

        float screenScale = Math.min(ctx.getScaledWindowWidth() / 1280f, ctx.getScaledWindowHeight() / 720f);
        super.updateRenderForScreen(ctx, z, screenScale);

        this.activeState = null;
    }

    public void updateRenderForParent(DrawContext ctx, float parW, float parH, int z, RenderMode mode) {
        updateStatefulRender(ctx, mode);
        if (activeState == null) return;

        super.updateRenderForParent(ctx, parW, parH, z);

        this.activeState = null;
    }

    private void updateStatefulRender(DrawContext ctx, RenderMode mode) {
        if (!positioned) {
            initPosition(ctx);
            positioned = true;
        }

        this.activeState = stateProvider.getState(mode);
    }
}