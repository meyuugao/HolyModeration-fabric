package me.yuugao.holymoderation.client.gui.tabs.main;

import me.yuugao.holymoderation.client.gui.modules.child.ColorPickerModule;
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
    public void onRender(DrawContext ctx, int mouseX, int mouseY, float tickDelta) {
        ColorPickerModule colorPickerModule = (ColorPickerModule) modules.get("ColorPicker");

        float relRadius = 0.15f;
        float animatedRelRadius = relRadius * parent.getAnimValue();

        colorPickerModule.render(ctx.getMatrices(), parent.getWidth(), parent.getHeight(),
                parent.getRenderPriority(), animatedRelRadius, new Color(0x000000), 2f);

        Color color = colorPickerModule.getColorFromMouse(mouseX, mouseY);

        serviceContext.getRender2DService().renderRect(ctx.getMatrices(), 0, 0, 100, 100,
                parent.getRenderPriority(), color == null ? Color.WHITE : color);
    }
}