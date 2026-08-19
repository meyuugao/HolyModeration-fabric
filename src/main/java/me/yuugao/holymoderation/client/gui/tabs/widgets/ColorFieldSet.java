package me.yuugao.holymoderation.client.gui.tabs.widgets;

import me.yuugao.holymoderation.client.gui.drawable.element.impl.ScreenCtx;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.ColorPickerDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.SearchDrawableElement;
import me.yuugao.holymoderation.client.util.factory.DrawableElementFactory;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.Render2DService;
import me.yuugao.holymoderation.client.util.service.ThemePalette;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ColorFieldSet {
    private final MinecraftService minecraftService;
    private final Render2DService render2DService;
    private final String title;
    private final Supplier<Color> getter;
    private final Consumer<Color> setter;
    private final Runnable commit;

    private final ColorPickerDrawableElement picker;
    private final SearchDrawableElement hex;
    private final SearchDrawableElement r;
    private final SearchDrawableElement g;
    private final SearchDrawableElement b;
    private final SearchDrawableElement a;

    private boolean syncing = false;

    public ColorFieldSet(DrawableElementFactory factory, MinecraftService minecraftService, Render2DService render2DService,
                         String title, Supplier<Color> getter, Consumer<Color> setter, Runnable commit) {
        this.minecraftService = minecraftService;
        this.render2DService = render2DService;
        this.title = title;
        this.getter = getter;
        this.setter = setter;
        this.commit = commit;

        this.picker = factory.createColorPicker(this::onPicker);
        this.hex = factory.createSearch(this::onHex);
        this.r = factory.createSearch(s -> onChannel(s, 0));
        this.g = factory.createSearch(s -> onChannel(s, 1));
        this.b = factory.createSearch(s -> onChannel(s, 2));
        this.a = factory.createSearch(s -> onChannel(s, 3));

        hex.setPlaceholder("RRGGBB");
        r.setPlaceholder("R");
        g.setPlaceholder("G");
        b.setPlaceholder("B");
        a.setPlaceholder("A");

        refresh();
    }

    public void render(DrawContext ctx, int z, float centerX, float topY, float pW, float pH, float radius, float fieldW, ThemePalette palette) {
        TextRenderer tr = minecraftService.getClient().textRenderer;

        renderTitle(ctx, z, centerX, topY, tr, palette);

        float pickerCenterY = topY + tr.fontHeight + 6f + radius;
        picker.updateRenderForParent(ctx, centerX / pW, pickerCenterY / pH, pW, pH, z, radius, palette.outline, 1.5f);

        float hexY = pickerCenterY + radius + 10f;
        float hexX = centerX - fieldW / 2f;
        hex.updateRenderForParent(ctx, hexX / pW, hexY / pH, fieldW, 18f, pW, pH, z,
                8f, palette.surface, palette.outline, palette.textPrimary, palette.textMuted, palette.primary, 1.5f, 2f);

        float chanY = hexY + 24f;
        float gap = 3f;
        float cw = (fieldW - gap * 3f) / 4f;
        SearchDrawableElement[] channels = {r, g, b, a};
        for (int i = 0; i < channels.length; i++) {
            float cx = hexX + i * (cw + gap);
            channels[i].updateRenderForParent(ctx, cx / pW, chanY / pH, cw, 18f, pW, pH, z,
                    6f, palette.surface, palette.outline, palette.textPrimary, palette.textMuted, palette.primary, 1.5f, 2f);
        }

        renderStatus(ctx, z, centerX, chanY + 26f, tr, palette);
    }

    public boolean handleClick(float pW, float pH, double mouseX, double mouseY) {
        if (picker.handleClick(new ScreenCtx(pW, pH, mouseX, mouseY))) {
            unfocusFields();
            return true;
        }

        boolean over = false;
        for (SearchDrawableElement field : fields()) {
            boolean o = field.isMouseOver(pW, pH, mouseX, mouseY);
            field.setFocused(o);
            if (o) over = true;
        }
        return over;
    }

    public void handleDrag(float pW, float pH, double mouseX, double mouseY) {
        picker.handleDrag(pW, pH, mouseX, mouseY);
    }

    public void handleRelease() {
        picker.handleRelease();
        commit.run();
    }

    public boolean onCharTyped(char c) {
        for (SearchDrawableElement field : fields()) {
            if (field.onCharTyped(c)) return true;
        }
        return false;
    }

    public boolean onKeyPress(int key, int scancode, int action, int modifiers) {
        for (SearchDrawableElement field : fields()) {
            if (field.onKeyPress(key, scancode, action, modifiers)) return true;
        }
        return false;
    }

    private SearchDrawableElement[] fields() {
        return new SearchDrawableElement[]{hex, r, g, b, a};
    }

    private void unfocusFields() {
        for (SearchDrawableElement field : fields()) {
            field.setFocused(false);
        }
    }

    private void onPicker(Color color) {
        setter.accept(color);
        refresh();
    }

    private void onHex(String value) {
        Color color = parseHex(value);
        if (color == null) return;
        setter.accept(color);
        refresh();
        commit.run();
    }

    private void onChannel(String value, int channel) {
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return;
        try {
            int v = Integer.parseInt(trimmed);
            if (v < 0 || v > 255) return;
            Color cur = getter.get();
            Color next = new Color(
                    channel == 0 ? v : cur.getRed(),
                    channel == 1 ? v : cur.getGreen(),
                    channel == 2 ? v : cur.getBlue(),
                    channel == 3 ? v : cur.getAlpha());
            setter.accept(next);
            refresh();
            commit.run();
        } catch (NumberFormatException ignored) {
        }
    }

    private void refresh() {
        if (syncing) return;
        syncing = true;
        try {
            Color c = getter.get();
            picker.setSelectedColor(c);
            hex.setQuerySilent(hexText(c));
            r.setQuerySilent(String.valueOf(c.getRed()));
            g.setQuerySilent(String.valueOf(c.getGreen()));
            b.setQuerySilent(String.valueOf(c.getBlue()));
            a.setQuerySilent(String.valueOf(c.getAlpha()));
        } finally {
            syncing = false;
        }
    }

    private void renderTitle(DrawContext ctx, int z, float centerX, float topY, TextRenderer tr, ThemePalette palette) {
        int w = tr.getWidth(title);
        render2DService.renderText(tr, Text.literal(title).asOrderedText(),
                (int) (centerX - w / 2f), (int) topY, z, palette.textPrimary.getRGB(), false, ctx);
    }

    private void renderStatus(DrawContext ctx, int z, float centerX, float y, TextRenderer tr, ThemePalette palette) {
        Color c = getter.get();
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        String status = "%s  RGB(%d,%d,%d)  HSB(%d°,%d%%,%d%%)".formatted(
                hexText(c), c.getRed(), c.getGreen(), c.getBlue(),
                Math.round(hsb[0] * 360f), Math.round(hsb[1] * 100f), Math.round(hsb[2] * 100f));
        int w = tr.getWidth(status);
        render2DService.renderText(tr, Text.literal(status).asOrderedText(),
                (int) (centerX - w / 2f), (int) y, z, palette.textMuted.getRGB(), false, ctx);
    }

    private static String hexText(Color c) {
        if (c.getAlpha() == 255) {
            return String.format("#%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue());
        }
        return String.format("#%02X%02X%02X%02X", c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
    }

    private Color parseHex(String value) {
        String t = value.trim().replace("#", "");
        try {
            if (t.length() == 6) {
                int rgb = Integer.parseInt(t, 16);
                Color cur = getter.get();
                return new Color((rgb >> 16) & 0xFF, (rgb >> 8) & 0xFF, rgb & 0xFF, cur.getAlpha());
            }
            if (t.length() == 8) {
                long rgba = Long.parseLong(t, 16);
                return new Color((int) ((rgba >> 24) & 0xFF), (int) ((rgba >> 16) & 0xFF),
                        (int) ((rgba >> 8) & 0xFF), (int) (rgba & 0xFF));
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }
}
