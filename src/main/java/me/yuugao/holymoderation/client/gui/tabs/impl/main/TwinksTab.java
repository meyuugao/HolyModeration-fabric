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
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import net.minecraft.client.util.math.MatrixStack;
import org.joml.Matrix4f;

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

    private static final Color BAN_COLOR = new Color(224, 96, 96);
    private static final Color MUTE_COLOR = new Color(235, 190, 70);
    private static final Color KICK_COLOR = new Color(230, 140, 70);
    private static final Color BLOP_COLOR = new Color(92, 204, 130);

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
    private float contentScroll = 0f;
    private float contentMax = 0f;
    private float fileScroll = 0f;
    private float fileMax = 0f;
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
        contentScroll = 0f;
    }

    @Override
    public void onRender(DrawContext ctx, int relMouseX, int relMouseY, float tickDelta) {
        ThemePalette palette = themeService.getPalette();
        float pW = parent.getWidth();
        float pH = parent.getHeight();
        int z = parent.getRenderPriority();

        scanFiles();

        filter.updateRenderForParent(ctx, PAD / pW, 36f / pH, LEFT_W, 20f, pW, pH, z,
                9f, palette.surface, palette.outline, palette.textPrimary, palette.textMuted, palette.primary, 1.5f, 2f);

        runButton.updateRenderForParent(ctx, PAD / pW, 62f / pH, LEFT_W, pW, pH, z,
                8f, palette.primary, palette.primaryBright, 1.5f, 2f);

        renderFileList(ctx, z, palette, pW, pH);
        renderContent(ctx, z, palette, pW, pH);
    }

    private void renderFileList(DrawContext ctx, int z, ThemePalette palette, float pW, float pH) {
        fileHitboxes.clear();
        TextRenderer tr = minecraftService.getClient().textRenderer;
        List<FileEntry> visible = visibleFiles();

        float listTop = 90f;
        float listBottom = pH - 10f;
        float listH = listBottom - listTop;

        float totalH = visible.size() * 24f;
        fileMax = Math.max(0f, totalH - listH);
        fileScroll = Math.max(0f, Math.min(fileScroll, fileMax));

        scissor(ctx, PAD, listTop, PAD + LEFT_W, listBottom);

        for (int i = 0; i < visible.size(); i++) {
            FileEntry f = visible.get(i);
            float y = listTop + i * 24f - fileScroll;
            if (y + 20f < listTop || y > listBottom) continue;

            boolean selected = i == selectedIndex;
            render2DService.renderSoftRoundedRect(ctx.getMatrices(), PAD, y, LEFT_W, 20f, z,
                    6f, selected ? palette.primary : palette.surface, 0);
            if (selected) {
                render2DService.renderSoftRoundedRectOutline(ctx.getMatrices(), PAD, y, LEFT_W, 20f, z,
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
        float top = 36f;
        float bottom = pH - 10f;

        scissor(ctx, x, top, x + w, bottom);

        if (entries.isEmpty()) {
            render2DService.renderText(tr, Text.literal(selectedName.isBlank() ? "Выберите файл слева." : "Нет данных.").asOrderedText(),
                    (int) x, (int) (top + 6f), z, palette.textMuted.getRGB(), false, ctx);
            ctx.disableScissor();
            contentMax = 0f;
            return;
        }

        float y = top + 4f - contentScroll;
        for (TwinksCheckModule.PlayerEntry e : entries) {
            y = renderPlayerCard(ctx, z, palette, tr, x, w, y, e);
            y += 10f;
        }

        contentMax = Math.max(0f, (y + contentScroll) - bottom);
        contentScroll = Math.max(0f, Math.min(contentScroll, contentMax));

        ctx.disableScissor();
    }

    private float renderPlayerCard(DrawContext ctx, int z, ThemePalette palette, TextRenderer tr,
                                   float x, float w, float y, TwinksCheckModule.PlayerEntry e) {
        render2DService.renderSoftRoundedRect(ctx.getMatrices(), x, y, w, 24f, z, 8f, palette.surface, 0);

        render2DService.renderText(tr, Text.literal(e.nickname).asOrderedText(),
                (int) (x + 10f), (int) (y + 7f), z, palette.textPrimary.getRGB(), false, ctx);

        float chipX = x + w - 8f;
        chipX = renderChip(ctx, z, tr, chipX, y, "ЧСП", e.isInBLOP, BLOP_COLOR, palette);
        chipX = renderChip(ctx, z, tr, chipX, y, "Мут", e.isMuted, MUTE_COLOR, palette);
        chipX = renderChip(ctx, z, tr, chipX, y, "Бан", e.isBanned, BAN_COLOR, palette);

        y += 27f;

        List<TwinksCheckModule.PunishmentEntry> list = e.fullHistory.isEmpty() ? e.recentHistory : e.fullHistory;
        for (TwinksCheckModule.PunishmentEntry p : list) {
            y = renderPunishment(ctx, z, palette, tr, x, w, y, p);
        }

        return y;
    }

    private float renderChip(DrawContext ctx, int z, TextRenderer tr, float rightX, float cardY,
                             String label, boolean active, Color color, ThemePalette palette) {
        int lw = tr.getWidth(label);
        float w = lw + 10f;
        float h = 14f;
        float x = rightX - w - 4f;
        float y = cardY + 5f;

        Color bg = active ? color : palette.surface;
        render2DService.renderSoftRoundedRect(ctx.getMatrices(), x, y, w, h, z, h / 2f, bg, 0);
        render2DService.renderText(tr, Text.literal(label).asOrderedText(),
                (int) (x + 5f), (int) (y + (h - tr.fontHeight) / 2f + 1f), z,
                (active ? palette.onPrimary : palette.textMuted).getRGB(), false, ctx);
        return x;
    }

    private float renderPunishment(DrawContext ctx, int z, ThemePalette palette, TextRenderer tr,
                                   float x, float w, float y, TwinksCheckModule.PunishmentEntry p) {
        Color typeColor = switch (p.type()) {
            case BAN -> BAN_COLOR;
            case MUTE -> MUTE_COLOR;
            case KICK -> KICK_COLOR;
        };
        String typeLabel = switch (p.type()) {
            case BAN -> "БАН";
            case MUTE -> "МУТ";
            case KICK -> "КИК";
        };

        float dotX = x + 10f;
        render2DService.renderSoftRoundedRect(ctx.getMatrices(), dotX, y + 6f, 6f, 6f, z, 3f, typeColor, 0);

        render2DService.renderText(tr, Text.literal(typeLabel).asOrderedText(),
                (int) (dotX + 10f), (int) (y + 4f), z, typeColor.getRGB(), false, ctx);

        String meta = "%s · %s".formatted(p.timeAgo(), p.by());
        render2DService.renderText(tr, Text.literal(meta).asOrderedText(),
                (int) (dotX + 10f + tr.getWidth(typeLabel) + 8f), (int) (y + 4f), z, palette.textMuted.getRGB(), false, ctx);

        y += tr.fontHeight + 2f;

        String reason = p.reason().isBlank() ? "—" : p.reason();
        int wrapWidth = Math.max(20, (int) (w - 26f));
        List<OrderedText> lines = tr.wrapLines(Text.literal(reason), wrapWidth);
        for (OrderedText line : lines) {
            render2DService.renderText(tr, line, (int) (dotX + 10f), (int) y, z, palette.textSecondary.getRGB(), false, ctx);
            y += tr.fontHeight + 1f;
        }

        return y + 3f;
    }

    private void scissor(DrawContext ctx, float x, float y, float x2, float y2) {
        MatrixStack ms = ctx.getMatrices();
        float[] a = transformPoint(ms, x, y);
        float[] b = transformPoint(ms, x2, y2);
        ctx.enableScissor((int) a[0], (int) a[1], (int) Math.ceil(b[0]), (int) Math.ceil(b[1]));
    }

    private static float[] transformPoint(MatrixStack ms, float x, float y) {
        Matrix4f m = ms.peek().getPositionMatrix();
        return new float[]{
                m.m00() * x + m.m10() * y + m.m20(),
                m.m01() * x + m.m11() * y + m.m21()
        };
    }

    @Override
    public boolean onMouseClick(float mouseX, float mouseY) {
        float pW = parent.getWidth();
        float pH = parent.getHeight();

        boolean overFilter = filter.isMouseOver(pW, pH, mouseX, mouseY);
        filter.setFocused(overFilter);
        if (overFilter) {
            filter.handleClick(pW, pH, mouseX, mouseY);
            return true;
        }

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
            contentScroll -= (float) (dy * 22f);
            contentScroll = Math.max(0f, Math.min(contentScroll, contentMax));
        } else {
            fileScroll -= (float) (dy * 22f);
            fileScroll = Math.max(0f, Math.min(fileScroll, fileMax));
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
