package me.yuugao.holymoderation.client.gui.modules.single;

import me.yuugao.holymoderation.client.gui.modules.SingleGuiModule;
import me.yuugao.holymoderation.client.gui.modules.button.ButtonAction;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;
import me.yuugao.holymoderation.client.util.serviceLocator.service.Render2DService;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.awt.Color;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ButtonModule extends SingleGuiModule {
    private float width;
    private float height;
    private Text text;
    private ButtonAction action;
    private boolean enabled;

    public ButtonModule(float relX, float relY, ButtonAction action, boolean enabled, ServiceContext serviceContext) {
        super(serviceContext, relX, relY);

        this.action = action;
        this.enabled = enabled;
    }

    public void render(MatrixStack matrices, float width, float height, float windowWidth, float windowHeight, int z,
                       Color buttonColor, Color outlineColor) {
        Render2DService render2DService = serviceContext.getRender2DService();

        this.width = width;
        this.height = height;

        matrices.push();

        render2DService.renderSoftRoundedRectOutline(matrices, 0f, 0f,
                getWidth(), getHeight(), z, 10f, buttonColor, outlineColor, 2, 3);

        matrices.pop();
    }
}