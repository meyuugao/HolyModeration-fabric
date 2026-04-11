package me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.DrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import java.awt.Color;

public abstract class ButtonDrawableElement extends DrawableElement {
    protected final ButtonAction action;
    protected final boolean enabled;
    protected float radius;
    protected Color buttonColor;
    protected Color outlineColor;
    protected float outlineWidth;
    protected float blurWidth;

    public ButtonDrawableElement(ServiceContext serviceContext, PivotMode pivotMode, ButtonAction action, boolean enabled) {
        super(serviceContext, pivotMode);
        this.action = action;
        this.enabled = enabled;
    }
}