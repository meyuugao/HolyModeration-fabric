package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;

import org.joml.Matrix4f;

import java.awt.Color;
import java.io.IOException;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class Render2DService {
    private final MinecraftService minecraftService;
    private final LoggerService loggerService;

    private ShaderProgram RECT;
    private ShaderProgram ROUNDED_RECT;
    private ShaderProgram SOFT_ROUNDED_RECT;
    private ShaderProgram ROUNDED_RECT_OUTLINE;
    private ShaderProgram SOFT_ROUNDED_RECT_OUTLINE;
    private ShaderProgram RGB_PALETTE;
    private ShaderProgram SV_SQUARE;
    private ShaderProgram HUE_BAR;

    private VertexConsumerProvider.Immediate textBuffers;
    private final java.util.ArrayDeque<int[]> scissorStack = new java.util.ArrayDeque<>();
    private int[] scissor = null;

    public void initializeShaders(ResourceManager rm) {
        try {
            RECT = new ShaderProgram(rm, "rect", VertexFormats.POSITION_COLOR);
            ROUNDED_RECT = new ShaderProgram(rm, "rounded_rect", VertexFormats.POSITION_COLOR_TEXTURE);
            SOFT_ROUNDED_RECT = new ShaderProgram(rm, "soft_rounded_rect", VertexFormats.POSITION_COLOR_TEXTURE);
            ROUNDED_RECT_OUTLINE = new ShaderProgram(rm, "rounded_rect_outline", VertexFormats.POSITION_COLOR_TEXTURE);
            SOFT_ROUNDED_RECT_OUTLINE = new ShaderProgram(rm, "soft_rounded_rect_outline", VertexFormats.POSITION_COLOR_TEXTURE);
            RGB_PALETTE = new ShaderProgram(rm, "rgb_palette", VertexFormats.POSITION_COLOR_TEXTURE);
            SV_SQUARE = new ShaderProgram(rm, "sv_square", VertexFormats.POSITION_COLOR_TEXTURE);
            HUE_BAR = new ShaderProgram(rm, "hue_bar", VertexFormats.POSITION_COLOR_TEXTURE);

            textBuffers = minecraftService.getClient().getBufferBuilders().getEntityVertexConsumers();
        } catch (IOException e) {
            loggerService.exception("Исключение в Render2DService/initializeShaders: %s".formatted(e));
            return;
        }
        loggerService.info("Shaders has been initialized.");
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
        float qw = w + blurWidth * 2f;
        float qh = h + blurWidth * 2f;
        renderQuad(matrices, x - blurWidth, y - blurWidth, qw, qh, z);
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
        float qw = w + blurWidth * 2f;
        float qh = h + blurWidth * 2f;
        renderQuad(matrices, x - blurWidth, y - blurWidth, qw, qh, z);
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

    public void renderSVSquare(MatrixStack matrices, float x, float y, float w, float h, int z, float radius, float hue, Color outlineColor, float outlineWidth) {
        SV_SQUARE.getUniformOrDefault("Radius").set(radius);
        SV_SQUARE.getUniformOrDefault("Size").set(w, h);
        SV_SQUARE.getUniformOrDefault("Hue").set(hue);
        SV_SQUARE.getUniformOrDefault("OutlineColor").set(outlineColor.getRed() / 255f, outlineColor.getGreen() / 255f, outlineColor.getBlue() / 255f, outlineColor.getAlpha() / 255f);
        SV_SQUARE.getUniformOrDefault("OutlineWidth").set(outlineWidth);
        RenderSystem.setShader(() -> SV_SQUARE);
        renderQuad(matrices, x, y, w, h, z);
    }

    public void renderHueBar(MatrixStack matrices, float x, float y, float w, float h, int z, float radius, Color outlineColor, float outlineWidth) {
        HUE_BAR.getUniformOrDefault("Radius").set(radius);
        HUE_BAR.getUniformOrDefault("Size").set(w, h);
        HUE_BAR.getUniformOrDefault("OutlineColor").set(outlineColor.getRed() / 255f, outlineColor.getGreen() / 255f, outlineColor.getBlue() / 255f, outlineColor.getAlpha() / 255f);
        HUE_BAR.getUniformOrDefault("OutlineWidth").set(outlineWidth);
        RenderSystem.setShader(() -> HUE_BAR);
        renderQuad(matrices, x, y, w, h, z);
    }

    public void renderImage(DrawContext ctx, Identifier texture, float x, float y, float w, float h, int z) {
        MatrixStack matrices = ctx.getMatrices();
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

        matrices.push();
        matrices.translate(x, y, 0f);
        Matrix4f m = matrices.peek().getPositionMatrix();

        Tessellator t = Tessellator.getInstance();
        BufferBuilder b = t.getBuffer();
        b.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        b.vertex(m, 0f, 0f, 0f).texture(0f, 0f).color(1f, 1f, 1f, 1f).next();
        b.vertex(m, 0f, h, 0f).texture(0f, 1f).color(1f, 1f, 1f, 1f).next();
        b.vertex(m, w, h, 0f).texture(1f, 1f).color(1f, 1f, 1f, 1f).next();
        b.vertex(m, w, 0f, 0f).texture(1f, 0f).color(1f, 1f, 1f, 1f).next();
        applyScissor();
        t.draw();
        clearScissor();

        matrices.pop();
        GlStateManager._activeTexture(33984);
        GlStateManager._bindTexture(0);
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
        applyScissor();
        t.draw();
        clearScissor();
        matrices.pop();
    }

    public void renderText(TextRenderer tr, String text, int x, int y, int z, int color, boolean shadow, DrawContext ctx) {
        MatrixStack ms = ctx.getMatrices();

        ms.push();
        ms.translate(0f, 0f, z);

        tr.draw(text, x, y, color, shadow, ms.peek().getPositionMatrix(), textBuffers,
                TextRenderer.TextLayerType.NORMAL, 0, 15728880);

        applyScissor();
        textBuffers.draw();
        clearScissor();

        ms.pop();
    }

    public void renderText(TextRenderer tr, OrderedText text, int x, int y, int z, int color, boolean shadow, DrawContext ctx) {
        MatrixStack ms = ctx.getMatrices();

        ms.push();
        ms.translate(0f, 0f, z);

        tr.draw(text, x, y, color, shadow, ms.peek().getPositionMatrix(), textBuffers,
                TextRenderer.TextLayerType.NORMAL, 0, 15728880);

        applyScissor();
        textBuffers.draw();
        clearScissor();

        ms.pop();
    }

    public void setupRender() {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
    }

    public void endRender() {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
    }

    public void resetScissor() {
        scissorStack.clear();
        scissor = null;
    }

    public void pushScissor(float x0, float y0, float x1, float y1) {
        int nx0 = Math.round(Math.min(x0, x1));
        int ny0 = Math.round(Math.min(y0, y1));
        int nx1 = Math.round(Math.max(x0, x1));
        int ny1 = Math.round(Math.max(y0, y1));
        if (scissor != null) {
            nx0 = Math.max(nx0, scissor[0]);
            ny0 = Math.max(ny0, scissor[1]);
            nx1 = Math.min(nx1, scissor[2]);
            ny1 = Math.min(ny1, scissor[3]);
        }
        scissorStack.push(scissor);
        scissor = new int[]{nx0, ny0, nx1, ny1};
    }

    public void popScissor() {
        scissor = scissorStack.isEmpty() ? null : scissorStack.pop();
    }

    private void applyScissor() {
        if (scissor == null) return;
        float sf = (float) minecraftService.getClient().getWindow().getScaleFactor();
        int sx = Math.max(0, (int) Math.floor(scissor[0] * sf));
        int sy = Math.max(0, (int) Math.floor(scissor[1] * sf));
        int ex = Math.max(sx, (int) Math.ceil(scissor[2] * sf));
        int ey = Math.max(sy, (int) Math.ceil(scissor[3] * sf));
        RenderSystem.enableScissor(sx, sy, ex - sx, ey - sy);
    }

    private void clearScissor() {
        if (scissor != null) {
            RenderSystem.disableScissor();
        }
    }
}