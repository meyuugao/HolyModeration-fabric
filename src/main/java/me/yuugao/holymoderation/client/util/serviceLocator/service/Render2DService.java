package me.yuugao.holymoderation.client.util.serviceLocator.service;

import static me.yuugao.holymoderation.client.util.Colors.*;


import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;

import org.joml.Matrix4f;

import java.awt.Color;

import com.mojang.blaze3d.systems.RenderSystem;

public class Render2DService extends Service {
    private ShaderProgram RECT;
    private ShaderProgram ROUNDED_RECT;
    private ShaderProgram SOFT_ROUNDED_RECT;
    private ShaderProgram ROUNDED_RECT_OUTLINE;
    private ShaderProgram SOFT_ROUNDED_RECT_OUTLINE;
    private ShaderProgram RGB_PALETTE;

    public void initializeShaders() {
        try {
            RECT = new ShaderProgram(
                    ServiceLocator.getMinecraftService().getClient().getResourceManager(),
                    "rect",
                    VertexFormats.POSITION_COLOR
            );

            ROUNDED_RECT = new ShaderProgram(
                    ServiceLocator.getMinecraftService().getClient().getResourceManager(),
                    "rounded_rect",
                    VertexFormats.POSITION_COLOR_TEXTURE
            );

            SOFT_ROUNDED_RECT = new ShaderProgram(
                    ServiceLocator.getMinecraftService().getClient().getResourceManager(),
                    "soft_rounded_rect",
                    VertexFormats.POSITION_COLOR_TEXTURE
            );

            ROUNDED_RECT_OUTLINE = new ShaderProgram(
                    ServiceLocator.getMinecraftService().getClient().getResourceManager(),
                    "rounded_rect_outline",
                    VertexFormats.POSITION_COLOR_TEXTURE
            );

            SOFT_ROUNDED_RECT_OUTLINE = new ShaderProgram(
                    ServiceLocator.getMinecraftService().getClient().getResourceManager(),
                    "soft_rounded_rect_outline",
                    VertexFormats.POSITION_COLOR_TEXTURE
            );

            RGB_PALETTE = new ShaderProgram(
                    ServiceLocator.getMinecraftService().getClient().getResourceManager(),
                    "rgb_palette",
                    VertexFormats.POSITION_COLOR_TEXTURE
            );
        } catch (Exception e) {
            ServiceLocator.getNotificationService().addNotification(NotificationType.EXCEPTION, DARK_RED + BOLD + "Исключение", "Исключение в Render2DService/initializeShaders: " + DARK_RED + e, 5f);
        }
    }

    public void renderRect(MatrixStack matrices, float x, float y, float width, float height, Color color) {
        setupRender();
        RECT.getUniformOrDefault("Color").set(
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                color.getAlpha() / 255f
        );
        RenderSystem.setShader(() -> RECT);

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();

        matrices.push();
        matrices.translate(x, y, 0);
        Matrix4f matrix = matrices.peek().getPositionMatrix();

        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        buffer.vertex(matrix, 0, height, 0).color(1f, 1f, 1f, 1f).next();
        buffer.vertex(matrix, width, height, 0).color(1f, 1f, 1f, 1f).next();
        buffer.vertex(matrix, width, 0, 0).color(1f, 1f, 1f, 1f).next();
        buffer.vertex(matrix, 0, 0, 0).color(1f, 1f, 1f, 1f).next();
        tessellator.draw();

        matrices.pop();
        endRender();
    }

    public void renderRoundedRect(MatrixStack matrices, float x, float y, float width, float height, float radius, Color color) {
        setupRender();
        ROUNDED_RECT.getUniformOrDefault("radius").set(radius);
        ROUNDED_RECT.getUniformOrDefault("size").set(width, height);
        ROUNDED_RECT.getUniformOrDefault("color").set(
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                color.getAlpha() / 255f
        );
        RenderSystem.setShader(() -> ROUNDED_RECT);
        renderQuad(matrices, x, y, width, height);
        endRender();
    }

    public void renderSoftRoundedRect(MatrixStack matrices, float x, float y, float w, float h, float radius, Color color, int blurWidth) {
        setupRender();

        SOFT_ROUNDED_RECT.getUniformOrDefault("Radius").set(radius);
        SOFT_ROUNDED_RECT.getUniformOrDefault("Size").set(w, h);
        SOFT_ROUNDED_RECT.getUniformOrDefault("Color").set(
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                color.getAlpha() / 255f
        );
        SOFT_ROUNDED_RECT.getUniformOrDefault("BlurWidth").set((float) blurWidth);

        RenderSystem.setShader(() -> SOFT_ROUNDED_RECT);

        float totalWidth = w + 2 * blurWidth;
        float totalHeight = h + 2 * blurWidth;
        renderQuad(matrices, x - blurWidth, y - blurWidth, totalWidth, totalHeight);

        endRender();
    }


    public void renderRoundedRectOutline(MatrixStack matrices, float x, float y, float width, float height, float radius, Color color, Color outlineColor, float outlineWidth) {
        setupRender();
        ROUNDED_RECT_OUTLINE.getUniformOrDefault("radius").set(radius);
        ROUNDED_RECT_OUTLINE.getUniformOrDefault("size").set(width, height);
        ROUNDED_RECT_OUTLINE.getUniformOrDefault("color").set(
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                color.getAlpha() / 255f
        );
        ROUNDED_RECT_OUTLINE.getUniformOrDefault("OutlineColor").set(
                outlineColor.getRed() / 255f,
                outlineColor.getGreen() / 255f,
                outlineColor.getBlue() / 255f,
                outlineColor.getAlpha() / 255f
        );
        ROUNDED_RECT_OUTLINE.getUniformOrDefault("OutlineWidth").set(outlineWidth);

        RenderSystem.setShader(() -> ROUNDED_RECT_OUTLINE);
        renderQuad(matrices, x, y, width, height);
        endRender();
    }

    public void renderSoftRoundedRectOutline(MatrixStack matrices, float x, float y, float w, float h, float radius, Color color, Color outlineColor, float outlineWidth, float blurWidth) {
        setupRender();

        SOFT_ROUNDED_RECT_OUTLINE.getUniformOrDefault("Radius").set(radius);
        SOFT_ROUNDED_RECT_OUTLINE.getUniformOrDefault("Size").set(w, h);
        SOFT_ROUNDED_RECT_OUTLINE.getUniformOrDefault("Color").set(
                color.getRed() / 255f,
                color.getGreen() / 255f,
                color.getBlue() / 255f,
                color.getAlpha() / 255f
        );
        SOFT_ROUNDED_RECT_OUTLINE.getUniformOrDefault("OutlineColor").set(
                outlineColor.getRed() / 255f,
                outlineColor.getGreen() / 255f,
                outlineColor.getBlue() / 255f,
                outlineColor.getAlpha() / 255f
        );
        SOFT_ROUNDED_RECT_OUTLINE.getUniformOrDefault("OutlineWidth").set(outlineWidth);
        SOFT_ROUNDED_RECT_OUTLINE.getUniformOrDefault("BlurWidth").set(blurWidth);

        RenderSystem.setShader(() -> SOFT_ROUNDED_RECT_OUTLINE);

        float totalWidth = w + 2 * blurWidth;
        float totalHeight = h + 2 * blurWidth;
        renderQuad(matrices, x - blurWidth, y - blurWidth, totalWidth, totalHeight);

        endRender();
    }

    public void renderRGBPalette(MatrixStack matrices, float centerX, float centerY, float radius, Color outlineColor, float outlineWidth) {
        setupRender();

        float size = (radius + outlineWidth) * 2;

        RGB_PALETTE.getUniformOrDefault("Radius").set(radius);
        RGB_PALETTE.getUniformOrDefault("OutlineColor").set(
                outlineColor.getRed() / 255f,
                outlineColor.getGreen() / 255f,
                outlineColor.getBlue() / 255f,
                outlineColor.getAlpha() / 255f
        );
        RGB_PALETTE.getUniformOrDefault("OutlineWidth").set(outlineWidth);
        RGB_PALETTE.getUniformOrDefault("Size").set(size, size);

        RenderSystem.setShader(() -> RGB_PALETTE);

        float x = centerX - radius - outlineWidth;
        float y = centerY - radius - outlineWidth;
        renderQuad(matrices, x, y, size, size);

        endRender();
    }

    private void renderQuad(MatrixStack matrices, float x, float y, float w, float h) {
        Tessellator t = Tessellator.getInstance();
        BufferBuilder b = t.getBuffer();
        matrices.push();
        matrices.translate(x, y, 0);
        Matrix4f m = matrices.peek().getPositionMatrix();
        b.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
        b.vertex(m, 0, h, 0).color(1f, 1f, 1f, 1f).texture(0, h).next();
        b.vertex(m, w, h, 0).color(1f, 1f, 1f, 1f).texture(w, h).next();
        b.vertex(m, w, 0, 0).color(1f, 1f, 1f, 1f).texture(w, 0).next();
        b.vertex(m, 0, 0, 0).color(1f, 1f, 1f, 1f).texture(0, 0).next();
        t.draw();
        matrices.pop();
    }

    public void renderText(TextRenderer textRenderer, String text, float x, float y, int color, boolean shadow, DrawContext drawContext) {
        textRenderer.draw(
                text,
                x,
                y,
                color,
                shadow,
                drawContext.getMatrices().peek().getPositionMatrix(),
                drawContext.getVertexConsumers(),
                TextRenderer.TextLayerType.NORMAL,
                0,
                15728880,
                textRenderer.isRightToLeft()
        );
    }

    public void setupRender() {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
    }

    public void endRender() {
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}