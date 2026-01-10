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

        modules.put("ColorPicker", new ColorPickerModule(serviceContext));
    }

    @Override
    public void onRender(DrawContext context, int mouseX, int mouseY, float tickDelta) {
        MainGuiScreen mainGuiScreen = parent;
        ColorPickerModule colorPickerModule = (ColorPickerModule) modules.get("ColorPicker");

        float radius = 25f;
        float totalRadius = radius * mainGuiScreen.getAnimValue(); //tip: отнимай от x не ток радиус а ещё и outlinewidth (possibly * 2)
        colorPickerModule.render(context.getMatrices(),
                mainGuiScreen.getX() + mainGuiScreen.getWidth() / 2 - totalRadius - colorPickerModule.getOutlineWidth(),
                mainGuiScreen.getY() + mainGuiScreen.getHeight() / 2 - totalRadius - colorPickerModule.getOutlineWidth(),
                parent.getRenderPriority(), totalRadius, new Color(0x000000), 3);
        colorPickerModule.updateColorFromMouse(mouseX, mouseY);
    }
}