package me.yuugao.holymoderation.client.util.factory;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.ColorPickerDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button.ButtonAction;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button.impl.ImageButtonDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button.impl.TextButtonDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.service.AnimationService;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.NotificationsService;
import me.yuugao.holymoderation.client.util.service.Render2DService;

import net.minecraft.text.Text;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class DrawableElementFactory {
    private final AnimationService animationService;
    private final Render2DService render2DService;
    private final MinecraftService minecraftService;
    private final NotificationsService notificationsService;

    public ColorPickerDrawableElement createColorPicker() {
        return new ColorPickerDrawableElement(animationService, render2DService);
    }

    public ImageButtonDrawableElement createImageButton(PivotMode pivotMode, ButtonAction action, boolean enabled) {
        return new ImageButtonDrawableElement(animationService, render2DService, minecraftService, notificationsService, pivotMode, action, enabled);
    }

    public TextButtonDrawableElement createTextButton(PivotMode pivotMode, ButtonAction action, boolean enabled, Text text) {
        return new TextButtonDrawableElement(animationService, render2DService, minecraftService, pivotMode, action, enabled, text);
    }
}
