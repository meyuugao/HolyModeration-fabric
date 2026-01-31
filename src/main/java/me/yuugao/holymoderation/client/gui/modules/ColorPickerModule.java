package me.yuugao.holymoderation.client.gui.modules;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.Render2DService;

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
    private float outlineWidth;
    private Color outlineColor;
    private Color selectedColor;

    public ColorPickerModule(ServiceContext serviceContext) {
        super(serviceContext);

        this.selectedColor = Color.WHITE;
    }

    public void render(MatrixStack matrices, float x, float y, int z, float radius, Color outlineColor, float outlineWidth) {
        Render2DService render2DService = serviceContext.getRender2DService();

        this.centerX = x;
        this.centerY = y;
        this.radius = radius;
        this.outlineColor = outlineColor;
        this.outlineWidth = outlineWidth;

        render2DService.renderRGBPalette(matrices, x, y, z, radius, outlineColor, outlineWidth);
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