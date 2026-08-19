package me.yuugao.holymoderation.client.gui.tabs.impl.main;

import me.yuugao.holymoderation.client.di.DIAccessor;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.DropdownDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.SearchDrawableElement;
import me.yuugao.holymoderation.client.gui.screen.impl.MainGuiScreen;
import me.yuugao.holymoderation.client.gui.tabs.SettingsTab;
import me.yuugao.holymoderation.client.modules.impl.NetModule;
import me.yuugao.holymoderation.client.util.factory.DrawableElementFactory;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.NetService;
import me.yuugao.holymoderation.client.util.service.Render2DService;
import me.yuugao.holymoderation.client.util.service.ThemePalette;
import me.yuugao.holymoderation.client.util.service.ThemeService;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.config.impl.ApiConfig;
import me.yuugao.holymoderation.client.util.service.state.PlayerStateService;
import me.yuugao.holymoderation.client.util.service.state.UserStateService;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.List;
import java.util.Map;

public class JournalTab extends SettingsTab {
    private static final List<String> REASONS = List.of(
            "report", "checkout", "autobuy", "autosell", "customka", "personal", "toManyChecks", "candidate");
    private static final List<String> MODES = List.of("lite", "lite120", "classic", "lpvp");

    private final NetModule netModule;
    private final NetService netService;
    private final PlayerStateService playerStateService;

    private final SearchDrawableElement playerField;
    private final DropdownDrawableElement reasonDropdown;
    private final DropdownDrawableElement modeDropdown;
    private final SearchDrawableElement numberField;

    public JournalTab(MainGuiScreen parent, ThemeService themeService, ConfigManagerService configManagerService,
                      MinecraftService minecraftService, Render2DService render2DService, DrawableElementFactory factory) {
        super(parent, themeService, configManagerService, minecraftService, render2DService, factory);

        this.netModule = DIAccessor.getDI().get(NetModule.class);
        this.netService = DIAccessor.getDI().get(NetService.class);
        this.playerStateService = DIAccessor.getDI().get(PlayerStateService.class);

        ApiConfig apiConfig = configManagerService.getApiConfig();
        UserStateService userStateService = DIAccessor.getDI().get(UserStateService.class);

        addTextField("API-ключ журнала", "вставьте токен", apiConfig.getApiToken(), s -> {
            apiConfig.setApiToken(s == null ? "" : s.trim());
            configManagerService.saveConfig(apiConfig);
        });

        addButton("Обновить данные", netModule::refresh);

        this.playerField = addTextField("Ник игрока", "Ник игрока", playerStateService.getCheckoutPlayer(), s -> {});
        addButton("Подставить активную", this::fillPlayer);

        this.reasonDropdown = addDropdown("Причина", REASONS, 0, s -> {});
        this.modeDropdown = addDropdown("Режим", MODES, defaultModeIndex(userStateService), s -> {});

        this.numberField = addTextField("Номер режима", "", String.valueOf(defaultNumber(userStateService)), s -> {});
        this.numberField.setCentered(true);

        addButton("Внести проверку", this::startCheckout);
    }

    private void fillPlayer() {
        String cp = playerStateService.getCheckoutPlayer();
        if (cp.isEmpty()) {
            netModuleNotify("Нет активной проверки.");
            return;
        }
        playerField.setQuerySilent(cp);
    }

    private static int defaultModeIndex(UserStateService uss) {
        String loc = uss == null ? "" : uss.getUserLocation();
        if (loc.startsWith("lite120")) return MODES.indexOf("lite120");
        if (loc.startsWith("lite")) return MODES.indexOf("lite");
        if (loc.startsWith("classic")) return MODES.indexOf("classic");
        if (loc.equals("lpvp")) return MODES.indexOf("lpvp");
        return MODES.indexOf("classic");
    }

    private static int defaultNumber(UserStateService uss) {
        if (uss == null) return 1;
        String loc = uss.getUserLocation();
        if (loc.equals("lpvp")) return 1;
        int dash = loc.indexOf('-');
        if (dash >= 0 && dash + 1 < loc.length()) {
            try {
                return Integer.parseInt(loc.substring(dash + 1));
            } catch (NumberFormatException ignored) {
            }
        }
        return 1;
    }

    private void startCheckout() {
        String player = playerField.getQuery().trim();
        if (player.isEmpty()) {
            netModuleNotify("Укажите ник игрока.");
            return;
        }
        String reason = reasonDropdown.getSelected();
        String mode = modeDropdown.getSelected();
        int number;
        try {
            number = Integer.parseInt(numberField.getQuery().trim());
        } catch (NumberFormatException e) {
            netModuleNotify("Укажите номер режима числом.");
            return;
        }
        boolean pvp = "lpvp".equals(mode);
        String apiMode = pvp ? "lite" : mode;
        netService.startCheckout(player, reason, apiMode, number, pvp);
    }

    private void netModuleNotify(String text) {
        me.yuugao.holymoderation.client.di.DIAccessor.getDI()
                .get(me.yuugao.holymoderation.client.util.service.NotificationsService.class).error(text);
    }

    @Override
    protected float extraHeight() {
        ApiConfig apiConfig = configManagerService.getApiConfig();
        TextRenderer tr = minecraftService.getClient().textRenderer;
        if (apiConfig.getApiToken().isBlank()) {
            return tr.fontHeight + 8f;
        }
        int lines = 9 + (apiConfig.getJournalStats().isEmpty() ? 0 : 7);
        return lines * (tr.fontHeight + 2f) + 40f;
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

        float cardX = PAD;
        float cardW = pW - PAD * 2f;
        float pad = 10f;

        float profileH = 7 * (tr.fontHeight + 2f) + pad * 2f + tr.fontHeight;
        renderCard(ctx, z, palette, cardX, y, cardW, profileH);
        float ty = y + pad;

        renderText(ctx, z, "ПРОФИЛЬ", cardX + pad, ty, tr, palette.primaryBright);
        ty += tr.fontHeight + 6f;

        ty = renderKv(ctx, z, tr, cardX + pad, ty, palette, "Никнейм", str(profile.get("nickname")));
        ty = renderKv(ctx, z, tr, cardX + pad, ty, palette, "Должность", rankName(profile.get("rank")));
        ty = renderKv(ctx, z, tr, cardX + pad, ty, palette, "ВК", str(profile.get("fullname")));
        ty = renderKv(ctx, z, tr, cardX + pad, ty, palette, "Баланс", num(profile.get("neponyatki")));
        ty = renderKv(ctx, z, tr, cardX + pad, ty, palette, "Выговоры", num(profile.get("reprimands")));
        ty = renderKv(ctx, z, tr, cardX + pad, ty, palette, "Предупреждения", num(profile.get("warns")));
        renderKv(ctx, z, tr, cardX + pad, ty, palette, "Режим", str(profile.get("anarchyMode")));

        y += profileH + 10f;

        String profDate = apiConfig.getLastJournalProfileUpdate() == null ? "—" : apiConfig.getLastJournalProfileUpdate();
        renderText(ctx, z, "Профиль обновлён: " + profDate, cardX + pad, y, tr, palette.textMuted);
        y += tr.fontHeight + 4f;

        if (!stats.isEmpty()) {
            float statsH = 8 * (tr.fontHeight + 2f) + pad * 2f + tr.fontHeight;
            renderCard(ctx, z, palette, cardX, y, cardW, statsH);
            float sy = y + pad;

            renderText(ctx, z, "СТАТИСТИКА", cardX + pad, sy, tr, palette.primaryBright);
            sy += tr.fontHeight + 6f;

            sy = renderKv(ctx, z, tr, cardX + pad, sy, palette, "Мутов всего", num(stats.get("mutesAll")));
            sy = renderKv(ctx, z, tr, cardX + pad, sy, palette, "Мутов за месяц", num(stats.get("mutesMonth")));
            sy = renderKv(ctx, z, tr, cardX + pad, sy, palette, "Мутов за сегодня", num(stats.get("mutesToday")));
            sy = renderKv(ctx, z, tr, cardX + pad, sy, palette, "Гарантов всего", num(stats.get("gaurantsAll")));
            sy = renderKv(ctx, z, tr, cardX + pad, sy, palette, "Гарантов за месяц", num(stats.get("gaurantsMonth")));
            renderKv(ctx, z, tr, cardX + pad, sy, palette, "Гарантов за сегодня", num(stats.get("gaurantsToday")));

            y += statsH + 10f;

            String statDate = apiConfig.getLastJournalStatsUpdate() == null ? "—" : apiConfig.getLastJournalStatsUpdate();
            renderText(ctx, z, "Статистика обновлена: " + statDate, cardX + pad, y, tr, palette.textMuted);
        }
    }

    private float renderKv(DrawContext ctx, int z, TextRenderer tr, float x, float y, ThemePalette palette,
                           String key, String value) {
        renderText(ctx, z, key, x, y, tr, palette.textSecondary);
        int kw = tr.getWidth(key);
        renderText(ctx, z, value, x + 110f, y, tr, palette.textPrimary);
        return y + tr.fontHeight + 2f;
    }

    private void renderCard(DrawContext ctx, int z, ThemePalette palette, float x, float y, float w, float h) {
        render2DService.renderSoftRoundedRect(ctx.getMatrices(), x, y, w, h, z, 10f, palette.surface, 0);
        render2DService.renderSoftRoundedRectOutline(ctx.getMatrices(), x, y, w, h, z, 10f, palette.surface, palette.outline, 1f, 1f);
    }

    private void renderText(DrawContext ctx, int z, String text, float x, float y, TextRenderer tr, Color color) {
        render2DService.renderText(tr, Text.literal(text).asOrderedText(),
                (int) x, (int) y, z, color.getRGB(), false, ctx);
    }

    private static String str(Object o) {
        return o == null ? "—" : String.valueOf(o);
    }

    private static String num(Object o) {
        try {
            return String.valueOf((long) Double.parseDouble(o.toString()));
        } catch (Exception e) {
            return "—";
        }
    }

    private static String rankName(Object rank) {
        if (rank == null) return "—";
        int r = (int) Double.parseDouble(rank.toString());
        return me.yuugao.holymoderation.client.util.service.ChatService.stripColor(
                NetModule.RANKS.getOrDefault(r, String.valueOf(r)));
    }
}
