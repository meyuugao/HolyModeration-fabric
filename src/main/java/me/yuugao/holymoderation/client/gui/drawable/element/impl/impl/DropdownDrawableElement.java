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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class DropdownDrawableElement extends DrawableElement {
    private static final float GAP = 4f;
    private static final float OPTION_HEIGHT = 18f;
    private static final float MAX_VISIBLE = 6f;
    private static final float SCROLL_SPEED = 14f;
    private static final float H_PADDING = 8f;

    private final Render2DService render2DService;
    private final MinecraftService minecraftService;
    private final Consumer<String> onChange;

    private TextRenderer tr;

    private final List<String> options = new ArrayList<>();
    private int selectedIndex = -1;
    private boolean expanded = false;
    private float scrollOffset = 0f;
    private float maxScroll = 0f;
    private int hoveredIndex = -1;

    private float radius;
    private float outlineWidth;
    private float blurWidth;

    private Color fieldColor;
    private Color outlineColor;
    private Color optionColor;
    private Color optionHoverColor;
    private Color optionSelectedColor;
    private Color textColor;
    private Color arrowColor;

    public DropdownDrawableElement(AnimationService animationService, Render2DService render2DService,
                                   MinecraftService minecraftService, Consumer<String> onChange) {
        super(animationService, PivotMode.LEFT_UP);

        this.render2DService = render2DService;
        this.minecraftService = minecraftService;
        this.onChange = onChange;
    }

    public void setOptions(List<String> options) {
        this.options.clear();
        if (options != null) this.options.addAll(options);
        if (selectedIndex >= this.options.size()) selectedIndex = this.options.size() - 1;
        scrollOffset = 0f;
        expanded = false;
    }

    public String getSelected() {
        return selectedIndex >= 0 && selectedIndex < options.size() ? options.get(selectedIndex) : null;
    }

    public void select(int index) {
        if (index < 0 || index >= options.size()) return;
        selectedIndex = index;
        if (onChange != null) onChange.accept(options.get(index));
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        if (expanded) scrollOffset = 0f;
    }

    @Override
    protected void render(DrawContext ctx, int z) {
        renderField(ctx, z);

        if (expanded) {
            renderOptions(ctx, z);
        }
    }

    private void renderField(DrawContext ctx, int z) {
        render2DService.renderSoftRoundedRectOutline(ctx, 0f, 0f, getWidth(), getHeight(), z,
                radius, fieldColor, outlineColor, outlineWidth, blurWidth);

        String selected = getSelected();
        float textY = (getHeight() - tr.fontHeight) / 2f;

        if (selected != null) {
            render2DService.renderText(tr, Text.literal(selected).asOrderedText(), (int) H_PADDING, (int) textY, z,
                    textColor.getRGB(), false, ctx);
        }

        renderArrow(ctx, ctx.getMatrices(), z, expanded);
    }

    private void renderArrow(DrawContext ctx, Matrix3x2fStack ms, int z, boolean up) {
        float cx = getWidth() - H_PADDING - 5f;
        float cy = getHeight() / 2f;
        float spread = 4f;
        float drop = up ? -3f : 3f;
        float stroke = Math.max(1.5f, getHeight() * 0.08f);

        renderArrowSegment(ctx, ms, z, cx - spread, cy - drop, cx, cy + drop, stroke);
        renderArrowSegment(ctx, ms, z, cx, cy + drop, cx + spread, cy - drop, stroke);
    }

    private void renderArrowSegment(DrawContext ctx, Matrix3x2fStack ms, int z,
                                    float x1, float y1, float x2, float y2, float stroke) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        float angle = (float) Math.atan2(dy, dx);
        float midX = (x1 + x2) / 2f;
        float midY = (y1 + y2) / 2f;

        ms.pushMatrix();
        ms.rotateAbout(angle, midX, midY);
        render2DService.renderSoftRoundedRect(ctx, midX - len / 2f, midY - stroke / 2f, len, stroke, z,
                stroke / 2f, arrowColor, 0);
        ms.popMatrix();
    }

    private void renderOptions(DrawContext ctx, int z) {
        float listTop = getHeight() + GAP;
        float listHeight = Math.min(options.size(), MAX_VISIBLE) * OPTION_HEIGHT;
        maxScroll = Math.max(0f, options.size() * OPTION_HEIGHT - listHeight);

        Matrix3x2fStack ms = ctx.getMatrices();
        float[] a = transformPoint(ms, 0f, listTop);
        float[] b = transformPoint(ms, getWidth(), listTop + listHeight);
        ctx.enableScissor((int) a[0], (int) a[1], (int) Math.ceil(b[0]), (int) Math.ceil(b[1]));

        render2DService.renderSoftRoundedRectOutline(ctx, 0f, listTop, getWidth(), listHeight, z,
                radius, optionColor, outlineColor, outlineWidth, blurWidth);

        int firstIdx = Math.max(0, (int) Math.floor(scrollOffset / OPTION_HEIGHT));
        int lastIdx = Math.min(options.size() - 1, (int) Math.ceil((scrollOffset + listHeight) / OPTION_HEIGHT));

        for (int i = firstIdx; i <= lastIdx; i++) {
            float y = listTop + i * OPTION_HEIGHT - scrollOffset;

            Color bg = i == selectedIndex ? optionSelectedColor : (i == hoveredIndex ? optionHoverColor : optionColor);
            render2DService.renderSoftRoundedRect(ctx, 1f, y + 1f, getWidth() - 2f, OPTION_HEIGHT - 2f, z,
                    radius, bg, 0);

            float textY = y + (OPTION_HEIGHT - tr.fontHeight) / 2f;
            render2DService.renderText(tr, Text.literal(options.get(i)).asOrderedText(),
                    (int) H_PADDING, (int) textY, z, textColor.getRGB(), false, ctx);
        }

        ctx.disableScissor();
    }

    public void updateRenderForParent(DrawContext ctx, float relX, float relY, float width, float height,
                                      float parW, float parH, int z, float radius,
                                      Color fieldColor, Color outlineColor, Color optionColor,
                                      Color optionHoverColor, Color optionSelectedColor,
                                      Color textColor, Color arrowColor, float outlineWidth, float blurWidth) {
        this.tr = minecraftService.getClient().textRenderer;
        setRelativePos(relX, relY);
        setWidth(width);
        setHeight(height);

        this.radius = radius;
        this.fieldColor = fieldColor;
        this.outlineColor = outlineColor;
        this.optionColor = optionColor;
        this.optionHoverColor = optionHoverColor;
        this.optionSelectedColor = optionSelectedColor;
        this.textColor = textColor;
        this.arrowColor = arrowColor;
        this.outlineWidth = outlineWidth;
        this.blurWidth = blurWidth;

        super.updateRenderForParent(ctx, parW, parH, z);
    }

    @Override
    public boolean handleClick(ScreenCtx screen) {
        return handleClick(screen.screenWidth(), screen.screenHeight(), screen.mouseX(), screen.mouseY());
    }

    public boolean handleClick(float parentW, float parentH, double mouseX, double mouseY) {
        if (!expanded) {
            if (isMouseOver(parentW, parentH, mouseX, mouseY)) {
                setExpanded(true);
                hoveredIndex = -1;
                return true;
            }
            return false;
        }

        if (isMouseOver(parentW, parentH, mouseX, mouseY)) {
            setExpanded(false);
            return true;
        }

        float[] local = screenToLocal(parentW, parentH, (float) mouseX, (float) mouseY);
        float listTop = getHeight() + GAP;
        float listHeight = Math.min(options.size(), MAX_VISIBLE) * OPTION_HEIGHT;
        if (local[0] >= 0f && local[0] <= getWidth() && local[1] >= listTop && local[1] <= listTop + listHeight) {
            int idx = (int) Math.floor((local[1] - listTop + scrollOffset) / OPTION_HEIGHT);
            if (idx >= 0 && idx < options.size()) {
                select(idx);
            }
            setExpanded(false);
            return true;
        }

        setExpanded(false);
        return false;
    }

    public void updateHovered(float parentW, float parentH, double mouseX, double mouseY) {
        if (!expanded) {
            hoveredIndex = -1;
            return;
        }

        float[] local = screenToLocal(parentW, parentH, (float) mouseX, (float) mouseY);
        float listTop = getHeight() + GAP;
        float listHeight = Math.min(options.size(), MAX_VISIBLE) * OPTION_HEIGHT;
        if (local[0] >= 0f && local[0] <= getWidth() && local[1] >= listTop && local[1] <= listTop + listHeight) {
            int idx = (int) Math.floor((local[1] - listTop + scrollOffset) / OPTION_HEIGHT);
            hoveredIndex = (idx >= 0 && idx < options.size()) ? idx : -1;
        } else {
            hoveredIndex = -1;
        }
    }

    public boolean handleScroll(float parentW, float parentH, double dy, double mouseX, double mouseY) {
        if (!expanded) return false;

        float[] local = screenToLocal(parentW, parentH, (float) mouseX, (float) mouseY);
        float listTop = getHeight() + GAP;
        float listHeight = Math.min(options.size(), MAX_VISIBLE) * OPTION_HEIGHT;
        if (local[0] < 0f || local[0] > getWidth() || local[1] < listTop || local[1] > listTop + listHeight) {
            return false;
        }

        scrollOffset -= (float) (dy * SCROLL_SPEED);
        scrollOffset = Math.max(0f, Math.min(scrollOffset, maxScroll));
        return true;
    }

    private static float[] transformPoint(Matrix3x2fStack ms, float x, float y) {
        return new float[]{
                ms.m00 * x + ms.m10 * y + ms.m20,
                ms.m01 * x + ms.m11 * y + ms.m21
        };
    }
}
