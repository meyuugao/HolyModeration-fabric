package me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button.impl;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button.ButtonAction;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button.ButtonDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.MinecraftService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.Render2DService;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.awt.Color;

import lombok.Getter;
import lombok.Setter;

public class TextButtonDrawableElement extends ButtonDrawableElement {
    private final float verticalPadding = 6f;
    private TextRenderer tr;
    @Getter
    @Setter
    private Text text;

    public TextButtonDrawableElement(ServiceContext serviceContext, PivotMode pivotMode, ButtonAction action, boolean enabled, Text text) {
        super(serviceContext, pivotMode, action, enabled);
        this.text = text;
    }

    @Override
    protected void render(DrawContext ctx, int z) {
        Render2DService render2DService = serviceContext.getRender2DService();
        MatrixStack ms = ctx.getMatrices();

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

        ms.push();

        ms.translate(offsetX, offsetY, 0);
        ms.scale(scale, scale, 1f);

        render2DService.renderSoftRoundedRectOutline(ms, 0f, 0f, idealWidth, idealHeight, z,
                radius, buttonColor, outlineColor, outlineWidth, blurWidth);

        float textX = (idealWidth - textWidth) / 2f;
        float textY = (idealHeight - textHeight) / 2f;
        render2DService.renderText(tr, text.asOrderedText(), (int) textX, (int) textY, z, 0xffffffff, false, ctx);

        ms.pop();
    }

    public void updateRenderForParent(DrawContext ctx, float relX, float relY, float width,
                                      float parW, float parH, int z, float radius,
                                      Color buttonColor, Color outlineColor, float outlineWidth, float blurWidth) {
        MinecraftService minecraftService = serviceContext.getMinecraftService();

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
}