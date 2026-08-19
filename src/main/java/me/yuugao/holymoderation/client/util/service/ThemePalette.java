package me.yuugao.holymoderation.client.util.service;

import java.awt.Color;

public class ThemePalette {
    public final Color background;
    public final Color surface;
    public final Color surfaceElevated;
    public final Color outline;
    public final Color primary;
    public final Color primaryBright;
    public final Color primaryDark;
    public final Color onPrimary;
    public final Color textPrimary;
    public final Color textSecondary;
    public final Color textMuted;
    public final Color track;
    public final Color selection;
    public final Color disabled;
    public final Color success;
    public final Color danger;

    private ThemePalette(Color background, Color surface, Color surfaceElevated, Color outline,
                         Color primary, Color primaryBright, Color primaryDark, Color onPrimary,
                         Color textPrimary, Color textSecondary, Color textMuted, Color track,
                         Color selection, Color disabled, Color success, Color danger) {
        this.background = background;
        this.surface = surface;
        this.surfaceElevated = surfaceElevated;
        this.outline = outline;
        this.primary = primary;
        this.primaryBright = primaryBright;
        this.primaryDark = primaryDark;
        this.onPrimary = onPrimary;
        this.textPrimary = textPrimary;
        this.textSecondary = textSecondary;
        this.textMuted = textMuted;
        this.track = track;
        this.selection = selection;
        this.disabled = disabled;
        this.success = success;
        this.danger = danger;
    }

    public static ThemePalette from(Color main, Color second) {
        Color background = main;
        Color surface = lighten(main, 0.14f);
        Color surfaceElevated = gradient(main, second, 0.16f);
        Color outline = gradient(main, second, 0.55f);
        Color primary = second;
        Color primaryBright = lighten(second, 0.18f);
        Color primaryDark = darken(second, 0.18f);
        Color onPrimary = readableOn(second);
        Color textPrimary = new Color(245, 245, 248);
        Color textSecondary = new Color(198, 201, 212);
        Color textMuted = new Color(148, 152, 163);
        Color track = gradient(main, second, 0.32f);
        Color selection = withAlpha(gradient(main, second, 0.62f), 130);
        Color disabled = new Color(96, 100, 108);
        Color success = new Color(92, 204, 130);
        Color danger = new Color(224, 96, 96);

        return new ThemePalette(background, surface, surfaceElevated, outline,
                primary, primaryBright, primaryDark, onPrimary,
                textPrimary, textSecondary, textMuted, track,
                selection, disabled, success, danger);
    }

    public static Color gradient(Color a, Color b, float t) {
        float tClamped = Math.max(0f, Math.min(1f, t));
        float[] ha = Color.RGBtoHSB(a.getRed(), a.getGreen(), a.getBlue(), null);
        float[] hb = Color.RGBtoHSB(b.getRed(), b.getGreen(), b.getBlue(), null);
        float hue = lerpHue(ha[0], hb[0], tClamped);
        float saturation = lerp(ha[1], hb[1], tClamped);
        float brightness = lerp(ha[2], hb[2], tClamped);
        return Color.getHSBColor(hue, saturation, brightness);
    }

    public static Color lighten(Color color, float amount) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return Color.getHSBColor(hsb[0], Math.max(0f, Math.min(1f, hsb[1] * (1f - amount * 0.6f))),
                Math.max(0f, Math.min(1f, hsb[2] + (1f - hsb[2]) * amount)));
    }

    public static Color darken(Color color, float amount) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return Color.getHSBColor(hsb[0], Math.max(0f, Math.min(1f, hsb[1])),
                Math.max(0f, Math.min(1f, hsb[2] * (1f - amount))));
    }

    public static Color withAlpha(Color color, int alpha) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), Math.max(0, Math.min(255, alpha)));
    }

    public static Color readableOn(Color background) {
        double luminance = (0.299 * background.getRed() + 0.587 * background.getGreen() + 0.114 * background.getBlue()) / 255.0;
        return luminance > 0.6 ? new Color(24, 26, 32) : new Color(255, 255, 255);
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    private static float lerpHue(float a, float b, float t) {
        float delta = b - a;
        if (delta > 0.5f) delta -= 1f;
        if (delta < -0.5f) delta += 1f;
        float hue = a + delta * t;
        if (hue < 0f) hue += 1f;
        if (hue > 1f) hue -= 1f;
        return hue;
    }
}
