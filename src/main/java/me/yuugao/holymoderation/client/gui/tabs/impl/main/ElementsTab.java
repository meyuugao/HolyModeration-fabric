package me.yuugao.holymoderation.client.gui.tabs.impl.main;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.ScreenCtx;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.CheckboxDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.ColorPickerDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.DropdownDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.SearchDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.SliderDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.ToggleDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button.impl.ImageButtonDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button.impl.TextButtonDrawableElement;
import me.yuugao.holymoderation.client.gui.screen.impl.MainGuiScreen;
import me.yuugao.holymoderation.client.gui.tabs.Tab;
import me.yuugao.holymoderation.client.util.factory.DrawableElementFactory;
import me.yuugao.holymoderation.client.util.service.ThemePalette;
import me.yuugao.holymoderation.client.util.service.ThemeService;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Arrays;

public class ElementsTab extends Tab<MainGuiScreen> {
    private static final Identifier DEMO_ICON = Identifier.of("minecraft", "textures/gui/clear.png");

    private final ThemeService themeService;

    private final SliderDrawableElement slider;
    private final ToggleDrawableElement toggle;
    private final CheckboxDrawableElement checkbox;
    private final DropdownDrawableElement dropdown;
    private final SearchDrawableElement search;
    private final ColorPickerDrawableElement colorPicker;
    private final TextButtonDrawableElement textButton;
    private final ImageButtonDrawableElement imageButton;

    public ElementsTab(MainGuiScreen parent, DrawableElementFactory drawableElementFactory, ThemeService themeService) {
        super(parent);

        this.themeService = themeService;

        this.slider = drawableElementFactory.createSlider(null);
        this.toggle = drawableElementFactory.createToggle(null);
        this.checkbox = drawableElementFactory.createCheckbox(null);
        this.dropdown = drawableElementFactory.createDropdown(null);
        this.search = drawableElementFactory.createSearch(null);
        this.colorPicker = drawableElementFactory.createColorPicker();
        this.textButton = drawableElementFactory.createTextButton(me.yuugao.holymoderation.client.gui.drawable.render.PivotMode.LEFT_UP,
                () -> {}, true, Text.literal("Кнопка"));
        this.imageButton = drawableElementFactory.createImageButton(me.yuugao.holymoderation.client.gui.drawable.render.PivotMode.LEFT_UP,
                () -> {}, true);

        slider.setRange(0f, 100f);
        slider.setStep(1f);
        slider.setValue(50f);

        dropdown.setOptions(Arrays.asList("Вариант 1", "Вариант 2", "Вариант 3"));

        drawableElements.put("search", search);
        drawableElements.put("slider", slider);
        drawableElements.put("toggle", toggle);
        drawableElements.put("checkbox", checkbox);
        drawableElements.put("colorPicker", colorPicker);
        drawableElements.put("dropdown", dropdown);
    }

    @Override
    public void onRender(DrawContext ctx, int relMouseX, int relMouseY, float tickDelta) {
        ThemePalette palette = themeService.getPalette();

        float pW = parent.getWidth();
        float pH = parent.getHeight();
        int z = parent.getRenderPriority();

        search.updateRenderForParent(ctx, 28f / pW, 30f / pH, pW - 56f, 20f, pW, pH, z,
                8f, palette.surface, palette.outline, palette.textPrimary, palette.textMuted, palette.primary, 1.5f, 2f);

        slider.updateRenderForParent(ctx, 28f / pW, 0.25f, pW - 56f, pW, pH, z,
                4f, palette.track, palette.primary, palette.primaryBright, palette.outline, 1.5f, 2f);

        toggle.updateRenderForParent(ctx, 28f / pW, 0.40f, 42f, 22f, pW, pH, z,
                palette.primary, palette.surface, palette.textPrimary, palette.outline, 1.5f, 2f);

        checkbox.updateRenderForParent(ctx, 92f / pW, 0.40f, 22f, pW, pH, z,
                6f, palette.surface, palette.primary, palette.onPrimary, palette.outline, 1.5f, 2f);

        textButton.updateRenderForParent(ctx, 140f / pW, 0.40f, 110f, pW, pH, z,
                8f, palette.primary, palette.primaryBright, 1.5f, 2f);

        imageButton.updateRenderForParent(ctx, DEMO_ICON, 270f / pW, 0.40f, 6f, 0.25f, pW, pH, z,
                8f, palette.surface, palette.outline, 1.5f, 2f);

        dropdown.updateRenderForParent(ctx, 28f / pW, 0.55f, pW * 0.5f, 22f, pW, pH, z,
                8f, palette.surface, palette.outline, palette.surfaceElevated,
                palette.primaryDark, palette.primary, palette.textPrimary, palette.textSecondary, 1.5f, 2f);

        colorPicker.updateRenderForParent(ctx, 0.76f, 0.60f, pW, pH, z,
                Math.min(pW, pH) * 0.14f, 10f, 6f, palette.outline, 1.5f);
    }

    @Override
    public boolean onMouseClick(float mouseX, float mouseY) {
        float pW = parent.getWidth();
        float pH = parent.getHeight();

        if (dropdown.handleClick(pW, pH, mouseX, mouseY)) return true;
        if (slider.handleClick(pW, pH, mouseX, mouseY)) return true;
        if (toggle.handleClick(pW, pH, mouseX, mouseY)) return true;
        if (checkbox.handleClick(pW, pH, mouseX, mouseY)) return true;
        if (textButton.hitInParent(pW, pH, mouseX, mouseY)) return true;
        if (imageButton.hitInParent(pW, pH, mouseX, mouseY)) return true;
        if (colorPicker.handleClick(new ScreenCtx(pW, pH, mouseX, mouseY))) return true;
        search.handleClick(pW, pH, mouseX, mouseY);
        return false;
    }

    @Override
    public void onMouseScroll(double dx, double dy, float mouseX, float mouseY) {
        float pW = parent.getWidth();
        float pH = parent.getHeight();

        if (dropdown.handleScroll(pW, pH, dy, mouseX, mouseY)) return;
        slider.handleScroll(pW, pH, dy, mouseX, mouseY);
    }

    @Override
    public void onMouseDrag(float mouseX, float mouseY) {
        slider.handleDrag(parent.getWidth(), parent.getHeight(), mouseX, mouseY);
        colorPicker.handleDrag(parent.getWidth(), parent.getHeight(), mouseX, mouseY);
    }

    @Override
    public void onMouseRelease() {
        slider.handleRelease();
        colorPicker.handleRelease();
    }

    @Override
    public void onMouseMoved(float mouseX, float mouseY) {
        dropdown.updateHovered(parent.getWidth(), parent.getHeight(), mouseX, mouseY);
    }

    @Override
    public boolean onCharTyped(char chr) {
        return search.onCharTyped(chr);
    }

    @Override
    public boolean onKeyPress(int key, int scancode, int action, int modifiers) {
        return search.onKeyPress(key, scancode, action, modifiers);
    }
}
