package me.yuugao.holymoderation.client.gui.tabs.impl.main;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.ColorPickerDrawableElement;
import me.yuugao.holymoderation.client.gui.screen.impl.MainGuiScreen;
import me.yuugao.holymoderation.client.gui.tabs.Tab;
import me.yuugao.holymoderation.client.util.factory.DrawableElementFactory;

import net.minecraft.client.gui.DrawContext;

import java.awt.Color;

public class GeneralTab extends Tab<MainGuiScreen> {
    public GeneralTab(MainGuiScreen parent, DrawableElementFactory drawableElementFactory) {
        super(parent);

        drawableElements.put("ColorPicker", drawableElementFactory.createColorPicker());
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
    }
}