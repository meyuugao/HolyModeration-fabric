package me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button.impl;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button.ButtonAction;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button.ButtonDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.LoggerService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.MinecraftService;
import me.yuugao.holymoderation.client.util.serviceLocator.service.impl.Render2DService;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import java.awt.Color;
import java.io.IOException;
import java.util.Optional;

public class ImageButtonDrawableElement extends ButtonDrawableElement {
    private Identifier image;
    private float imageWidth;
    private float imageHeight;
    private float padding;
    private float scale;

    public ImageButtonDrawableElement(ServiceContext serviceContext, PivotMode pivotMode, ButtonAction action, boolean enabled) {
        super(serviceContext, pivotMode, action, enabled);
    }

    @Override
    protected void render(DrawContext ctx, int z) {
        Render2DService render2DService = serviceContext.getRender2DService();
        MatrixStack ms = ctx.getMatrices();

        float scaledImgW = imageWidth * scale;
        float scaledImgH = imageHeight * scale;

        float buttonW = scaledImgW + padding;
        float buttonH = scaledImgH + padding;

        float imgX = (buttonW - scaledImgW) / 2f;
        float imgY = (buttonH - scaledImgH) / 2f;

        ms.push();

        render2DService.renderSoftRoundedRectOutline(ms, 0f, 0f, buttonW, buttonH, z,
                radius, buttonColor, outlineColor, outlineWidth, blurWidth);

        ms.translate(imgX, imgY, 0);
        render2DService.renderImage(ctx, image, 0f, 0f, scaledImgW, scaledImgH, z);

        ms.pop();
    }

    public void updateRenderForScreen(DrawContext ctx, Identifier image, float relX, float relY, float padding, float scale,
                                      int z, float radius, Color buttonColor, Color outlineColor, float outlineWidth, float blurWidth) {
        updateRender(image, relX, relY, padding, scale, radius, buttonColor, outlineColor, outlineWidth, blurWidth);
        float screenScale = Math.min(ctx.getScaledWindowWidth() / 1280f, ctx.getScaledWindowHeight() / 720f);
        super.updateRenderForScreen(ctx, z, screenScale);
    }

    public void updateRenderForParent(DrawContext ctx, Identifier image, float relX, float relY, float padding, float scale,
                                      float parW, float parH, int z, float radius,
                                      Color buttonColor, Color outlineColor, float outlineWidth, float blurWidth) {
        updateRender(image, relX, relY, padding, scale, radius, buttonColor, outlineColor, outlineWidth, blurWidth);
        super.updateRenderForParent(ctx, parW, parH, z);
    }

    private void updateRender(Identifier image, float relX, float relY, float padding, float scale, float radius,
                              Color buttonColor, Color outlineColor, float outlineWidth, float blurWidth) {
        this.image = image;
        setRelativePos(relX, relY);
        this.padding = padding;
        this.scale = scale;

        int[] sizes = getImageSizes(image);
        this.imageWidth = sizes[0];
        this.imageHeight = sizes[1];

        this.width = (imageWidth * scale) + padding;
        this.height = (imageHeight * scale) + padding;

        this.radius = radius;
        this.buttonColor = buttonColor;
        this.outlineColor = outlineColor;
        this.outlineWidth = outlineWidth;
        this.blurWidth = blurWidth;
    }

    private int[] getImageSizes(Identifier texture) {
        MinecraftService minecraftService = serviceContext.getMinecraftService();
        LoggerService loggerService = serviceContext.getLoggerService();

        try {
            Optional<Resource> optional = minecraftService.getClient().getResourceManager().getResource(texture);
            if (optional.isPresent()) {
                Resource resource = optional.get();
                try (NativeImage image = NativeImage.read(resource.getInputStream())) {
                    int width = image.getWidth();
                    int height = image.getHeight();
                    return new int[]{width, height};
                }
            }
            return new int[]{0, 0};
        } catch (IOException e) {
            loggerService.exception("Исключение в ImageButtonDrawableElement/getImageSizes: %s".formatted(e));
            return new int[]{0, 0};
        }
    }
}