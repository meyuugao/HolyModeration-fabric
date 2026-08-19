package me.yuugao.holymoderation.client.gui.tabs;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.DrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.SearchDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.SliderDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.ToggleDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button.ButtonAction;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button.impl.TextButtonDrawableElement;
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

import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

public abstract class SettingsTab extends Tab<MainGuiScreen> {
    protected static final float SEARCH_Y = 36f;
    protected static final float SEARCH_H = 20f;
    protected static final float ROW_HEIGHT = 28f;
    protected static final float PAD = 16f;
    protected static final float LABEL_W = 140f;
    protected static final float VAL_W = 62f;
    protected static final float GAP = 10f;
    protected static final float FIELD_H = 20f;
    protected static final float TOGGLE_W = 42f;
    protected static final float TOGGLE_H = 22f;
    protected static final float BADGE_H = 18f;
    protected static final float BOTTOM_PAD = 10f;

    protected final ThemeService themeService;
    protected final ConfigManagerService configManagerService;
    protected final MinecraftService minecraftService;
    protected final Render2DService render2DService;
    protected final DrawableElementFactory factory;

    protected final List<Row> rows = new ArrayList<>();
    private final SearchDrawableElement searchField;

    protected float scroll = 0f;
    protected float maxScroll = 0f;

    protected static final class Row {
        public final String label;
        public final DrawableElement element;
        public final SearchDrawableElement valueField;
        public final Supplier<String> valueText;
        public final Runnable commit;

        Row(String label, DrawableElement element, SearchDrawableElement valueField, Supplier<String> valueText, Runnable commit) {
            this.label = label;
            this.element = element;
            this.valueField = valueField;
            this.valueText = valueText;
            this.commit = commit;
        }

        List<SearchDrawableElement> fields() {
            List<SearchDrawableElement> list = new ArrayList<>();
            if (element instanceof SearchDrawableElement field) list.add(field);
            if (valueField != null) list.add(valueField);
            return list;
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

        this.searchField = factory.createSearch(null);
        this.searchField.setPlaceholder("Поиск по настройкам...");
    }

    protected ToggleDrawableElement addToggle(String label, boolean initial, Consumer<Boolean> onChange) {
        ToggleDrawableElement toggle = factory.createToggle(onChange);
        toggle.setEnabled(initial);
        rows.add(new Row(label, toggle, null, null, null));
        return toggle;
    }

    protected SliderDrawableElement addSlider(String label, float min, float max, float step, float initial,
                                              Consumer<Float> onChange, Supplier<String> valueText, Runnable commit) {
        final SearchDrawableElement[] fieldRef = new SearchDrawableElement[1];

        SliderDrawableElement slider = factory.createSlider(v -> {
            onChange.accept(v);
            SearchDrawableElement f = fieldRef[0];
            if (f != null && !f.isFocused()) {
                f.setQuerySilent(valueText.get());
            }
        });
        slider.setRange(min, max);
        slider.setStep(step);
        slider.setValue(initial);

        SearchDrawableElement valueField = factory.createSearch(s -> parseSliderValue(slider, s, commit));
        valueField.setCentered(true);
        valueField.setPlaceholder("");
        valueField.setQuerySilent(valueText.get());
        fieldRef[0] = valueField;

        rows.add(new Row(label, slider, valueField, valueText, commit));
        return slider;
    }

    protected SearchDrawableElement addTextField(String label, String placeholder, String initial, Consumer<String> onChange) {
        SearchDrawableElement field = factory.createSearch(onChange);
        field.setQuerySilent(initial);
        if (placeholder != null) field.setPlaceholder(placeholder);
        rows.add(new Row(label, field, null, null, null));
        return field;
    }

    protected TextButtonDrawableElement addButton(String label, ButtonAction action) {
        TextButtonDrawableElement button = factory.createTextButton(
                me.yuugao.holymoderation.client.gui.drawable.render.PivotMode.LEFT_UP,
                action, true, Text.literal(label));
        rows.add(new Row(label, button, null, null, null));
        return button;
    }

    private void parseSliderValue(SliderDrawableElement slider, String s, Runnable commit) {
        String t = s.trim().replace(',', '.');
        if (t.isEmpty()) return;
        try {
            float v = Float.parseFloat(t);
            slider.setValue(v);
            if (commit != null) commit.run();
        } catch (NumberFormatException ignored) {
        }
    }

    protected List<Row> visibleRows() {
        String q = searchField.getQuery().trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) return rows;

        List<Row> visible = new ArrayList<>();
        for (Row row : rows) {
            if (row.label.toLowerCase(Locale.ROOT).contains(q)) {
                visible.add(row);
            }
        }
        return visible;
    }

    protected float contentStartY() {
        return SEARCH_Y + SEARCH_H + 12f;
    }

    protected String searchQuery() {
        return searchField.getQuery();
    }

    protected float rowsEndY() {
        return contentStartY() + visibleRows().size() * ROW_HEIGHT;
    }

    protected float extraHeight() {
        return 0f;
    }

    protected float contentHeight() {
        return rowsEndY() + extraHeight() + BOTTOM_PAD;
    }

    protected void renderExtra(DrawContext ctx, ThemePalette palette, float pW, float pH, int z, float scroll) {
    }

    protected void renderOverlay(DrawContext ctx, ThemePalette palette, float pW, float pH, int z) {
    }

    @Override
    public void onRender(DrawContext ctx, int relMouseX, int relMouseY, float tickDelta) {
        ThemePalette palette = themeService.getPalette();
        float pW = parent.getWidth();
        float pH = parent.getHeight();
        int z = parent.getRenderPriority();

        searchField.updateRenderForParent(ctx, PAD / pW, SEARCH_Y / pH, pW - PAD * 2f, SEARCH_H, pW, pH, z,
                9f, palette.surface, palette.outline, palette.textPrimary, palette.textMuted, palette.primary, 1.5f, 2f);

        float fieldX = PAD + LABEL_W + GAP;
        float fieldW = pW - PAD * 2f - LABEL_W - GAP;
        float valX = pW - PAD - VAL_W;
        float sliderW = valX - fieldX - GAP;

        float bottom = pH - 6f;
        maxScroll = Math.max(0f, contentHeight() - bottom);
        scroll = Math.max(0f, Math.min(scroll, maxScroll));

        scissor(ctx, 0f, contentStartY() - 8f, pW, bottom);

        List<Row> visible = visibleRows();
        for (int i = 0; i < visible.size(); i++) {
            Row row = visible.get(i);
            float rowTop = contentStartY() + i * ROW_HEIGHT - scroll;

            renderLabel(ctx, z, row.label, PAD, rowTop + (ROW_HEIGHT - minecraftService.getClient().textRenderer.fontHeight) / 2f + 1f, palette);

            DrawableElement element = row.element;
            if (element instanceof ToggleDrawableElement toggle) {
                toggle.updateRenderForParent(ctx, (pW - PAD - TOGGLE_W) / pW, (rowTop + (ROW_HEIGHT - TOGGLE_H) / 2f) / pH,
                        TOGGLE_W, TOGGLE_H, pW, pH, z,
                        palette.primary, palette.surface, palette.textPrimary, palette.outline, 1.5f, 2f);
            } else if (element instanceof SliderDrawableElement slider) {
                slider.updateRenderForParent(ctx, fieldX / pW, (rowTop + (ROW_HEIGHT - FIELD_H) / 2f) / pH,
                        sliderW, pW, pH, z,
                        5f, palette.track, palette.primary, palette.primaryBright, palette.outline, 1.5f, 2f);
                if (row.valueField != null) {
                    row.valueField.updateRenderForParent(ctx, valX / pW, (rowTop + (ROW_HEIGHT - BADGE_H) / 2f) / pH,
                            VAL_W, BADGE_H, pW, pH, z,
                            9f, palette.surface, palette.outline, palette.textPrimary, palette.textMuted, palette.primary, 1f, 2f);
                }
            } else if (element instanceof SearchDrawableElement field) {
                field.updateRenderForParent(ctx, fieldX / pW, (rowTop + (ROW_HEIGHT - FIELD_H) / 2f) / pH,
                        fieldW, FIELD_H, pW, pH, z,
                        8f, palette.surface, palette.outline, palette.textPrimary, palette.textMuted, palette.primary, 1.5f, 2f);
            } else if (element instanceof TextButtonDrawableElement button) {
                button.updateRenderForParent(ctx, (pW - PAD - 180f) / pW, (rowTop + (ROW_HEIGHT - FIELD_H) / 2f) / pH,
                        180f, pW, pH, z, 9f, palette.primary, palette.primaryBright, 1.5f, 2f);
            }
        }

        renderExtra(ctx, palette, pW, pH, z, scroll);

        ctx.disableScissor();

        renderOverlay(ctx, palette, pW, pH, z);
    }

    protected void scissor(DrawContext ctx, float x, float y, float x2, float y2) {
        MatrixStack ms = ctx.getMatrices();
        float[] a = transformPoint(ms, x, y);
        float[] b = transformPoint(ms, x2, y2);
        ctx.enableScissor((int) a[0], (int) a[1], (int) Math.ceil(b[0]), (int) Math.ceil(b[1]));
    }

    protected static float[] transformPoint(MatrixStack ms, float x, float y) {
        Matrix4f m = ms.peek().getPositionMatrix();
        return new float[]{
                m.m00() * x + m.m10() * y + m.m20(),
                m.m01() * x + m.m11() * y + m.m21()
        };
    }

    protected void renderLabel(DrawContext ctx, int z, String text, float x, float y, ThemePalette palette) {
        TextRenderer tr = minecraftService.getClient().textRenderer;
        render2DService.renderText(tr, Text.literal(text).asOrderedText(),
                (int) x, (int) y, z, palette.textSecondary.getRGB(), false, ctx);
    }

    @Override
    public boolean onMouseClick(float mouseX, float mouseY) {
        float pW = parent.getWidth();
        float pH = parent.getHeight();

        boolean overSearch = searchField.isMouseOver(pW, pH, mouseX, mouseY);
        searchField.setFocused(overSearch);
        if (overSearch) {
            unfocusAllRows();
            return true;
        }

        List<Row> visible = visibleRows();

        for (int i = visible.size() - 1; i >= 0; i--) {
            DrawableElement element = visible.get(i).element;
            if (element instanceof ToggleDrawableElement toggle && toggle.handleClick(pW, pH, mouseX, mouseY)) {
                unfocusAllRows();
                return true;
            }
            if (element instanceof SliderDrawableElement slider && slider.handleClick(pW, pH, mouseX, mouseY)) {
                unfocusAllRows();
                return true;
            }
            if (element instanceof TextButtonDrawableElement button && button.hitInParent(pW, pH, mouseX, mouseY)) {
                unfocusAllRows();
                return true;
            }
        }

        boolean overField = false;
        for (Row row : visible) {
            for (SearchDrawableElement field : row.fields()) {
                boolean was = field.isFocused();
                boolean over = field.isMouseOver(pW, pH, mouseX, mouseY);
                if (over && !was && row.valueField == field && row.valueText != null) {
                    field.setQuerySilent(row.valueText.get());
                }
                if (over) {
                    field.handleClick(pW, pH, mouseX, mouseY);
                } else {
                    field.setFocused(false);
                }
                if (over) overField = true;
            }
        }
        return overField;
    }

    @Override
    public void onMouseScroll(double dx, double dy, float mouseX, float mouseY) {
        float pW = parent.getWidth();
        float pH = parent.getHeight();
        boolean handled = false;
        for (Row row : visibleRows()) {
            if (row.element instanceof SliderDrawableElement slider) {
                if (slider.handleScroll(pW, pH, dy, mouseX, mouseY)) {
                    handled = true;
                }
            }
        }
        if (!handled && maxScroll > 0f) {
            scroll -= (float) (dy * 22f);
            scroll = Math.max(0f, Math.min(scroll, maxScroll));
        }
    }

    @Override
    public void onMouseDrag(float mouseX, float mouseY) {
        float pW = parent.getWidth();
        float pH = parent.getHeight();
        for (Row row : visibleRows()) {
            if (row.element instanceof SliderDrawableElement slider) {
                slider.handleDrag(pW, pH, mouseX, mouseY);
            }
            for (SearchDrawableElement field : row.fields()) {
                field.handleDrag(pW, pH, mouseX, mouseY);
            }
        }
    }

    @Override
    public void onMouseMoved(float mouseX, float mouseY) {
        float pW = parent.getWidth();
        float pH = parent.getHeight();
        for (Row row : visibleRows()) {
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
            for (SearchDrawableElement field : row.fields()) {
                field.handleRelease();
            }
        }
    }

    @Override
    public boolean onCharTyped(char chr) {
        if (searchField.onCharTyped(chr)) return true;
        for (Row row : visibleRows()) {
            for (SearchDrawableElement field : row.fields()) {
                if (field.onCharTyped(chr)) return true;
            }
        }
        return false;
    }

    @Override
    public boolean onKeyPress(int key, int scancode, int action, int modifiers) {
        if (searchField.onKeyPress(key, scancode, action, modifiers)) return true;
        for (Row row : visibleRows()) {
            for (SearchDrawableElement field : row.fields()) {
                if (field.onKeyPress(key, scancode, action, modifiers)) return true;
            }
        }
        return false;
    }

    private void unfocusAllRows() {
        for (Row row : rows) {
            for (SearchDrawableElement field : row.fields()) {
                if (field.isFocused() && row.valueField == field && row.valueText != null) {
                    field.setQuerySilent(row.valueText.get());
                }
                field.setFocused(false);
            }
        }
    }

    protected static float round2(float v) {
        return Math.round(v * 100f) / 100f;
    }
}
