package me.yuugao.holymoderation.client.gui.tabs;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.DrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.ScreenCtx;
import me.yuugao.holymoderation.client.gui.screen.GuiScreen;
import me.yuugao.holymoderation.client.util.service.Render2DService;

import net.minecraft.client.gui.DrawContext;

import org.joml.Matrix3x2fStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class Tab<T extends GuiScreen> {
    protected final T parent;
    protected final LinkedHashMap<String, DrawableElement> drawableElements = new LinkedHashMap<>();
    protected final Render2DService render2DService;

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

    protected void pushScissor(DrawContext ctx, float x0, float y0, float x1, float y1) {
        Matrix3x2fStack ms = ctx.getMatrices();
        float[] a = transformPoint(ms, x0, y0);
        float[] b = transformPoint(ms, x1, y1);
        render2DService.pushScissor(a[0], a[1], b[0], b[1]);
    }

    protected static float[] transformPoint(Matrix3x2fStack ms, float x, float y) {
        return new float[]{
                ms.m00 * x + ms.m10 * y + ms.m20,
                ms.m01 * x + ms.m11 * y + ms.m21
        };
    }
}
