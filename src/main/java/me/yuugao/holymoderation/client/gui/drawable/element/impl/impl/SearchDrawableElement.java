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

import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.function.Consumer;

public class SearchDrawableElement extends DrawableElement {
    private static final float H_PADDING = 8f;
    private static final float CARET_WIDTH = 1.5f;
    private static final Color SELECTION_COLOR = new Color(255, 255, 255, 60);

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
    private int selAnchor = -1;
    private int maxLength = -1;

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
        clearSelection();
        if (onChange != null) onChange.accept(this.query);
    }

    public void setQuerySilent(String query) {
        this.query = query == null ? "" : query;
        this.caretIndex = this.query.length();
        clearSelection();
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder == null ? "" : placeholder;
    }

    public void setCentered(boolean centered) {
        this.centered = centered;
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
    }

    public void clear() {
        setQuery("");
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
        if (focused) {
            caretIndex = query.length();
            clearSelection();
            selAnchor = caretIndex;
        }
    }

    public boolean onCharTyped(char c) {
        if (!focused) return false;
        if (maxLength >= 0 && query.length() >= maxLength && !hasSelection()) return true;
        if (hasSelection()) {
            deleteSelection();
        }
        query = query.substring(0, caretIndex) + c + query.substring(caretIndex);
        caretIndex++;
        clearSelection();
        selAnchor = caretIndex;
        if (onChange != null) onChange.accept(query);
        return true;
    }

    public boolean onKeyPress(int key, int scancode, int action, int modifiers) {
        if (!focused) return false;
        if (action == GLFW.GLFW_RELEASE) return true;

        boolean ctrl = (modifiers & GLFW.GLFW_MOD_CONTROL) != 0;
        boolean shift = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;

        if (ctrl) {
            switch (key) {
                case GLFW.GLFW_KEY_C -> {
                    copySelection();
                    return true;
                }
                case GLFW.GLFW_KEY_X -> {
                    copySelection();
                    if (hasSelection()) deleteSelection();
                    return true;
                }
                case GLFW.GLFW_KEY_V -> {
                    pasteAtCaret();
                    return true;
                }
                case GLFW.GLFW_KEY_A -> {
                    selStart = 0;
                    selEnd = query.length();
                    selAnchor = 0;
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
                    selAnchor = caretIndex;
                    if (onChange != null) onChange.accept(query);
                }
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (hasSelection()) {
                    deleteSelection();
                } else if (caretIndex < query.length()) {
                    query = query.substring(0, caretIndex) + query.substring(caretIndex + 1);
                    selAnchor = caretIndex;
                    if (onChange != null) onChange.accept(query);
                }
            }
            case GLFW.GLFW_KEY_LEFT -> {
                int next = Math.max(0, caretIndex - 1);
                if (shift) {
                    caretIndex = next;
                    updateShiftSelection();
                } else {
                    clearSelection();
                    caretIndex = next;
                    selAnchor = caretIndex;
                }
            }
            case GLFW.GLFW_KEY_RIGHT -> {
                int next = Math.min(query.length(), caretIndex + 1);
                if (shift) {
                    caretIndex = next;
                    updateShiftSelection();
                } else {
                    clearSelection();
                    caretIndex = next;
                    selAnchor = caretIndex;
                }
            }
            case GLFW.GLFW_KEY_HOME -> {
                if (shift) {
                    caretIndex = 0;
                    updateShiftSelection();
                } else {
                    clearSelection();
                    caretIndex = 0;
                    selAnchor = 0;
                }
            }
            case GLFW.GLFW_KEY_END -> {
                if (shift) {
                    caretIndex = query.length();
                    updateShiftSelection();
                } else {
                    clearSelection();
                    caretIndex = query.length();
                    selAnchor = caretIndex;
                }
            }
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER, GLFW.GLFW_KEY_ESCAPE -> focused = false;
            default -> {
                return false;
            }
        }
        return true;
    }

    private void updateShiftSelection() {
        if (selAnchor < 0) {
            selAnchor = caretIndex;
            return;
        }
        int a = Math.min(selAnchor, caretIndex);
        int b = Math.max(selAnchor, caretIndex);
        if (a == b) {
            clearSelection();
        } else {
            selStart = a;
            selEnd = b;
        }
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
        selAnchor = caretIndex;
        if (onChange != null) onChange.accept(query);
    }

    private void copySelection() {
        String text = hasSelection() ? query.substring(selStart, selEnd) : query;
        if (text.isEmpty()) return;
        try {
            GLFW.glfwSetClipboardString(minecraftService.getClient().getWindow().getHandle(), text);
        } catch (Exception ignored) {
        }
    }

    private void pasteAtCaret() {
        try {
            String text = GLFW.glfwGetClipboardString(minecraftService.getClient().getWindow().getHandle());
            if (text == null || text.isEmpty()) return;
            if (hasSelection()) {
                deleteSelection();
            }
            int remaining = maxLength >= 0 ? maxLength - query.length() : Integer.MAX_VALUE;
            if (remaining <= 0) return;
            if (text.length() > remaining) {
                text = text.substring(0, remaining);
            }
            query = query.substring(0, caretIndex) + text + query.substring(caretIndex);
            caretIndex += text.length();
            clearSelection();
            selAnchor = caretIndex;
            if (onChange != null) onChange.accept(query);
        } catch (Exception ignored) {
        }
    }

    @Override
    protected void render(DrawContext ctx, int z) {
        render2DService.renderSoftRoundedRectOutline(ctx.getMatrices(), 0f, 0f, getWidth(), getHeight(), z,
                radius, fieldColor, outlineColor, outlineWidth, blurWidth);

        MatrixStack ms = ctx.getMatrices();
        float[] a = transformPoint(ms, 0f, 0f);
        float[] b = transformPoint(ms, getWidth(), getHeight());
        render2DService.pushScissor(a[0], a[1], b[0], b[1]);

        float textY = (getHeight() - tr.fontHeight) / 2f + 1f;
        float caretY = (getHeight() - tr.fontHeight) / 2f;

        if (query.isEmpty()) {
            float px = textStartX(placeholder);
            render2DService.renderText(tr, Text.literal(placeholder).asOrderedText(),
                    (int) px, (int) textY, z, placeholderColor.getRGB(), false, ctx);
        } else {
            float qx = textStartX(query);
            render2DService.renderText(tr, Text.literal(query).asOrderedText(),
                    (int) qx, (int) textY, z, textColor.getRGB(), false, ctx);
        }

        if (focused && hasSelection()) {
            float startX = textStartX(query);
            float sx = startX + tr.getWidth(query.substring(0, selStart));
            float ex = startX + tr.getWidth(query.substring(0, selEnd));
            render2DService.renderRect(ctx.getMatrices(), sx, caretY, ex - sx, tr.fontHeight, z, SELECTION_COLOR);
        }

        if (focused && (System.currentTimeMillis() / 500) % 2 == 0) {
            float startX = textStartX(query);
            float cx = startX + tr.getWidth(query.substring(0, caretIndex));
            render2DService.renderRect(ctx.getMatrices(), cx, caretY, CARET_WIDTH, tr.fontHeight, z, caretColor);
        }

        render2DService.popScissor();
    }

    private float textStartX(String renderedText) {
        float availW = getWidth() - H_PADDING * 2f;
        if (centered) {
            int tw = tr.getWidth(renderedText);
            return Math.max(H_PADDING, H_PADDING + (availW - tw) / 2f);
        }
        return H_PADDING;
    }

    private int caretFromX(float localX) {
        float rel = localX - textStartX(query);
        int best = 0;
        float bestD = Math.abs(rel);
        for (int i = 0; i <= query.length(); i++) {
            float w = tr.getWidth(query.substring(0, i));
            float d = Math.abs(w - rel);
            if (d < bestD) {
                bestD = d;
                best = i;
            }
        }
        return best;
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
        if (over) {
            float[] local = screenToLocal(parentW, parentH, (float) mouseX, (float) mouseY);
            caretIndex = caretFromX(local[0]);
            clearSelection();
            selAnchor = caretIndex;
        }
        return over;
    }

    public boolean handleDrag(float parentW, float parentH, double mouseX, double mouseY) {
        if (!focused) return false;
        float[] local = screenToLocal(parentW, parentH, (float) mouseX, (float) mouseY);
        caretIndex = caretFromX(local[0]);
        if (selAnchor >= 0) {
            int a = Math.min(selAnchor, caretIndex);
            int b = Math.max(selAnchor, caretIndex);
            if (a == b) {
                clearSelection();
            } else {
                selStart = a;
                selEnd = b;
            }
        }
        return true;
    }

    public void handleRelease() {
    }

    private static float[] transformPoint(MatrixStack ms, float x, float y) {
        Matrix4f m = ms.peek().getPositionMatrix();
        return new float[]{
                m.m00() * x + m.m10() * y + m.m20(),
                m.m01() * x + m.m11() * y + m.m21()
        };
    }
}
