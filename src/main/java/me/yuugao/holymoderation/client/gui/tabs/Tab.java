package me.yuugao.holymoderation.client.gui.tabs;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.DrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.ScreenCtx;
import me.yuugao.holymoderation.client.gui.screen.GuiScreen;
import me.yuugao.holymoderation.client.util.service.Render2DService;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import org.joml.Matrix4f;

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

    /**
     * Push a scissor region expressed in the CURRENT local coordinate system (i.e.
     * relative to whatever MatrixStack translations/scales are active). The helper
     * transforms the two corners through the matrix to screen-space GUI coordinates
     * before forwarding to Render2DService.pushScissor.
     *
     * Translation of a 2D point (x, y, 0, 1) through a column-major Matrix4f M is:
     *   newX = m00*x + m10*y + m30
     *   newY = m01*x + m11*y + m31
     * The earlier version of this helper used m20/m21 (z-translation row), which is
     * always 0 for 2D UI transforms and produced wrong screen coordinates — the
     * scissor ended up near (0,0) instead of at the actual element position.
     */
    protected void pushScissor(DrawContext ctx, float x0, float y0, float x1, float y1) {
        MatrixStack ms = ctx.getMatrices();
        float[] a = transformPoint(ms, x0, y0);
        float[] b = transformPoint(ms, x1, y1);
        render2DService.pushScissor(a[0], a[1], b[0], b[1]);
    }

    protected static float[] transformPoint(MatrixStack ms, float x, float y) {
        Matrix4f m = ms.peek().getPositionMatrix();
        return new float[]{
                m.m00() * x + m.m10() * y + m.m30(),
                m.m01() * x + m.m11() * y + m.m31()
        };
    }
}
