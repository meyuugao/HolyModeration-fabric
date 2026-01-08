package me.yuugao.holymoderation.client.gui.modules;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ColorPickerModule extends GuiModule {
    private float centerX;
    private float centerY;
    private float radius;
    private Color outlineColor;
    private float outlineWidth;

    private Color selectedColor;

    public ColorPickerModule(ServiceContext serviceContext) {
        super(serviceContext);

        this.selectedColor = Color.WHITE;
    }

    public void render(MatrixStack matrices, float centerX, float centerY, int z, float radius, Color outlineColor, float outlineWidth) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.outlineColor = outlineColor;
        this.outlineWidth = outlineWidth;
        serviceContext.getRender2DService().renderRGBPalette(matrices, centerX, centerY, z, radius, outlineColor, outlineWidth);
    }

    public boolean isMouseOver(double mouseX, double mouseY) {
        float dx = (float) (mouseX - centerX);
        float dy = (float) (mouseY - centerY);
        float dist = (float) Math.sqrt(dx * dx + dy * dy);
        return dist <= radius + outlineWidth;
    }

    public void updateColorFromMouse(double mouseX, double mouseY) {
        float dx = (float) (mouseX - centerX);
        float dy = (float) (mouseY - centerY);
        float dist = (float) Math.sqrt(dx * dx + dy * dy);

        if (dist > radius) {
            return;
        }

        float angle = (float) Math.atan2(dy, dx);
        float hue = (angle + (float) Math.PI) / (2.0f * (float) Math.PI);
        float saturation = Math.min(dist / radius, 1.0f);
        float value = 1.0f;

        selectedColor = Color.getHSBColor(hue, saturation, value);
    }
}