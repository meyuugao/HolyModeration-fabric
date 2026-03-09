package me.yuugao.holymoderation.client.gui.tabs.impl.main;

import me.yuugao.holymoderation.client.gui.screen.impl.MainGuiScreen;
import me.yuugao.holymoderation.client.gui.tabs.Tab;
import me.yuugao.holymoderation.client.modules.drawable.element.impl.ColorPickerDrawableElement;
import me.yuugao.holymoderation.client.modules.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

public class GeneralTab extends Tab<MainGuiScreen> {
    public GeneralTab(MainGuiScreen parent, ServiceContext serviceContext) {
        super(parent, serviceContext);

        drawableElements.put("ColorPicker", new ColorPickerDrawableElement(serviceContext, PivotMode.CENTER));
    }

    @Override
    public void onRender(DrawContext ctx, int relMouseX, int relMouseY, float tickDelta) {
        ColorPickerDrawableElement colorPicker = (ColorPickerDrawableElement) drawableElements.get("ColorPicker");

        float relRadius = 0.15f;
        float animatedRelRadius = relRadius * parent.getAnimValue();

        float pW = parent.getWidth();
        float pH = parent.getHeight();

        colorPicker.updateRenderForParent(ctx, 0.5f, 0.5f, pW, pH,
                parent.getRenderPriority(), Math.min(pW, pH) * animatedRelRadius,
                new Color(0x000000), 2f);

        Color color = colorPicker.getColorFromMouse(pW, pH, relMouseX, relMouseY);

        serviceContext.getRender2DService().renderRect(ctx.getMatrices(), 0, 0, 100, 100,
                parent.getRenderPriority(), color == null ? Color.WHITE : color);
    }
}