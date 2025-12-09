package me.yuugao.holymoderation.client.util;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;
import net.minecraft.client.util.math.MatrixStack;

import java.awt.Color;

public class ColorPicker {
    private float centerX;
    private float centerY;
    private float radius;
    private Color outlineColor;
    private float outlineWidth;
    
    private Color selectedColor;
    
    public ColorPicker(float centerX, float centerY, float radius, Color outlineColor, float outlineWidth) {
        this.centerX = centerX;
        this.centerY = centerY;
        this.radius = radius;
        this.outlineColor = outlineColor;
        this.outlineWidth = outlineWidth;
        this.selectedColor = Color.WHITE;
    }
    
    public void render(MatrixStack matrices) {
        ServiceLocator.getRender2DService().renderRGBPalette(matrices, centerX, centerY, radius, outlineColor, outlineWidth);
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
    
    public Color getSelectedColor() {
        return selectedColor;
    }
    
    public void setPosition(float centerX, float centerY) {
        this.centerX = centerX;
        this.centerY = centerY;
    }
    
    public void setRadius(float radius) {
        this.radius = radius;
    }
    
    public void setOutlineColor(Color outlineColor) {
        this.outlineColor = outlineColor;
    }
    
    public void setOutlineWidth(float outlineWidth) {
        this.outlineWidth = outlineWidth;
    }
    
    public float getCenterX() {
        return centerX;
    }
    
    public float getCenterY() {
        return centerY;
    }
    
    public float getRadius() {
        return radius;
    }
    
    public Color getOutlineColor() {
        return outlineColor;
    }
    
    public float getOutlineWidth() {
        return outlineWidth;
    }
}