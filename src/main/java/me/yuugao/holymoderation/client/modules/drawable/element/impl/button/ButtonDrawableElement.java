package me.yuugao.holymoderation.client.modules.drawable.element.impl.button;

import me.yuugao.holymoderation.client.modules.drawable.element.DrawableElement;
import me.yuugao.holymoderation.client.modules.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.MinecraftService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.Render2DService;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.awt.Color;

public class ButtonDrawableElement extends DrawableElement {
    private final ButtonAction action;
    private final boolean enabled;
    private TextRenderer tr;
    private Text text;
    private float radius;
    private Color buttonColor;
    private Color outlineColor;
    private float outlineWidth;
    private float blurWidth;

    public ButtonDrawableElement(ServiceContext serviceContext, PivotMode pivotMode, ButtonAction action, boolean enabled) {
        super(serviceContext, pivotMode);
        this.action = action;
        this.enabled = enabled;
    }

    @Override
    protected void render(DrawContext ctx, int z) {
        Render2DService render2DService = serviceContext.getRender2DService();
        MatrixStack ms = ctx.getMatrices();

        int textWidth = tr.getWidth(text);
        int textHeight = tr.fontHeight;

        float buttonWidth = textWidth + 16f;
        float buttonHeight = textHeight + 12f;

        ms.push();

        ms.scale(getWidth() / buttonWidth, getHeight() / buttonHeight, 1f);

        render2DService.renderSoftRoundedRectOutline(ms, 0f, 0f,
                buttonWidth, buttonHeight, z, radius, buttonColor, outlineColor, outlineWidth, blurWidth);
        render2DService.renderText(tr, text.asOrderedText(),
                (int) (0 + (buttonWidth - textWidth) / 2f),
                (int) (0 + (buttonHeight - textHeight) / 2f),
                z,
                0xffffffff,
                false,
                ctx);

        ms.pop();
    }

    public void updateRenderForScreen(DrawContext ctx, Text text, float relX, float relY, float width, float height,
                                      int z, float radius, Color buttonColor, Color outlineColor, float outlineWidth, float blurWidth) {
        updateRender(text, relX, relY, width, height, radius, buttonColor, outlineColor, outlineWidth, blurWidth);
        float screenScale = Math.min(ctx.getScaledWindowWidth() / 960f, ctx.getScaledWindowHeight() / 540f);
        super.updateRenderForScreen(ctx, z, screenScale);
    }

    public void updateRenderForParent(DrawContext ctx, Text text, float relX, float relY, float width, float height,
                                      float parW, float parH, int z, float radius,
                                      Color buttonColor, Color outlineColor, float outlineWidth, float blurWidth) {
        updateRender(text, relX, relY, width, height, radius, buttonColor, outlineColor, outlineWidth, blurWidth);
        super.updateRenderForParent(ctx, parW, parH, z);
    }

    private void updateRender(Text text, float relX, float relY, float width, float height, float radius,
                              Color buttonColor, Color outlineColor, float outlineWidth, float blurWidth) {
        MinecraftService minecraftService = serviceContext.getMinecraftService();

        this.tr = minecraftService.getClient().textRenderer;
        this.text = text;
        setRelativePos(relX, relY);
        this.width = width;
        this.height = height;
        this.radius = radius;
        this.buttonColor = buttonColor;
        this.outlineColor = outlineColor;
        this.outlineWidth = outlineWidth;
        this.blurWidth = blurWidth;
    }
}