package me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button.impl;

import static me.yuugao.holymoderation.client.util.Colors.BOLD;
import static me.yuugao.holymoderation.client.util.Colors.DARK_RED;


import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button.ButtonAction;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button.ButtonDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.service.*;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import org.joml.Matrix3x2fStack;

import java.awt.Color;
import java.io.IOException;
import java.util.Optional;

public class ImageButtonDrawableElement extends ButtonDrawableElement {
    private final Render2DService render2DService;
    private final MinecraftService minecraftService;
    private final NotificationsService notificationsService;

    private Identifier image;

    private float imageHeight;
    private float padding;
    private float scale;
    private float imageWidth;

    public ImageButtonDrawableElement(AnimationService animationService, Render2DService render2DService,
                                      MinecraftService minecraftService, NotificationsService notificationsService,
                                      PivotMode pivotMode, ButtonAction action, boolean enabled) {
        super(animationService, pivotMode, action, enabled);

        this.render2DService = render2DService;
        this.minecraftService = minecraftService;
        this.notificationsService = notificationsService;
    }

    @Override
    protected void render(DrawContext ctx, int z) {
        Matrix3x2fStack ms = ctx.getMatrices();

        float scaledImgW = imageWidth * scale;
        float scaledImgH = imageHeight * scale;

        float buttonW = scaledImgW + padding;
        float buttonH = scaledImgH + padding;

        float imgX = (buttonW - scaledImgW) / 2f;
        float imgY = (buttonH - scaledImgH) / 2f;

        render2DService.setupRender();

        ms.pushMatrix();

        render2DService.renderSoftRoundedRectOutline(ctx, 0f, 0f, buttonW, buttonH, z, radius, buttonColor, outlineColor, outlineWidth, blurWidth);

        ms.translate(imgX, imgY);
        render2DService.renderImage(ctx, image, 0f, 0f, scaledImgW, scaledImgH, z);

        ms.popMatrix();

        render2DService.endRender();
    }

    public void updateRenderForScreen(DrawContext ctx, Identifier image, float relX, float relY, float padding, float scale, int z, float radius, Color buttonColor, Color outlineColor, float outlineWidth, float blurWidth) {
        updateRender(image, relX, relY, padding, scale, radius, buttonColor, outlineColor, outlineWidth, blurWidth);
        float screenScale = Math.min(ctx.getScaledWindowWidth() / 1280f, ctx.getScaledWindowHeight() / 720f);
        super.updateRenderForScreen(ctx, z, screenScale);
    }

    public void updateRenderForParent(DrawContext ctx, Identifier image, float relX, float relY, float padding, float scale, float parW, float parH, int z, float radius, Color buttonColor, Color outlineColor, float outlineWidth, float blurWidth) {
        updateRender(image, relX, relY, padding, scale, radius, buttonColor, outlineColor, outlineWidth, blurWidth);
        super.updateRenderForParent(ctx, parW, parH, z);
    }

    private void updateRender(Identifier image, float relX, float relY, float padding, float scale, float radius, Color buttonColor, Color outlineColor, float outlineWidth, float blurWidth) {
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
            notificationsService.addNotification(NotificationType.EXCEPTION, "%s%sИсключение".formatted(DARK_RED, BOLD),
                    "Исключение в ImageButtonDrawableElement/getImageSizes: %s%s".formatted(DARK_RED, e), 5f);
            return new int[]{0, 0};
        }
    }
}