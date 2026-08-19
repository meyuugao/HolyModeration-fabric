package me.yuugao.holymoderation.client.gui.tabs;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.DrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.ScreenCtx;
import me.yuugao.holymoderation.client.gui.screen.GuiScreen;

import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class Tab<T extends GuiScreen> {
    protected final T parent;
    protected final LinkedHashMap<String, DrawableElement> drawableElements = new LinkedHashMap<>();

    public abstract void onRender(DrawContext ctx, int mouseX, int mouseY, float tickDelta);

    public boolean onMouseClick(float mouseX, float mouseY) {
        ArrayList<DrawableElement> list = new ArrayList<>(drawableElements.values());
        Collections.reverse(list);
        for (DrawableElement element : list) {
            if (element.handleClick(new ScreenCtx(parent.getWidth(), parent.getHeight(), mouseX, mouseY))) {
                return true;
            }
        }
        return false;
    }

    public void onMouseScroll(double dx, double dy, float mouseX, float mouseY) {
        for (DrawableElement element : drawableElements.values()) {
            element.onMouseScroll(dx, dy, (int) mouseX, (int) mouseY);
        }
    }

    public void onMouseDrag(float mouseX, float mouseY) {
    }

    public void onMouseRelease() {
    }

    public void onMouseMoved(float mouseX, float mouseY) {
    }

    public boolean onCharTyped(char chr) {
        return false;
    }

    public boolean onKeyPress(int key, int scancode, int action, int modifiers) {
        return false;
    }
}
