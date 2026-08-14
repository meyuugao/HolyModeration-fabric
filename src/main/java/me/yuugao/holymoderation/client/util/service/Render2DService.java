package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;

import java.awt.Color;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class Render2DService {
    private final LoggerService loggerService;

    public void initializeShaders(ResourceManager rm) {
        loggerService.info("Shaders has been initialized.");
    }

    public void renderRect(DrawContext ctx, float x, float y, float w, float h, int z, Color color) {
        ctx.fill((int) x, (int) y, (int) (x + w), (int) (y + h), color.getRGB());
    }

    public void renderRoundedRect(DrawContext ctx, float x, float y, float w, float h, int z, float radius, Color color) {
        ctx.fill((int) x, (int) y, (int) (x + w), (int) (y + h), color.getRGB());
    }

    public void renderSoftRoundedRect(DrawContext ctx, float x, float y, float w, float h, int z, float radius, Color color, int blurWidth) {
        ctx.fill((int) x, (int) y, (int) (x + w), (int) (y + h), color.getRGB());
    }

    public void renderRoundedRectOutline(DrawContext ctx, float x, float y, float w, float h, int z, float radius, Color color, Color outlineColor, float outlineWidth) {
        ctx.fill((int) x, (int) y, (int) (x + w), (int) (y + h), outlineColor.getRGB());
        ctx.fill((int) (x + outlineWidth), (int) (y + outlineWidth), (int) (x + w - outlineWidth), (int) (y + h - outlineWidth), color.getRGB());
    }

    public void renderSoftRoundedRectOutline(DrawContext ctx, float x, float y, float w, float h, int z, float radius, Color color, Color outlineColor, float outlineWidth, float blurWidth) {
        ctx.fill((int) x, (int) y, (int) (x + w), (int) (y + h), outlineColor.getRGB());
        ctx.fill((int) (x + outlineWidth), (int) (y + outlineWidth), (int) (x + w - outlineWidth), (int) (y + h - outlineWidth), color.getRGB());
    }

    public void renderRGBPalette(DrawContext ctx, float x, float y, int z, float radius, Color outlineColor, float outlineWidth) {
        float size = (radius + outlineWidth) * 2f;
        ctx.fill((int) x, (int) y, (int) (x + size), (int) (y + size), outlineColor.getRGB());
    }

    public void renderImage(DrawContext ctx, Identifier texture, float x, float y, float w, float h, int z) {
        ctx.drawTexture(RenderPipelines.GUI_TEXTURED, texture, (int) x, (int) y, 0, 0, (int) w, (int) h, (int) w, (int) h);
    }

    public void renderText(TextRenderer tr, String text, int x, int y, int z, int color, boolean shadow, DrawContext ctx) {
        ctx.drawText(tr, text, x, y, color, shadow);
    }

    public void renderText(TextRenderer tr, OrderedText text, int x, int y, int z, int color, boolean shadow, DrawContext ctx) {
        ctx.drawText(tr, text, x, y, color, shadow);
    }

    public void setupRender() {
    }

    public void endRender() {
    }
}
