package me.yuugao.holymoderation.client.gui.modules;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.Render2DService;

import net.minecraft.client.util.math.MatrixStack;

import org.jetbrains.annotations.Nullable;

import java.awt.Color;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ColorPickerModule extends GuiModule {
    private float centerX;
    private float centerY;
    private float radius;
    private float outlineWidth;
    private Color outlineColor;

    public ColorPickerModule(ServiceContext serviceContext, float relX, float relY) {
        super(serviceContext, relX, relY);
    }

    public void render(MatrixStack matrices, float parentX, float parentY, float parentWidth, float parentHeight, int z, float relScale, Color outlineColor, float outlineWidth) {
        Render2DService render2DService = serviceContext.getRender2DService();

        float x = getX(parentX, parentWidth);
        float y = getY(parentY, parentHeight);

        this.centerX = x;
        this.centerY = y;
        this.radius = Math.min(parentWidth, parentHeight) * relScale;
        this.outlineColor = outlineColor;
        this.outlineWidth = outlineWidth;

        matrices.push();

        render2DService.renderRGBPalette(matrices, x - this.radius - outlineWidth,
                y - this.radius - outlineWidth, z, this.radius, outlineColor, outlineWidth);

        matrices.pop();
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        float dx = (float) (mouseX - centerX);
        float dy = (float) (mouseY - centerY);
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        return dist <= radius;
    }

    @Nullable
    public Color getColorFromMouse(double mouseX, double mouseY) {
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