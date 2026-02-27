package me.yuugao.holymoderation.client.gui.modules.button;

import me.yuugao.holymoderation.client.gui.modules.GuiModule;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.awt.Color;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ButtonModule extends GuiModule {
    private float x;
    private float y;
    private float width;
    private float height;
    private Text text;
    private ButtonAction action;
    private boolean enabled;

    public ButtonModule(ButtonAction action, boolean enabled, ServiceContext serviceContext) {
        super(serviceContext);

        this.action = action;
        this.enabled = enabled;
    }

    public void render(MatrixStack matrices, float x, float y, int z, float width, float height, Text text, Color outlineColor, float outlineWidth) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
    }
}