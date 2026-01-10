package me.yuugao.holymoderation.client.util.serviceLocator.service;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;
import me.yuugao.holymoderation.client.util.serviceLocator.service.NotificationType;
import me.yuugao.holymoderation.client.util.serviceLocator.service.Service;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.OrderedText;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
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
            ResourceManager rm = ServiceLocator.getMinecraftService().getClient().getResourceManager();
            RECT = new ShaderProgram(rm, "rect", VertexFormats.POSITION_COLOR);
            ROUNDED_RECT = new ShaderProgram(rm, "rounded_rect", VertexFormats.POSITION_COLOR_TEXTURE);
            SOFT_ROUNDED_RECT = new ShaderProgram(rm, "soft_rounded_rect", VertexFormats.POSITION_COLOR_TEXTURE);
            ROUNDED_RECT_OUTLINE = new ShaderProgram(rm, "rounded_rect_outline", VertexFormats.POSITION_COLOR_TEXTURE);
            SOFT_ROUNDED_RECT_OUTLINE = new ShaderProgram(rm, "soft_rounded_rect_outline", VertexFormats.POSITION_COLOR_TEXTURE);
            RGB_PALETTE = new ShaderProgram(rm, "rgb_palette", VertexFormats.POSITION_COLOR_TEXTURE);
        } catch (Exception e) {
            ServiceLocator.getNotificationService().addNotification(
                    NotificationType.EXCEPTION,
                    "Исключение",
                    "Render2DService: " + e,
                    5f
            );
        }
    }

    public void renderRect(MatrixStack matrices, float x, float y, float w, float h, int z, Color color) {
        RECT.getUniformOrDefault("Color").set(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        RenderSystem.setShader(() -> RECT);
        renderQuad(matrices, x, y, w, h, z);
    }

    public void renderRoundedRect(MatrixStack matrices, float x, float y, float w, float h, int z, float radius, Color color) {
        ROUNDED_RECT.getUniformOrDefault("radius").set(radius);
        ROUNDED_RECT.getUniformOrDefault("size").set(w, h);
        ROUNDED_RECT.getUniformOrDefault("color").set(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        RenderSystem.setShader(() -> ROUNDED_RECT);
        renderQuad(matrices, x, y, w, h, z);
    }

    public void renderSoftRoundedRect(MatrixStack matrices, float x, float y, float w, float h, int z, float radius, Color color, int blurWidth) {
        SOFT_ROUNDED_RECT.getUniformOrDefault("Radius").set(radius);
        SOFT_ROUNDED_RECT.getUniformOrDefault("Size").set(w, h);
        SOFT_ROUNDED_RECT.getUniformOrDefault("Color").set(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        SOFT_ROUNDED_RECT.getUniformOrDefault("BlurWidth").set((float) blurWidth);
        RenderSystem.setShader(() -> SOFT_ROUNDED_RECT);
        float tw = w + blurWidth * 2f;
        float th = h + blurWidth * 2f;
        renderQuad(matrices, x - blurWidth, y - blurWidth, tw, th, z);
    }

    public void renderRoundedRectOutline(MatrixStack matrices, float x, float y, float w, float h, int z, float radius, Color color, Color outlineColor, float outlineWidth) {
        ROUNDED_RECT_OUTLINE.getUniformOrDefault("radius").set(radius);
        ROUNDED_RECT_OUTLINE.getUniformOrDefault("size").set(w, h);
        ROUNDED_RECT_OUTLINE.getUniformOrDefault("color").set(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        ROUNDED_RECT_OUTLINE.getUniformOrDefault("OutlineColor").set(outlineColor.getRed() / 255f, outlineColor.getGreen() / 255f, outlineColor.getBlue() / 255f, outlineColor.getAlpha() / 255f);
        ROUNDED_RECT_OUTLINE.getUniformOrDefault("OutlineWidth").set(outlineWidth);
        RenderSystem.setShader(() -> ROUNDED_RECT_OUTLINE);
        renderQuad(matrices, x, y, w, h, z);
    }

    public void renderSoftRoundedRectOutline(MatrixStack matrices, float x, float y, float w, float h, int z, float radius, Color color, Color outlineColor, float outlineWidth, float blurWidth) {
        SOFT_ROUNDED_RECT_OUTLINE.getUniformOrDefault("Radius").set(radius);
        SOFT_ROUNDED_RECT_OUTLINE.getUniformOrDefault("Size").set(w, h);
        SOFT_ROUNDED_RECT_OUTLINE.getUniformOrDefault("Color").set(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        SOFT_ROUNDED_RECT_OUTLINE.getUniformOrDefault("OutlineColor").set(outlineColor.getRed() / 255f, outlineColor.getGreen() / 255f, outlineColor.getBlue() / 255f, outlineColor.getAlpha() / 255f);
        SOFT_ROUNDED_RECT_OUTLINE.getUniformOrDefault("OutlineWidth").set(outlineWidth);
        SOFT_ROUNDED_RECT_OUTLINE.getUniformOrDefault("BlurWidth").set(blurWidth);
        RenderSystem.setShader(() -> SOFT_ROUNDED_RECT_OUTLINE);
        float tw = w + blurWidth * 2f;
        float th = h + blurWidth * 2f;
        renderQuad(matrices, x - blurWidth, y - blurWidth, tw, th, z);
    }

    public void renderRGBPalette(MatrixStack matrices, float x, float y, int z, float radius, Color outlineColor, float outlineWidth) {
        float size = (radius + outlineWidth) * 2f;
        RGB_PALETTE.getUniformOrDefault("Radius").set(radius);
        RGB_PALETTE.getUniformOrDefault("OutlineColor").set(outlineColor.getRed() / 255f, outlineColor.getGreen() / 255f, outlineColor.getBlue() / 255f, outlineColor.getAlpha() / 255f);
        RGB_PALETTE.getUniformOrDefault("OutlineWidth").set(outlineWidth);
        RGB_PALETTE.getUniformOrDefault("Size").set(size, size);
        RenderSystem.setShader(() -> RGB_PALETTE);
        renderQuad(matrices, x, y, size, size, z);
    }

    private void renderQuad(MatrixStack matrices, float x, float y, float w, float h, int z) {
        Tessellator t = Tessellator.getInstance();
        BufferBuilder b = t.getBuffer();
        matrices.push();
        matrices.translate(x, y, z);
        Matrix4f m = matrices.peek().getPositionMatrix();
        b.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR_TEXTURE);
        b.vertex(m, 0, h, 0).color(1f, 1f, 1f, 1f).texture(0, h).next();
        b.vertex(m, w, h, 0).color(1f, 1f, 1f, 1f).texture(w, h).next();
        b.vertex(m, w, 0, 0).color(1f, 1f, 1f, 1f).texture(w, 0).next();
        b.vertex(m, 0, 0, 0).color(1f, 1f, 1f, 1f).texture(0, 0).next();
        t.draw();
        matrices.pop();
    }

    public void renderText(TextRenderer tr, String text, int x, int y, int z, int color, boolean shadow, DrawContext ctx) {
        ctx.getMatrices().push();
        ctx.getMatrices().translate(0f, 0f, z);
        ctx.drawText(tr, text, x, y, color, shadow);
        ctx.getMatrices().pop();
    }

    public void renderText(TextRenderer tr, OrderedText text, int x, int y, int z, int color, boolean shadow, DrawContext ctx) {
        ctx.getMatrices().push();
        ctx.getMatrices().translate(0f, 0f, z);
        ctx.drawText(tr, text, x, y, color, shadow);
        ctx.getMatrices().pop();
    }

    public void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public void endRender() {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
    }
}