package me.yuugao.holymoderation.client.util.serviceLocator.service;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL40C;

import java.awt.Color;

import com.mojang.blaze3d.systems.RenderSystem;

public class Render2DService extends Service {
    public void drawRect(MatrixStack matrices, float x, float y, float width, float height, Color c) {
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

        buffer.vertex(matrix, x, y + height, 0.0F).color(c.getRGB()).next();
        buffer.vertex(matrix, x + width, y + height, 0.0F).color(c.getRGB()).next();
        buffer.vertex(matrix, x + width, y, 0.0F).color(c.getRGB()).next();
        buffer.vertex(matrix, x, y, 0.0F).color(c.getRGB()).next();
        tessellator.draw();

        endRender();
    }

    public void renderRoundedGradientRect(MatrixStack matrices, float x, float y, float width, float height, float radius, Color color1, Color color2, Color color3, Color color4) {
        RenderSystem.colorMask(false, false, false, true);
        RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
        RenderSystem.clear(GL40C.GL_COLOR_BUFFER_BIT, false);
        RenderSystem.colorMask(true, true, true, true);

        drawRoundedRect(matrices, x, y, width, height, radius, color1, 4);

        setupRender();
        RenderSystem.blendFunc(GL40C.GL_DST_ALPHA, GL40C.GL_ONE_MINUS_DST_ALPHA);

        Matrix4f matrix = matrices.peek().getPositionMatrix();
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        bufferBuilder.vertex(matrix, x, y + height, 0.0F).color(color1.getRGB());
        bufferBuilder.vertex(matrix, x + width, y + height, 0.0F).color(color2.getRGB());
        bufferBuilder.vertex(matrix, x + width, y, 0.0F).color(color3.getRGB());
        bufferBuilder.vertex(matrix, x, y, 0.0F).color(color4.getRGB());
        tessellator.draw();

        endRender();
    }

    public void drawRoundedRect(MatrixStack matrices, float x, float y, float width, float height, float radius, Color color, float samples) {
        setupRender();
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);

        float cr = color.getRed() / 255f;
        float cg = color.getGreen() / 255f;
        float cb = color.getBlue() / 255f;
        float ca = color.getAlpha() / 255f;

        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);

        double[][] corners = new double[][]{
                new double[]{x + width - radius, y + height - radius, radius},
                new double[]{x + width - radius, y + radius, radius},
                new double[]{x + radius, y + radius, radius},
                new double[]{x + radius, y + height - radius, radius}
        };

        for (int i = 0; i < 4; i++) {
            double[] corner = corners[i];
            double rad = corner[2];
            for (double r = i * 90d; r < (90 + i * 90d); r += (90 / samples)) {
                float rad1 = (float) Math.toRadians(r);
                float sin = (float) (Math.sin(rad1) * rad);
                float cos = (float) (Math.cos(rad1) * rad);
                buffer.vertex(matrices.peek().getPositionMatrix(), (float) corner[0] + sin, (float) corner[1] + cos, 0.0F).color(cr, cg, cb, ca).next();
            }
            float rad1 = (float) Math.toRadians(90 + i * 90d);
            float sin = (float) (Math.sin(rad1) * rad);
            float cos = (float) (Math.cos(rad1) * rad);
            buffer.vertex(matrices.peek().getPositionMatrix(), (float) corner[0] + sin, (float) corner[1] + cos, 0.0F).color(cr, cg, cb, ca).next();
        }
        tessellator.draw();

        endRender();
    }

    public void drawText(TextRenderer textRenderer, String text, float x, float y, int color, boolean shadow, DrawContext drawContext) {
        textRenderer.draw(text, x, y, color, shadow, drawContext.getMatrices().peek().getPositionMatrix(), drawContext.getVertexConsumers(), TextRenderer.TextLayerType.NORMAL, 0, 15728880, textRenderer.isRightToLeft());
    }

    public void setupRender() {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
    }

    public void endRender() {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }
}