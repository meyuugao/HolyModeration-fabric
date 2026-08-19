package me.yuugao.holymoderation.client.gui.tabs.impl.main;

import me.yuugao.holymoderation.client.di.DIAccessor;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.SearchDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.button.impl.TextButtonDrawableElement;
import me.yuugao.holymoderation.client.gui.screen.impl.MainGuiScreen;
import me.yuugao.holymoderation.client.gui.tabs.Tab;
import me.yuugao.holymoderation.client.modules.impl.TwinksCheckModule;
import me.yuugao.holymoderation.client.util.factory.DrawableElementFactory;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.Render2DService;
import me.yuugao.holymoderation.client.util.service.ThemePalette;
import me.yuugao.holymoderation.client.util.service.ThemeService;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.awt.Color;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

public class TwinksTab extends Tab<MainGuiScreen> {
    private static final float LEFT_W = 170f;
    private static final float PAD = 14f;

    private final ThemeService themeService;
    private final MinecraftService minecraftService;
    private final Render2DService render2DService;
    private final TwinksCheckModule twinksCheckModule;

    private final TextButtonDrawableElement runButton;
    private final SearchDrawableElement filter;

    private final List<FileEntry> files = new ArrayList<>();
    private final List<float[]> fileHitboxes = new ArrayList<>();
    private List<TwinksCheckModule.PlayerEntry> entries = new ArrayList<>();
    private String selectedName = "";
    private int selectedIndex = -1;
    private float scroll = 0f;
    private float maxScroll = 0f;
    private long lastDirScan = 0L;

    private record FileEntry(String name, String shortName, Path path, long modified) {
    }

    public TwinksTab(MainGuiScreen parent, ThemeService themeService, ConfigManagerService configManagerService,
                     MinecraftService minecraftService, Render2DService render2DService, DrawableElementFactory factory) {
        super(parent);

        this.themeService = themeService;
        this.minecraftService = minecraftService;
        this.render2DService = render2DService;
        this.twinksCheckModule = DIAccessor.getDI().get(TwinksCheckModule.class);

        this.runButton = factory.createTextButton(me.yuugao.holymoderation.client.gui.drawable.render.PivotMode.LEFT_UP,
                this::runCheck, true, Text.literal("Запустить проверку"));
        this.filter = factory.createSearch(null);
        this.filter.setPlaceholder("Фильтр файлов...");
    }

    private void runCheck() {
        twinksCheckModule.runCheck();
    }

    private void scanFiles() {
        long now = System.currentTimeMillis();
        if (now - lastDirScan < 2000 && !files.isEmpty()) return;
        lastDirScan = now;

        files.clear();
        Path dir = TwinksCheckModule.getWorkDir();
        if (dir == null || !Files.exists(dir)) return;

        try (Stream<Path> stream = Files.list(dir)) {
            stream.filter(p -> p.getFileName().toString().endsWith(".txt"))
                    .filter(p -> !p.getFileName().toString().equals("checktwinks.txt"))
                    .filter(p -> !p.getFileName().toString().equals("temp.txt"))
                    .forEach(p -> {
                        long modified = 0L;
                        try {
                            modified = Files.getLastModifiedTime(p).toMillis();
                        } catch (IOException ignored) {
                        }
                        String name = p.getFileName().toString();
                        String shortName = name.length() > 10 ? name.substring(0, 10) + "…" : name;
                        files.add(new FileEntry(name, shortName, p, modified));
                    });
        } catch (IOException ignored) {
        }

        files.sort(Comparator.comparingLong(FileEntry::modified).reversed());
    }

    private List<FileEntry> visibleFiles() {
        String q = filter.getQuery().trim().toLowerCase(Locale.ROOT);
        if (q.isEmpty()) return files;
        List<FileEntry> out = new ArrayList<>();
        for (FileEntry f : files) {
            if (f.name.toLowerCase(Locale.ROOT).contains(q) || f.shortName.toLowerCase(Locale.ROOT).contains(q)) {
                out.add(f);
            }
        }
        return out;
    }

    private void select(int index) {
        List<FileEntry> visible = visibleFiles();
        if (index < 0 || index >= visible.size()) return;
        FileEntry f = visible.get(index);
        selectedIndex = index;
        selectedName = f.name;
        entries = TwinksCheckModule.parseResultsFile(f.path);
        scroll = 0f;
    }

    @Override
    public void onRender(DrawContext ctx, int relMouseX, int relMouseY, float tickDelta) {
        ThemePalette palette = themeService.getPalette();
        float pW = parent.getWidth();
        float pH = parent.getHeight();
        int z = parent.getRenderPriority();

        scanFiles();

        filter.updateRenderForParent(ctx, PAD / pW, 30f / pH, LEFT_W, 20f, pW, pH, z,
                9f, palette.surface, palette.outline, palette.textPrimary, palette.textMuted, palette.primary, 1.5f, 2f);

        runButton.updateRenderForParent(ctx, PAD / pW, 56f / pH, LEFT_W, pW, pH, z,
                8f, palette.primary, palette.primaryBright, 1.5f, 2f);

        renderFileList(ctx, z, palette, pW, pH);

        renderContent(ctx, z, palette, pW, pH);
    }

    private void renderFileList(DrawContext ctx, int z, ThemePalette palette, float pW, float pH) {
        fileHitboxes.clear();
        TextRenderer tr = minecraftService.getClient().textRenderer;
        List<FileEntry> visible = visibleFiles();

        float listTop = 84f;
        float listH = pH - listTop - 8f;
        int visibleCount = Math.max(0, (int) ((listH) / 24f));

        float fileScroll = 0f;
        float totalH = visible.size() * 24f;
        if (totalH > listH) {
            fileScroll = Math.max(0f, Math.min(scroll * 0.2f, totalH - listH));
        }

        ctx.enableScissor((int) (parent.getX()), (int) (parent.getY() + listTop), (int) (parent.getX() + PAD + LEFT_W), (int) (parent.getY() + listTop + listH));

        for (int i = 0; i < visible.size(); i++) {
            FileEntry f = visible.get(i);
            float y = listTop + i * 24f - fileScroll;
            if (y + 20f < listTop || y > listTop + listH) continue;

            boolean selected = i == selectedIndex;
            render2DService.renderSoftRoundedRect(ctx, PAD, y, LEFT_W, 20f, z,
                    6f, selected ? palette.primary : palette.surface, 0);
            if (selected) {
                render2DService.renderSoftRoundedRectOutline(ctx, PAD, y, LEFT_W, 20f, z,
                        6f, palette.primary, palette.primaryBright, 1.2f, 1f);
            }
            render2DService.renderText(tr, Text.literal(f.shortName).asOrderedText(),
                    (int) (PAD + 8f), (int) (y + 5f), z,
                    (selected ? palette.onPrimary : palette.textPrimary).getRGB(), false, ctx);

            fileHitboxes.add(new float[]{PAD, y, LEFT_W, 20f, i});
        }

        ctx.disableScissor();
    }

    private void renderContent(DrawContext ctx, int z, ThemePalette palette, float pW, float pH) {
        TextRenderer tr = minecraftService.getClient().textRenderer;
        float x = PAD + LEFT_W + 14f;
        float w = pW - x - PAD;
        float top = 30f;
        float bottom = pH - 8f;

        ctx.enableScissor((int) (parent.getX() + x), (int) (parent.getY() + top), (int) (parent.getX() + x + w), (int) (parent.getY() + bottom));

        if (entries.isEmpty()) {
            render2DService.renderText(tr, Text.literal(selectedName.isBlank() ? "Выберите файл слева." : "Нет данных.").asOrderedText(),
                    (int) x, (int) (top + 6f), z, palette.textMuted.getRGB(), false, ctx);
            ctx.disableScissor();
            return;
        }

        float y = top - scroll;
        for (TwinksCheckModule.PlayerEntry e : entries) {
            y = renderPlayerCard(ctx, z, palette, tr, x, w, y, e);
            y += 10f;
        }

        maxScroll = Math.max(0f, (y + scroll) - bottom);
        ctx.disableScissor();
    }

    private float renderPlayerCard(DrawContext ctx, int z, ThemePalette palette, TextRenderer tr,
                                   float x, float w, float y, TwinksCheckModule.PlayerEntry e) {
        Color ban = new Color(224, 96, 96);
        Color mute = new Color(235, 190, 70);
        Color kick = new Color(230, 140, 70);
        Color ok = new Color(92, 204, 130);

        render2DService.renderSoftRoundedRect(ctx, x, y, w, 24f, z, 8f, palette.surface, 0);

        render2DService.renderText(tr, Text.literal(e.nickname).asOrderedText(),
                (int) (x + 10f), (int) (y + 7f), z, palette.textPrimary.getRGB(), false, ctx);

        String badges = (e.isBanned ? "BANNED  " : "") + (e.isInBLOP ? "IN_BLOP  " : "") + (e.historyFound ? "" : "NO_HISTORY");
        if (!badges.isBlank()) {
            int bw = tr.getWidth(badges);
            render2DService.renderText(tr, Text.literal(badges).asOrderedText(),
                    (int) (x + w - bw - 10f), (int) (y + 7f), z,
                    (e.isBanned ? ban : e.isInBLOP ? mute : palette.textMuted).getRGB(), false, ctx);
        }

        y += 26f;

        List<TwinksCheckModule.PunishmentEntry> list = e.fullHistory.isEmpty() ? e.recentHistory : e.fullHistory;
        for (TwinksCheckModule.PunishmentEntry p : list) {
            if (y > parent.getHeight()) break;
            String reason = p.reason();
            if (reason.length() > 48) reason = reason.substring(0, 48) + "…";
            String line = "%s · %s · %s".formatted(p.by(), p.timeAgo(), reason);

            Color typeColor = switch (p.type()) {
                case BAN -> ban;
                case MUTE -> mute;
                case KICK -> kick;
            };

            render2DService.renderText(tr, Text.literal("▪").asOrderedText(),
                    (int) (x + 10f), (int) (y + 4f), z, typeColor.getRGB(), false, ctx);
            render2DService.renderText(tr, Text.literal(line).asOrderedText(),
                    (int) (x + 26f), (int) (y + 4f), z, palette.textSecondary.getRGB(), false, ctx);
            y += tr.fontHeight + 3f;
        }

        return y;
    }

    @Override
    public boolean onMouseClick(float mouseX, float mouseY) {
        float pW = parent.getWidth();
        float pH = parent.getHeight();

        boolean overFilter = filter.isMouseOver(pW, pH, mouseX, mouseY);
        filter.setFocused(overFilter);
        if (overFilter) return true;

        if (runButton.hitInParent(pW, pH, mouseX, mouseY)) return true;

        for (float[] b : fileHitboxes) {
            if (mouseX >= b[0] && mouseX <= b[0] + b[2] && mouseY >= b[1] && mouseY <= b[1] + b[3]) {
                select((int) b[4]);
                return true;
            }
        }
        return false;
    }

    @Override
    public void onMouseScroll(double dx, double dy, float mouseX, float mouseY) {
        float pW = parent.getWidth();
        if (mouseX > PAD + LEFT_W) {
            scroll -= (float) (dy * 20f);
            scroll = Math.max(0f, Math.min(scroll, maxScroll));
        }
    }

    @Override
    public boolean onCharTyped(char chr) {
        return filter.onCharTyped(chr);
    }

    @Override
    public boolean onKeyPress(int key, int scancode, int action, int modifiers) {
        return filter.onKeyPress(key, scancode, action, modifiers);
    }
}
