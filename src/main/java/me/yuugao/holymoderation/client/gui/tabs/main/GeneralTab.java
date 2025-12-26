package me.yuugao.holymoderation.client.gui.tabs.main;

import me.yuugao.holymoderation.client.gui.modules.ColorPickerModule;
import me.yuugao.holymoderation.client.gui.screen.MainGuiScreen;
import me.yuugao.holymoderation.client.gui.tabs.Tab;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

public class GeneralTab extends Tab {
    public GeneralTab(MainGuiScreen parent, ServiceContext serviceContext) {
        super(parent, serviceContext);

        modules.put("ColorPicker", new ColorPickerModule(this, serviceContext));
    }

    @Override
    public void onRender(DrawContext context, int mouseX, int mouseY, float tickDelta) {
        MainGuiScreen mainGuiScreen = (MainGuiScreen) parent;
        ColorPickerModule colorPickerModule = (ColorPickerModule) modules.get("ColorPicker");

        float radius = 25f;
        colorPickerModule.render(context.getMatrices(), mainGuiScreen.getX() + mainGuiScreen.getWidth() / 2, mainGuiScreen.getY() + mainGuiScreen.getHeight() / 2, radius * mainGuiScreen.getAnimValue(), new Color(0x000000), 3);
        colorPickerModule.updateColorFromMouse(mouseX, mouseY);
    }
}