package me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.DrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.service.AnimationService;

import java.awt.Color;

public abstract class ButtonDrawableElement extends DrawableElement {
    protected final ButtonAction action;

    protected final boolean enabled;

    protected Color buttonColor;
    protected Color outlineColor;

    protected float outlineWidth;
    protected float blurWidth;
    protected float radius;

    public ButtonDrawableElement(AnimationService animationService, PivotMode pivotMode, ButtonAction action, boolean enabled) {
        super(animationService, pivotMode);

        this.action = action;
        this.enabled = enabled;
    }
    public boolean hitInParent(float parentW, float parentH, double localX, double localY) {
        if (!enabled || action == null) return false;
        if (isMouseOver(parentW, parentH, localX, localY)) {
            action.execute();
            return true;
        }
        return false;
    }
}