package me.yuugao.holymoderation.client.gui.tabs;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.DrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.SearchDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.SliderDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.ToggleDrawableElement;
import me.yuugao.holymoderation.client.gui.screen.impl.MainGuiScreen;
import me.yuugao.holymoderation.client.util.factory.DrawableElementFactory;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.Render2DService;
import me.yuugao.holymoderation.client.util.service.ThemePalette;
import me.yuugao.holymoderation.client.util.service.ThemeService;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class SettingsTab extends Tab<MainGuiScreen> {
    protected static final float ROW_START_Y = 36f;
    protected static final float ROW_HEIGHT = 28f;
    protected static final float PAD = 16f;
    protected static final float LABEL_W = 132f;
    protected static final float VAL_W = 58f;
    protected static final float GAP = 10f;
    protected static final float FIELD_H = 20f;
    protected static final float TOGGLE_W = 42f;
    protected static final float TOGGLE_H = 22f;
    protected static final float BADGE_H = 18f;

    protected final ThemeService themeService;
    protected final ConfigManagerService configManagerService;
    protected final MinecraftService minecraftService;
    protected final Render2DService render2DService;
    protected final DrawableElementFactory factory;

    protected final List<Row> rows = new ArrayList<>();

    protected static final class Row {
        final String label;
        final DrawableElement element;
        final Supplier<String> valueText;
        final Runnable commit;

        Row(String label, DrawableElement element, Supplier<String> valueText, Runnable commit) {
            this.label = label;
            this.element = element;
            this.valueText = valueText;
            this.commit = commit;
        }
    }

    public SettingsTab(MainGuiScreen parent, ThemeService themeService, ConfigManagerService configManagerService,
                       MinecraftService minecraftService, Render2DService render2DService, DrawableElementFactory factory) {
        super(parent);
        this.themeService = themeService;
        this.configManagerService = configManagerService;
        this.minecraftService = minecraftService;
        this.render2DService = render2DService;
        this.factory = factory;
    }

    protected ToggleDrawableElement addToggle(String label, boolean initial, Consumer<Boolean> onChange) {
        ToggleDrawableElement toggle = factory.createToggle(onChange);
        toggle.setEnabled(initial);
        rows.add(new Row(label, toggle, null, null));
        return toggle;
    }

    protected SliderDrawableElement addSlider(String label, float min, float max, float step, float initial,
                                              Consumer<Float> onChange, Supplier<String> valueText, Runnable commit) {
        SliderDrawableElement slider = factory.createSlider(onChange);
        slider.setRange(min, max);
        slider.setStep(step);
        slider.setValue(initial);
        rows.add(new Row(label, slider, valueText, commit));
        return slider;
    }

    protected SearchDrawableElement addTextField(String label, String placeholder, String initial, Consumer<String> onChange) {
        SearchDrawableElement field = factory.createSearch(onChange);
        field.setQuerySilent(initial);
        if (placeholder != null) field.setPlaceholder(placeholder);
        rows.add(new Row(label, field, null, null));
        return field;
    }

    @Override
    public void onRender(DrawContext ctx, int relMouseX, int relMouseY, float tickDelta) {
        ThemePalette palette = themeService.getPalette();
        float pW = parent.getWidth();
        float pH = parent.getHeight();
        int z = parent.getRenderPriority();

        float fieldX = PAD + LABEL_W + GAP;
        float fieldW = pW - PAD * 2f - LABEL_W - GAP;
        float valX = pW - PAD - VAL_W;
        float sliderW = valX - fieldX - GAP;

        for (int i = 0; i < rows.size(); i++) {
            Row row = rows.get(i);
            float rowTop = ROW_START_Y + i * ROW_HEIGHT;

            renderLabel(ctx, z, row.label, PAD, rowTop + (ROW_HEIGHT - minecraftService.getClient().textRenderer.fontHeight) / 2f, palette);

            DrawableElement element = row.element;
            if (element instanceof ToggleDrawableElement toggle) {
                toggle.updateRenderForParent(ctx, (pW - PAD - TOGGLE_W) / pW, (rowTop + (ROW_HEIGHT - TOGGLE_H) / 2f) / pH,
                        TOGGLE_W, TOGGLE_H, pW, pH, z,
                        palette.primary, palette.surface, palette.textPrimary, palette.outline, 1.5f, 2f);
            } else if (element instanceof SliderDrawableElement slider) {
                slider.updateRenderForParent(ctx, fieldX / pW, (rowTop + (ROW_HEIGHT - FIELD_H) / 2f) / pH,
                        sliderW, pW, pH, z,
                        5f, palette.track, palette.primary, palette.primaryBright, palette.outline, 1.5f, 2f);
                if (row.valueText != null) {
                    renderBadge(ctx, z, row.valueText.get(), valX, rowTop + (ROW_HEIGHT - BADGE_H) / 2f, VAL_W, BADGE_H, palette);
                }
            } else if (element instanceof SearchDrawableElement field) {
                field.updateRenderForParent(ctx, fieldX / pW, (rowTop + (ROW_HEIGHT - FIELD_H) / 2f) / pH,
                        fieldW, FIELD_H, pW, pH, z,
                        8f, palette.surface, palette.outline, palette.textPrimary, palette.textMuted, palette.primary, 1.5f, 2f);
            }
        }
    }

    protected void renderLabel(DrawContext ctx, int z, String text, float x, float y, ThemePalette palette) {
        TextRenderer tr = minecraftService.getClient().textRenderer;
        render2DService.renderText(tr, Text.literal(text).asOrderedText(),
                (int) x, (int) y, z, palette.textSecondary.getRGB(), false, ctx);
    }

    protected void renderBadge(DrawContext ctx, int z, String text, float x, float y, float w, float h, ThemePalette palette) {
        TextRenderer tr = minecraftService.getClient().textRenderer;
        render2DService.renderSoftRoundedRectOutline(ctx, x, y, w, h, z,
                h / 2f, palette.surface, palette.outline, 1f, 2f);
        int tw = tr.getWidth(text);
        render2DService.renderText(tr, Text.literal(text).asOrderedText(),
                (int) (x + (w - tw) / 2f), (int) (y + (h - tr.fontHeight) / 2f), z, palette.textPrimary.getRGB(), false, ctx);
    }

    @Override
    public boolean onMouseClick(float mouseX, float mouseY) {
        float pW = parent.getWidth();
        float pH = parent.getHeight();

        for (int i = rows.size() - 1; i >= 0; i--) {
            DrawableElement element = rows.get(i).element;
            if (element instanceof ToggleDrawableElement toggle && toggle.handleClick(pW, pH, mouseX, mouseY)) {
                unfocusAll();
                return true;
            }
            if (element instanceof SliderDrawableElement slider && slider.handleClick(pW, pH, mouseX, mouseY)) {
                unfocusAll();
                return true;
            }
        }

        boolean overField = false;
        for (Row row : rows) {
            if (row.element instanceof SearchDrawableElement field) {
                boolean over = field.isMouseOver(pW, pH, mouseX, mouseY);
                field.setFocused(over);
                if (over) overField = true;
            }
        }
        return overField;
    }

    @Override
    public void onMouseScroll(double dx, double dy, float mouseX, float mouseY) {
        float pW = parent.getWidth();
        float pH = parent.getHeight();
        for (Row row : rows) {
            if (row.element instanceof SliderDrawableElement slider) {
                slider.handleScroll(pW, pH, dy, mouseX, mouseY);
            }
        }
    }

    @Override
    public void onMouseDrag(float mouseX, float mouseY) {
        float pW = parent.getWidth();
        float pH = parent.getHeight();
        for (Row row : rows) {
            if (row.element instanceof SliderDrawableElement slider) {
                slider.handleDrag(pW, pH, mouseX, mouseY);
            }
        }
    }

    @Override
    public void onMouseMoved(float mouseX, float mouseY) {
        float pW = parent.getWidth();
        float pH = parent.getHeight();
        for (Row row : rows) {
            if (row.element instanceof SliderDrawableElement slider) {
                slider.handleDrag(pW, pH, mouseX, mouseY);
            }
        }
    }

    @Override
    public void onMouseRelease() {
        for (Row row : rows) {
            if (row.element instanceof SliderDrawableElement slider) {
                slider.handleRelease();
                if (row.commit != null) row.commit.run();
            }
        }
    }

    @Override
    public boolean onCharTyped(char chr) {
        for (Row row : rows) {
            if (row.element instanceof SearchDrawableElement field && field.onCharTyped(chr)) return true;
        }
        return false;
    }

    @Override
    public boolean onKeyPress(int key, int scancode, int action, int modifiers) {
        for (Row row : rows) {
            if (row.element instanceof SearchDrawableElement field && field.onKeyPress(key, scancode, action, modifiers)) return true;
        }
        return false;
    }

    private void unfocusAll() {
        for (Row row : rows) {
            if (row.element instanceof SearchDrawableElement field) field.setFocused(false);
        }
    }

    protected static float round2(float v) {
        return Math.round(v * 100f) / 100f;
    }
}
