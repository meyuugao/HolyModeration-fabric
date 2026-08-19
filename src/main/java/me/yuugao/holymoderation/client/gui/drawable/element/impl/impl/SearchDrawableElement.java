package me.yuugao.holymoderation.client.gui.drawable.element.impl.impl;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.DrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.ScreenCtx;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.service.AnimationService;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.Render2DService;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import org.joml.Matrix3x2fStack;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.function.Consumer;

public class SearchDrawableElement extends DrawableElement {
    private static final float H_PADDING = 8f;
    private static final float CARET_WIDTH = 1.5f;

    private final Render2DService render2DService;
    private final MinecraftService minecraftService;
    private final Consumer<String> onChange;

    private TextRenderer tr;

    private String query = "";
    private String placeholder = "Поиск...";
    private int caretIndex = 0;
    private boolean focused = false;

    private float radius;
    private float outlineWidth;
    private float blurWidth;

    private Color fieldColor;
    private Color outlineColor;
    private Color textColor;
    private Color placeholderColor;
    private Color caretColor;

    public SearchDrawableElement(AnimationService animationService, Render2DService render2DService,
                                 MinecraftService minecraftService, Consumer<String> onChange) {
        super(animationService, PivotMode.LEFT_UP);

        this.render2DService = render2DService;
        this.minecraftService = minecraftService;
        this.onChange = onChange;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query == null ? "" : query;
        this.caretIndex = this.query.length();
        if (onChange != null) onChange.accept(this.query);
    }

    public void clear() {
        setQuery("");
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
        if (focused) caretIndex = query.length();
    }

    public boolean onCharTyped(char c) {
        if (!focused) return false;
        query = query.substring(0, caretIndex) + c + query.substring(caretIndex);
        caretIndex++;
        if (onChange != null) onChange.accept(query);
        return true;
    }

    public boolean onKeyPress(int key, int scancode, int action, int modifiers) {
        if (!focused) return false;
        if (action == GLFW.GLFW_RELEASE) return true;

        switch (key) {
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (caretIndex > 0) {
                    query = query.substring(0, caretIndex - 1) + query.substring(caretIndex);
                    caretIndex--;
                    if (onChange != null) onChange.accept(query);
                }
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (caretIndex < query.length()) {
                    query = query.substring(0, caretIndex) + query.substring(caretIndex + 1);
                    if (onChange != null) onChange.accept(query);
                }
            }
            case GLFW.GLFW_KEY_LEFT -> caretIndex = Math.max(0, caretIndex - 1);
            case GLFW.GLFW_KEY_RIGHT -> caretIndex = Math.min(query.length(), caretIndex + 1);
            case GLFW.GLFW_KEY_HOME -> caretIndex = 0;
            case GLFW.GLFW_KEY_END -> caretIndex = query.length();
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER, GLFW.GLFW_KEY_ESCAPE -> focused = false;
            default -> {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void render(DrawContext ctx, int z) {
        render2DService.renderSoftRoundedRectOutline(ctx, 0f, 0f, getWidth(), getHeight(), z,
                radius, fieldColor, outlineColor, outlineWidth, blurWidth);

        Matrix3x2fStack ms = ctx.getMatrices();
        float[] a = transformPoint(ms, H_PADDING - 2f, 0f);
        float[] b = transformPoint(ms, getWidth() - H_PADDING + 2f, getHeight());
        ctx.enableScissor((int) a[0], (int) a[1], (int) Math.ceil(b[0]), (int) Math.ceil(b[1]));

        float textY = (getHeight() - tr.fontHeight) / 2f;

        if (query.isEmpty()) {
            render2DService.renderText(tr, Text.literal(placeholder).asOrderedText(),
                    (int) H_PADDING, (int) textY, z, placeholderColor.getRGB(), false, ctx);
        } else {
            render2DService.renderText(tr, Text.literal(query).asOrderedText(),
                    (int) H_PADDING, (int) textY, z, textColor.getRGB(), false, ctx);
        }

        if (focused && (System.currentTimeMillis() / 500) % 2 == 0) {
            int caretX = (int) (H_PADDING + tr.getWidth(query.substring(0, caretIndex)));
            render2DService.renderRect(ctx, caretX, textY, CARET_WIDTH, tr.fontHeight, z, caretColor);
        }

        ctx.disableScissor();
    }

    public void updateRenderForParent(DrawContext ctx, float relX, float relY, float width, float height,
                                      float parW, float parH, int z, float radius,
                                      Color fieldColor, Color outlineColor, Color textColor,
                                      Color placeholderColor, Color caretColor, float outlineWidth, float blurWidth) {
        this.tr = minecraftService.getClient().textRenderer;
        setRelativePos(relX, relY);
        setWidth(width);
        setHeight(height);

        this.radius = radius;
        this.fieldColor = fieldColor;
        this.outlineColor = outlineColor;
        this.textColor = textColor;
        this.placeholderColor = placeholderColor;
        this.caretColor = caretColor;
        this.outlineWidth = outlineWidth;
        this.blurWidth = blurWidth;

        super.updateRenderForParent(ctx, parW, parH, z);
    }

    @Override
    public boolean handleClick(ScreenCtx screen) {
        return handleClick(screen.screenWidth(), screen.screenHeight(), screen.mouseX(), screen.mouseY());
    }

    public boolean handleClick(float parentW, float parentH, double mouseX, double mouseY) {
        boolean over = isMouseOver(parentW, parentH, mouseX, mouseY);
        setFocused(over);
        return over;
    }

    private static float[] transformPoint(Matrix3x2fStack ms, float x, float y) {
        return new float[]{
                ms.m00 * x + ms.m10 * y + ms.m20,
                ms.m01 * x + ms.m11 * y + ms.m21
        };
    }
}
