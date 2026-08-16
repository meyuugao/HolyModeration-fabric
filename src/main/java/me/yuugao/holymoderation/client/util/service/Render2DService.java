package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gl.UniformType;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BuiltBuffer;
import net.minecraft.client.render.ProjectionMatrix2;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.resource.ResourceManager;
import net.minecraft.text.OrderedText;
import net.minecraft.util.Identifier;

import org.joml.Matrix3x2fStack;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map;
import java.util.OptionalInt;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class Render2DService {
    private static final int UBO_SIZE = 16;
    private static final float GUI_NEAR = 1000.0f;
    private static final float GUI_FAR = 11000.0f;
    private static final float GUI_Z = -11000.0f;

    private final MinecraftService minecraftService;
    private final LoggerService loggerService;

    private QuadPipeline rectPipeline;
    private QuadPipeline roundedRectPipeline;
    private QuadPipeline softRoundedRectPipeline;
    private QuadPipeline roundedRectOutlinePipeline;
    private QuadPipeline softRoundedRectOutlinePipeline;
    private QuadPipeline rgbPalettePipeline;

    private ProjectionMatrix2 guiProjection;
    private final Map<String, GpuBuffer> uniformBuffers = new HashMap<>();
    private final Matrix4f guiModelView = new Matrix4f();
    private final Vector4f guiColorModulator = new Vector4f(1f, 1f, 1f, 1f);
    private final Vector3f guiModelOffset = new Vector3f();
    private final Matrix4f guiTextureMat = new Matrix4f();

    @FunctionalInterface
    private interface UniformWriter {
        void write(CommandEncoder encoder);
    }

    private record QuadPipeline(RenderPipeline pipeline, String[] uniforms) {
    }

    public void initializeShaders(ResourceManager rm) {
        try {
            rectPipeline = buildPipeline("pipeline/hm_rect", "core/rect", "core/rect", "Color");
            roundedRectPipeline = buildPipeline("pipeline/hm_rounded_rect", "core/rounded_rect", "core/rounded_rect", "Radius", "Size", "Color");
            softRoundedRectPipeline = buildPipeline("pipeline/hm_soft_rounded_rect", "core/soft_rounded_rect", "core/soft_rounded_rect", "Radius", "Size", "Color", "BlurWidth");
            roundedRectOutlinePipeline = buildPipeline("pipeline/hm_rounded_rect_outline", "core/rounded_rect_outline", "core/rounded_rect_outline", "Radius", "Size", "Color", "OutlineColor", "OutlineWidth");
            softRoundedRectOutlinePipeline = buildPipeline("pipeline/hm_soft_rounded_rect_outline", "core/soft_rounded_rect_outline", "core/soft_rounded_rect_outline", "Radius", "Size", "Color", "OutlineColor", "OutlineWidth", "BlurWidth");
            rgbPalettePipeline = buildPipeline("pipeline/hm_rgb_palette", "core/rgb_palette", "core/rgb_palette", "Radius", "OutlineColor", "OutlineWidth", "Size");

            loggerService.info("Shaders has been initialized.");
        } catch (Exception e) {
            loggerService.exception("Exception in Render2DService/initializeShaders: %s".formatted(e));
            rectPipeline = null;
            roundedRectPipeline = null;
            softRoundedRectPipeline = null;
            roundedRectOutlinePipeline = null;
            softRoundedRectOutlinePipeline = null;
            rgbPalettePipeline = null;
        }
    }

    private static QuadPipeline buildPipeline(String location, String vertexShader, String fragmentShader, String... uniforms) {
        RenderPipeline.Builder builder = RenderPipeline.builder()
                .withLocation(location)
                .withVertexShader(vertexShader)
                .withFragmentShader(fragmentShader)
                .withUniform("DynamicTransforms", UniformType.UNIFORM_BUFFER)
                .withUniform("Projection", UniformType.UNIFORM_BUFFER)
                .withBlend(BlendFunction.TRANSLUCENT)
                .withVertexFormat(VertexFormats.POSITION_TEXTURE_COLOR, VertexFormat.DrawMode.QUADS);
        for (String uniform : uniforms) {
            builder.withUniform(uniform, UniformType.UNIFORM_BUFFER);
        }
        return new QuadPipeline(builder.build(), uniforms);
    }

    public void renderRect(DrawContext ctx, float x, float y, float w, float h, int z, Color color) {
        ensureShaders();
        renderQuad(ctx, rectPipeline, encoder -> writeVec4(encoder, "Color", color), x, y, w, h);
    }

    public void renderRoundedRect(DrawContext ctx, float x, float y, float w, float h, int z, float radius, Color color) {
        ensureShaders();
        renderQuad(ctx, roundedRectPipeline, encoder -> {
            writeFloat(encoder, "Radius", radius);
            writeVec2(encoder, "Size", w, h);
            writeVec4(encoder, "Color", color);
        }, x, y, w, h);
    }

    public void renderSoftRoundedRect(DrawContext ctx, float x, float y, float w, float h, int z, float radius, Color color, int blurWidth) {
        ensureShaders();
        float blur = blurWidth;
        renderQuad(ctx, softRoundedRectPipeline, encoder -> {
            writeFloat(encoder, "Radius", radius);
            writeVec2(encoder, "Size", w, h);
            writeVec4(encoder, "Color", color);
            writeFloat(encoder, "BlurWidth", blur);
        }, x - blur, y - blur, w + 2f * blur, h + 2f * blur);
    }

    public void renderRoundedRectOutline(DrawContext ctx, float x, float y, float w, float h, int z, float radius, Color color, Color outlineColor, float outlineWidth) {
        ensureShaders();
        renderQuad(ctx, roundedRectOutlinePipeline, encoder -> {
            writeFloat(encoder, "Radius", radius);
            writeVec2(encoder, "Size", w, h);
            writeVec4(encoder, "Color", color);
            writeVec4(encoder, "OutlineColor", outlineColor);
            writeFloat(encoder, "OutlineWidth", outlineWidth);
        }, x, y, w, h);
    }

    public void renderSoftRoundedRectOutline(DrawContext ctx, float x, float y, float w, float h, int z, float radius, Color color, Color outlineColor, float outlineWidth, float blurWidth) {
        ensureShaders();
        float blur = blurWidth;
        renderQuad(ctx, softRoundedRectOutlinePipeline, encoder -> {
            writeFloat(encoder, "Radius", radius);
            writeVec2(encoder, "Size", w, h);
            writeVec4(encoder, "Color", color);
            writeVec4(encoder, "OutlineColor", outlineColor);
            writeFloat(encoder, "OutlineWidth", outlineWidth);
            writeFloat(encoder, "BlurWidth", blur);
        }, x - blur, y - blur, w + 2f * blur, h + 2f * blur);
    }

    public void renderRGBPalette(DrawContext ctx, float x, float y, int z, float radius, Color outlineColor, float outlineWidth) {
        ensureShaders();
        float size = (radius + outlineWidth) * 2f;
        renderQuad(ctx, rgbPalettePipeline, encoder -> {
            writeFloat(encoder, "Radius", radius);
            writeVec4(encoder, "OutlineColor", outlineColor);
            writeFloat(encoder, "OutlineWidth", outlineWidth);
            writeVec2(encoder, "Size", size, size);
        }, x, y, size, size);
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

    private void ensureShaders() {
        if (rectPipeline == null) {
            initializeShaders(minecraftService.getClient().getResourceManager());
        }
    }

    private void renderQuad(DrawContext ctx, QuadPipeline pipeline, UniformWriter uniforms,
                            float x, float y, float w, float h) {
        if (pipeline == null) {
            return;
        }
        try {
            Matrix3x2fStack matrices = ctx.getMatrices();
            float[] p0 = transform(matrices, x, y + h);
            float[] p1 = transform(matrices, x + w, y + h);
            float[] p2 = transform(matrices, x + w, y);
            float[] p3 = transform(matrices, x, y);

            BufferBuilder bb = Tessellator.getInstance().begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
            bb.vertex(p0[0], p0[1], 0f).color(1f, 1f, 1f, 1f).texture(0f, h);
            bb.vertex(p1[0], p1[1], 0f).color(1f, 1f, 1f, 1f).texture(w, h);
            bb.vertex(p2[0], p2[1], 0f).color(1f, 1f, 1f, 1f).texture(w, 0f);
            bb.vertex(p3[0], p3[1], 0f).color(1f, 1f, 1f, 1f).texture(0f, 0f);
            BuiltBuffer built = bb.end();

            GpuDevice device = RenderSystem.getDevice();
            CommandEncoder encoder = device.createCommandEncoder();
            try {
                uniforms.write(encoder);

                GpuBuffer vbo = device.createBuffer(() -> "hm_quad_vbo",
                        GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_COPY_DST, built.getBuffer());
                try {
                    GpuBufferSlice projection = guiProjection()
                            .set(ctx.getScaledWindowWidth(), ctx.getScaledWindowHeight());
                    GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().write(
                            guiModelView.setTranslation(0f, 0f, GUI_Z), guiColorModulator, guiModelOffset, guiTextureMat);

                    try (RenderPass pass = encoder.createRenderPass(
                            () -> "hm_render_pass",
                            minecraftService.getClient().getFramebuffer().getColorAttachmentView(),
                            OptionalInt.empty())) {
                        pass.setPipeline(pipeline.pipeline());
                        pass.setUniform("Projection", projection);
                        pass.setUniform("DynamicTransforms", transforms);
                        for (String uniform : pipeline.uniforms()) {
                            pass.setUniform(uniform, uniformBuffer(uniform).slice());
                        }

                        RenderSystem.ShapeIndexBuffer shapeIndexBuffer = RenderSystem.getSequentialBuffer(VertexFormat.DrawMode.QUADS);
                        pass.setVertexBuffer(0, vbo);
                        pass.setIndexBuffer(shapeIndexBuffer.getIndexBuffer(4), shapeIndexBuffer.getIndexType());
                        pass.drawIndexed(0, 0, 6, 1);
                    }
                } finally {
                    vbo.close();
                }
            } finally {
                built.close();
            }
        } catch (Exception e) {
            loggerService.exception("Exception in Render2DService/renderQuad: %s".formatted(e));
        }
    }

    private ProjectionMatrix2 guiProjection() {
        if (guiProjection == null) {
            guiProjection = new ProjectionMatrix2("gui", GUI_NEAR, GUI_FAR, true);
        }
        return guiProjection;
    }

    private GpuBuffer uniformBuffer(String name) {
        return uniformBuffers.computeIfAbsent(name, n -> {
            GpuDevice device = RenderSystem.getDevice();
            ByteBuffer data = ByteBuffer.allocate(UBO_SIZE).order(ByteOrder.nativeOrder());
            return device.createBuffer(() -> "hm_uniform_" + n,
                    GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST, data);
        });
    }

    private void writeFloat(CommandEncoder encoder, String name, float value) {
        ByteBuffer data = ByteBuffer.allocate(UBO_SIZE).order(ByteOrder.nativeOrder());
        Std140Builder.intoBuffer(data).putFloat(value);
        encoder.writeToBuffer(uniformBuffer(name).slice(), data.rewind());
    }

    private void writeVec2(CommandEncoder encoder, String name, float x, float y) {
        ByteBuffer data = ByteBuffer.allocate(UBO_SIZE).order(ByteOrder.nativeOrder());
        Std140Builder.intoBuffer(data).putVec2(x, y);
        encoder.writeToBuffer(uniformBuffer(name).slice(), data.rewind());
    }

    private void writeVec4(CommandEncoder encoder, String name, Color color) {
        ByteBuffer data = ByteBuffer.allocate(UBO_SIZE).order(ByteOrder.nativeOrder());
        Std140Builder.intoBuffer(data).putVec4(
                color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);
        encoder.writeToBuffer(uniformBuffer(name).slice(), data.rewind());
    }

    private static float[] transform(Matrix3x2fStack matrices, float x, float y) {
        return new float[]{
                matrices.m00 * x + matrices.m10 * y + matrices.m20,
                matrices.m01 * x + matrices.m11 * y + matrices.m21
        };
    }
}
