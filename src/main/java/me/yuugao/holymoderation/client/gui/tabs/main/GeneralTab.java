package me.yuugao.holymoderation.client.gui.tabs.main;

import me.yuugao.holymoderation.client.gui.modules.ColorPickerModule;
import me.yuugao.holymoderation.client.gui.screen.MainGuiScreen;
import me.yuugao.holymoderation.client.gui.tabs.Tab;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

public class GeneralTab extends Tab<MainGuiScreen> {
    public GeneralTab(MainGuiScreen parent, ServiceContext serviceContext) {
        super(parent, serviceContext);

        modules.put("ColorPicker", new ColorPickerModule(serviceContext, 0.5f, 0.5f));
    }

    @Override
    public void onRender(DrawContext context, int mouseX, int mouseY, float tickDelta) {
        MainGuiScreen mainGuiScreen = parent;
        ColorPickerModule colorPickerModule = (ColorPickerModule) modules.get("ColorPicker");

        float relRadius = 0.15f;
        float animatedRelRadius = relRadius * mainGuiScreen.getAnimValue();
        colorPickerModule.render(context.getMatrices(), mainGuiScreen.getX(), mainGuiScreen.getY(),
                mainGuiScreen.getWidth(), mainGuiScreen.getHeight(), parent.getRenderPriority(),
                animatedRelRadius, new Color(0x000000), 2f);
    }
}