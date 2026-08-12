package me.yuugao.holymoderation.client.gui.drawable.element.state.provider;

import me.yuugao.holymoderation.client.gui.drawable.element.state.RenderState;
import me.yuugao.holymoderation.client.gui.drawable.render.RenderMode;

public abstract class RenderStateProvider<T extends RenderState> {
    public abstract T getState(RenderMode mode);
}