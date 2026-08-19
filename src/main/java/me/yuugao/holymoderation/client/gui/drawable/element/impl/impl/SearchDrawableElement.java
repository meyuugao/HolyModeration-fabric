package me.yuugao.holymoderation.client.gui.drawable.element.impl.impl;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.DrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.ScreenCtx;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.service.AnimationService;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.Render2DService;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
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
    private boolean centered = false;
    private int selStart = -1;
    private int selEnd = -1;

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

    public void setQuerySilent(String query) {
        this.query = query == null ? "" : query;
        this.caretIndex = this.query.length();
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder == null ? "" : placeholder;
    }

    public void setCentered(boolean centered) {
        this.centered = centered;
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
        if (hasSelection()) {
            deleteSelection();
        }
        query = query.substring(0, caretIndex) + c + query.substring(caretIndex);
        caretIndex++;
        if (onChange != null) onChange.accept(query);
        return true;
    }

    public boolean onKeyPress(int key, int scancode, int action, int modifiers) {
        if (!focused) return false;
        if (action == GLFW.GLFW_RELEASE) return true;

        boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        if (ctrl) {
            switch (key) {
                case GLFW.GLFW_KEY_C -> {
                    copySelection();
                    return true;
                }
                case GLFW.GLFW_KEY_V -> {
                    pasteAtCaret();
                    return true;
                }
                case GLFW.GLFW_KEY_A -> {
                    selStart = 0;
                    selEnd = query.length();
                    caretIndex = query.length();
                    return true;
                }
                default -> {
                }
            }
        }

        switch (key) {
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (hasSelection()) {
                    deleteSelection();
                } else if (caretIndex > 0) {
                    query = query.substring(0, caretIndex - 1) + query.substring(caretIndex);
                    caretIndex--;
                    if (onChange != null) onChange.accept(query);
                }
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (hasSelection()) {
                    deleteSelection();
                } else if (caretIndex < query.length()) {
                    query = query.substring(0, caretIndex) + query.substring(caretIndex + 1);
                    if (onChange != null) onChange.accept(query);
                }
            }
            case GLFW.GLFW_KEY_LEFT -> {
                clearSelection();
                caretIndex = Math.max(0, caretIndex - 1);
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                clearSelection();
                caretIndex = Math.min(query.length(), caretIndex + 1);
            }
            case GLFW.GLFW_KEY_HOME -> {
                clearSelection();
                caretIndex = 0;
            }
            case GLFW.GLFW_KEY_END -> {
                clearSelection();
                caretIndex = query.length();
            }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER, GLFW.GLFW_KEY_ESCAPE -> focused = false;
            default -> {
                return false;
            }
        }
        return true;
    }

    private boolean hasSelection() {
        return selStart >= 0 && selEnd > selStart;
    }

    private void clearSelection() {
        selStart = -1;
        selEnd = -1;
    }

    private void deleteSelection() {
        if (!hasSelection()) return;
        query = query.substring(0, selStart) + query.substring(selEnd);
        caretIndex = selStart;
        clearSelection();
        if (onChange != null) onChange.accept(query);
    }

    private void copySelection() {
        String text = hasSelection() ? query.substring(selStart, selEnd) : query;
        if (text.isEmpty()) return;
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
        } catch (Exception ignored) {
        }
    }

    private void pasteAtCaret() {
        try {
            Object data = Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
            if (!(data instanceof String text) || text.isEmpty()) return;
            if (hasSelection()) {
                deleteSelection();
            }
            query = query.substring(0, caretIndex) + text + query.substring(caretIndex);
            caretIndex += text.length();
            clearSelection();
            if (onChange != null) onChange.accept(query);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void render(DrawContext ctx, int z) {
        MatrixStack ms = ctx.getMatrices();

        render2DService.setupRender();

        render2DService.renderSoftRoundedRectOutline(ms, 0f, 0f, getWidth(), getHeight(), z,
                radius, fieldColor, outlineColor, outlineWidth, blurWidth);

        float[] a = transformPoint(ms, H_PADDING - 2f, 0f);
        float[] b = transformPoint(ms, getWidth() - H_PADDING + 2f, getHeight());
        ctx.enableScissor((int) a[0], (int) a[1], (int) Math.ceil(b[0]), (int) Math.ceil(b[1]));

        float textY = (getHeight() - tr.fontHeight) / 2f + 1f;
        float availW = getWidth() - H_PADDING * 2f;

        if (query.isEmpty()) {
            float px = H_PADDING;
            if (centered) {
                int tw = tr.getWidth(placeholder);
                px = Math.max(H_PADDING, H_PADDING + (availW - tw) / 2f);
            }
            render2DService.renderText(tr, Text.literal(placeholder).asOrderedText(),
                    (int) px, (int) textY, z, placeholderColor.getRGB(), false, ctx);
        } else {
            int qw = tr.getWidth(query);
            float qx = H_PADDING;
            if (centered) {
                qx = Math.max(H_PADDING, H_PADDING + (availW - qw) / 2f);
            }
            render2DService.renderText(tr, Text.literal(query).asOrderedText(),
                    (int) qx, (int) textY, z, textColor.getRGB(), false, ctx);

            if (focused && (System.currentTimeMillis() / 500) % 2 == 0) {
                int caretX = (int) (qx + tr.getWidth(query.substring(0, caretIndex)));
                render2DService.renderRect(ms, caretX, textY, CARET_WIDTH, tr.fontHeight, z, caretColor);
            }
        }

        ctx.disableScissor();

        render2DService.endRender();
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

    private static float[] transformPoint(MatrixStack ms, float x, float y) {
        Matrix4f m = ms.peek().getPositionMatrix();
        return new float[]{
                m.m00() * x + m.m10() * y + m.m20(),
                m.m01() * x + m.m11() * y + m.m21()
        };
    }
}
