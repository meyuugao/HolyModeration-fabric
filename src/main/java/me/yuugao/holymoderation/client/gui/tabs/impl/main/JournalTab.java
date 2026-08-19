package me.yuugao.holymoderation.client.gui.tabs.impl.main;

import me.yuugao.holymoderation.client.di.DIAccessor;
import me.yuugao.holymoderation.client.gui.screen.impl.MainGuiScreen;
import me.yuugao.holymoderation.client.gui.tabs.SettingsTab;
import me.yuugao.holymoderation.client.modules.impl.NetModule;
import me.yuugao.holymoderation.client.util.factory.DrawableElementFactory;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.Render2DService;
import me.yuugao.holymoderation.client.util.service.ThemePalette;
import me.yuugao.holymoderation.client.util.service.ThemeService;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.config.impl.ApiConfig;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JournalTab extends SettingsTab {
    private final NetModule netModule;

    public JournalTab(MainGuiScreen parent, ThemeService themeService, ConfigManagerService configManagerService,
                      MinecraftService minecraftService, Render2DService render2DService, DrawableElementFactory factory) {
        super(parent, themeService, configManagerService, minecraftService, render2DService, factory);

        this.netModule = DIAccessor.getDI().get(NetModule.class);

        ApiConfig apiConfig = configManagerService.getApiConfig();

        addTextField("API-ключ журнала", "вставьте токен", apiConfig.getApiToken(), s -> {
            apiConfig.setApiToken(s == null ? "" : s.trim());
            configManagerService.saveConfig(apiConfig);
        });

        addButton("Обновить данные", netModule::refresh);
    }

    @Override
    protected float extraHeight() {
        ApiConfig apiConfig = configManagerService.getApiConfig();
        TextRenderer tr = minecraftService.getClient().textRenderer;
        if (apiConfig.getApiToken().isBlank() || apiConfig.getJournalProfile().isEmpty()) {
            return tr.fontHeight + 8f;
        }
        int lines = 9 + (apiConfig.getJournalStats().isEmpty() ? 0 : 6);
        return lines * (tr.fontHeight + 2f) + 8f;
    }

    @Override
    protected void renderExtra(DrawContext ctx, ThemePalette palette, float pW, float pH, int z, float scroll) {
        ApiConfig apiConfig = configManagerService.getApiConfig();
        TextRenderer tr = minecraftService.getClient().textRenderer;

        float y = rowsEndY() - scroll + 8f;

        if (apiConfig.getApiToken().isBlank()) {
            renderText(ctx, z, "Установите API-ключ из журнала, чтобы разблокировать вкладку.", PAD, y, tr, palette.textMuted);
            return;
        }

        Map<String, Object> profile = apiConfig.getJournalProfile();
        Map<String, Object> stats = apiConfig.getJournalStats();

        if (profile.isEmpty()) {
            renderText(ctx, z, "Данные ещё не загружены — нажмите «Обновить данные».", PAD, y, tr, palette.textMuted);
            return;
        }

        List<String> lines = new ArrayList<>();
        lines.add("Профиль:");
        lines.add("  Ник: " + profile.get("nickname"));
        Object rank = profile.get("rank");
        if (rank != null) {
            int r = (int) Double.parseDouble(rank.toString());
            String rankName = NetModule.RANKS.getOrDefault(r, String.valueOf(r));
            lines.add("  Должность: " + me.yuugao.holymoderation.client.util.service.ChatService.stripColor(rankName));
        }
        lines.add("  ВК: " + profile.get("fullname"));
        lines.add("  Баланс: " + (int) Double.parseDouble(profile.get("neponyatki").toString()));
        lines.add("  Выговоры: " + (int) Double.parseDouble(profile.get("reprimands").toString()));
        lines.add("  Предупреждения: " + (int) Double.parseDouble(profile.get("warns").toString()));
        lines.add("  Режим: " + profile.get("anarchyMode"));

        if (!stats.isEmpty()) {
            lines.add("");
            lines.add("Статистика:");
            lines.add("  Проверок всего: " + num(stats.get("mutesAll")) + " мутов, " + num(stats.get("gaurantsAll")) + " гарантов");
            lines.add("  Мутов за месяц: " + num(stats.get("mutesMonth")) + ", за сегодня: " + num(stats.get("mutesToday")));
            lines.add("  Гарантов за месяц: " + num(stats.get("gaurantsMonth")) + ", за сегодня: " + num(stats.get("gaurantsToday")));
        }

        for (String line : lines) {
            Color color = line.startsWith("  ") ? palette.textSecondary : palette.textPrimary;
            renderText(ctx, z, line, PAD, y, tr, color);
            y += tr.fontHeight + 2f;
        }
    }

    private static long num(Object o) {
        try {
            return (long) Double.parseDouble(o.toString());
        } catch (Exception e) {
            return 0;
        }
    }

    private void renderText(DrawContext ctx, int z, String text, float x, float y, TextRenderer tr, Color color) {
        render2DService.renderText(tr, Text.literal(text).asOrderedText(),
                (int) x, (int) y, z, color.getRGB(), false, ctx);
    }
}
