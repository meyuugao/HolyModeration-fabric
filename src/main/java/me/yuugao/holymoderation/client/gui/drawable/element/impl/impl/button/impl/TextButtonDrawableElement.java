package me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button.impl;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button.ButtonAction;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button.ButtonDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.service.AnimationService;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.Render2DService;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import org.joml.Matrix3x2fStack;

import java.awt.Color;

public class TextButtonDrawableElement extends ButtonDrawableElement {
    private final Render2DService render2DService;
    private final MinecraftService minecraftService;
    private final Text text;

    private final float verticalPadding = 6f;

    private TextRenderer tr;

    public TextButtonDrawableElement(AnimationService animationService, Render2DService render2DService,
                                     MinecraftService minecraftService, PivotMode pivotMode,
                                     ButtonAction action, boolean enabled, Text text) {
        super(animationService, pivotMode, action, enabled);

        this.text = text;
        this.render2DService = render2DService;
        this.minecraftService = minecraftService;
    }

    @Override
    protected void render(DrawContext ctx, int z) {
        Matrix3x2fStack ms = ctx.getMatrices();

        int textWidth = tr.getWidth(text);
        int textHeight = tr.fontHeight;

        float textPadding = 8f;
        float idealWidth = textWidth + textPadding * 2;
        float idealHeight = textHeight + verticalPadding * 2;

        float scaleX = getWidth() / idealWidth;
        float scale = Math.min(scaleX, 1.0f);

        float scaledWidth = idealWidth * scale;
        float scaledHeight = idealHeight * scale;

        float offsetX = (getWidth() - scaledWidth) / 2f;
        float offsetY = (getHeight() - scaledHeight) / 2f;

        render2DService.setupRender();

        ms.pushMatrix();

        ms.translate(offsetX, offsetY);
        ms.scale(scale, scale);

        render2DService.renderSoftRoundedRectOutline(ctx, 0f, 0f, idealWidth, idealHeight, z,
                radius, buttonColor, outlineColor, outlineWidth, blurWidth);

        float textX = (idealWidth - textWidth) / 2f;
        float textY = (idealHeight - textHeight) / 2f;
        render2DService.renderText(tr, text.asOrderedText(), (int) textX, (int) textY, z, readableOn(buttonColor), false, ctx);

        ms.popMatrix();

        render2DService.endRender();
    }

    public void updateRenderForParent(DrawContext ctx, float relX, float relY, float width,
                                      float parW, float parH, int z, float radius,
                                      Color buttonColor, Color outlineColor, float outlineWidth, float blurWidth) {
        this.tr = minecraftService.getClient().textRenderer;
        setRelativePos(relX, relY);

        this.width = width;

        int textHeight = tr.fontHeight;
        this.height = textHeight + verticalPadding * 2;

        this.radius = radius;
        this.buttonColor = buttonColor;
        this.outlineColor = outlineColor;
        this.outlineWidth = outlineWidth;
        this.blurWidth = blurWidth;

        super.updateRenderForParent(ctx, parW, parH, z);
    }

    private static int readableOn(Color background) {
        double luminance = (0.299 * background.getRed() + 0.587 * background.getGreen() + 0.114 * background.getBlue()) / 255.0;
        return luminance > 0.6 ? 0xFF181A20 : 0xFFFFFFFF;
    }
}