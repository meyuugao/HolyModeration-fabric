package me.yuugao.holymoderation.client.gui.modules.child;

import me.yuugao.holymoderation.client.gui.modules.ChildGuiModule;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.Render2DService;

import net.minecraft.client.util.math.MatrixStack;

import org.jetbrains.annotations.Nullable;

import java.awt.Color;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ColorPickerModule extends ChildGuiModule {
    private float x;
    private float y;
    private float radius;
    private float outlineWidth;
    private Color outlineColor;

    public ColorPickerModule(ServiceContext serviceContext, float relX, float relY) {
        super(serviceContext, relX, relY);
    }

    public void render(MatrixStack matrices, float parentWidth, float parentHeight, int z,
                       float relScale, Color outlineColor, float outlineWidth) {
        Render2DService render2DService = serviceContext.getRender2DService();

        this.radius = Math.min(parentWidth, parentHeight) * relScale;
        this.outlineWidth = outlineWidth;
        this.x = relX * parentWidth - this.radius - this.outlineWidth;
        this.y = relY * parentHeight - this.radius - this.outlineWidth;
        this.outlineColor = outlineColor;

        render2DService.setupRender();
        matrices.push();
        render2DService.renderRGBPalette(matrices, this.x, this.y, z, this.radius, outlineColor, outlineWidth);
        matrices.pop();
        render2DService.endRender();
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        float centerX = x + radius + outlineWidth;
        float centerY = y + radius + outlineWidth;

        float dx = (float) (mouseX - centerX);
        float dy = (float) (mouseY - centerY);
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        return dist <= radius;
    }

    @Nullable
    public Color getColorFromMouse(double mouseX, double mouseY) {
        float centerX = x + radius + outlineWidth;
        float centerY = y + radius + outlineWidth;

        float dx = (float) (mouseX - centerX);
        float dy = (float) (mouseY - centerY);
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist > radius) return null;

        float angle = (float) Math.atan2(dy, dx);
        float hue = (angle + (float) Math.PI) / (2.0f * (float) Math.PI);
        float saturation = dist / radius;
        float value = 1.0f;

        return Color.getHSBColor(hue, saturation, value);
    }
}