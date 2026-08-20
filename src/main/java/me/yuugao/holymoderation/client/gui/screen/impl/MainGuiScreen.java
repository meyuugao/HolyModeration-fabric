package me.yuugao.holymoderation.client.gui.screen.impl;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.screen.AnimatedGuiScreen;
import me.yuugao.holymoderation.client.gui.tabs.impl.main.AppearanceTab;
import me.yuugao.holymoderation.client.gui.tabs.impl.main.AutomationTab;
import me.yuugao.holymoderation.client.gui.tabs.impl.main.ChatTab;
import me.yuugao.holymoderation.client.gui.tabs.impl.main.JournalTab;
import me.yuugao.holymoderation.client.gui.tabs.impl.main.SoundsTab;
import me.yuugao.holymoderation.client.gui.tabs.impl.main.TwinksTab;
import me.yuugao.holymoderation.client.util.factory.DrawableElementFactory;
import me.yuugao.holymoderation.client.util.service.AnimationService;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.Render2DService;
import me.yuugao.holymoderation.client.util.service.ThemePalette;
import me.yuugao.holymoderation.client.util.service.ThemeService;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import org.joml.Matrix3x2fStack;

import lombok.Getter;

@Singleton
public class MainGuiScreen extends AnimatedGuiScreen {
    private static final float TARGET_WIDTH = 560f;
    private static final float TARGET_HEIGHT = 470f;
    private static final float TAB_BAR_HEIGHT = 30f;
    private static final float TAB_PILL_HEIGHT = 16f;
    private static final float TAB_PAD_X = 9f;
    private static final float TAB_GAP = 4f;

    @Getter
    private final int renderPriority = 2000;

    private final Render2DService render2DService;
    private final ThemeService themeService;
    private final MinecraftService minecraftService;

    private float renderAnim = 1f;

    @Inject
    public MainGuiScreen(AnimationService animationService, Render2DService render2DService,
                         ConfigManagerService configManagerService, DrawableElementFactory drawableElementFactory,
                         ThemeService themeService, MinecraftService minecraftService) {
        super(Text.of("HolyModeration Main Gui Screen"), animationService);

        this.render2DService = render2DService;
        this.themeService = themeService;
        this.minecraftService = minecraftService;

        addTab("Автоматика", new AutomationTab(this, themeService, configManagerService, minecraftService, render2DService, drawableElementFactory));
        addTab("Оформление", new AppearanceTab(this, themeService, configManagerService, minecraftService, render2DService, drawableElementFactory));
        addTab("Звуки", new SoundsTab(this, themeService, configManagerService, minecraftService, render2DService, drawableElementFactory));
        addTab("Чат", new ChatTab(this, themeService, configManagerService, minecraftService, render2DService, drawableElementFactory));
        addTab("Твинки", new TwinksTab(this, themeService, minecraftService, render2DService, drawableElementFactory));
        addTab("Журнал", new JournalTab(this, themeService, configManagerService, minecraftService, render2DService, drawableElementFactory));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float tickDelta) {
        super.render(ctx, mouseX, mouseY, tickDelta);

        Matrix3x2fStack ms = ctx.getMatrices();
        ThemePalette palette = themeService.getPalette();
        render2DService.resetScissor();

        this.width = TARGET_WIDTH;
        this.height = TARGET_HEIGHT;
        this.renderAnim = Math.max(getAnimValue(), 0.001f);

        float baseOutline = 1f;
        float scaledOutline = baseOutline * (height / 100f);

        this.screenScale = Math.min(ctx.getScaledWindowWidth() / 1280f, ctx.getScaledWindowHeight() / 720f);

        render2DService.setupRender();

        ms.pushMatrix();

        float windowWidth = ctx.getScaledWindowWidth();
        float windowHeight = ctx.getScaledWindowHeight();

        this.x = (windowWidth - width * renderAnim * screenScale) / 2f;
        this.y = (windowHeight - height * renderAnim * screenScale) / 2f;

        ms.translate(x, y);
        ms.scale(screenScale, screenScale);
        ms.translate(width / 2f, height / 2f);
        ms.scale(renderAnim, renderAnim);
        ms.translate(-width / 2f, -height / 2f);

        render2DService.renderSoftRoundedRectOutline(
                ctx, 0f, 0f, width, height,
                renderPriority, 12f,
                palette.background, palette.outline,
                scaledOutline, 3
        );

        float[] outerA = transformPoint(ms, 0f, 0f);
        float[] outerB = transformPoint(ms, width, height);
        render2DService.pushScissor(outerA[0], outerA[1], outerB[0], outerB[1]);

        renderTabBar(ctx, palette);

        int relMouseX = (int) toLocalX(mouseX);
        int relMouseY = (int) toLocalY(mouseY);
        renderTabs(ctx, relMouseX, relMouseY, tickDelta);

        render2DService.popScissor();

        ms.popMatrix();

        render2DService.endRender();
    }

    private static float[] transformPoint(Matrix3x2fStack ms, float x, float y) {
        return new float[]{
                ms.m00 * x + ms.m10 * y + ms.m20,
                ms.m01 * x + ms.m11 * y + ms.m21
        };
    }

    private void renderTabBar(DrawContext ctx, ThemePalette palette) {
        TextRenderer tr = minecraftService.getClient().textRenderer;

        float total = 0f;
        for (String key : tabOrder) {
            total += tr.getWidth(key) + TAB_PAD_X * 2f;
        }
        total += TAB_GAP * (tabOrder.size() - 1);

        float cursorX = (width - total) / 2f;
        for (String key : tabOrder) {
            boolean active = key.equals(activeTabKey);
            float pillW = tr.getWidth(key) + TAB_PAD_X * 2f;
            float pillX = cursorX;
            float pillY = (TAB_BAR_HEIGHT - TAB_PILL_HEIGHT) / 2f;

            render2DService.renderSoftRoundedRect(ctx, pillX, pillY, pillW, TAB_PILL_HEIGHT, renderPriority,
                    TAB_PILL_HEIGHT / 2f, active ? palette.primary : palette.surface, 0);

            if (active) {
                render2DService.renderSoftRoundedRectOutline(ctx, pillX, pillY, pillW, TAB_PILL_HEIGHT, renderPriority,
                        TAB_PILL_HEIGHT / 2f, palette.primary, palette.primaryBright, 1.2f, 2f);
            }

            float textX = pillX + TAB_PAD_X;
            float textY = pillY + (TAB_PILL_HEIGHT - tr.fontHeight) / 2f;
            render2DService.renderText(tr, Text.literal(key).asOrderedText(),
                    (int) textX, (int) textY, renderPriority,
                    (active ? palette.onPrimary : palette.textSecondary).getRGB(), false, ctx);

            cursorX += pillW + TAB_GAP;
        }

        render2DService.renderRoundedRect(ctx, 16f, TAB_BAR_HEIGHT + 2f, width - 32f, 1.2f, renderPriority,
                1f, palette.selection);
    }

    @Override
    protected float toLocalX(double mouseX) {
        if (screenScale <= 0f) return (float) mouseX;
        float raw = (float) ((mouseX - x) / screenScale);
        return width / 2f + (raw - width / 2f) / renderAnim;
    }

    @Override
    protected float toLocalY(double mouseY) {
        if (screenScale <= 0f) return (float) mouseY;
        float raw = (float) ((mouseY - y) / screenScale);
        return height / 2f + (raw - height / 2f) / renderAnim;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubleClick) {
        float localX = toLocalX(click.x());
        float localY = toLocalY(click.y());
        int tabCount = tabOrder.size();

        if (click.button() == 0 && tabCount > 0
                && localX >= 0f && localX <= width && localY >= 0f && localY <= TAB_BAR_HEIGHT) {
            int index = tabIndexFor(localX);
            if (index >= 0) {
                activeTabKey = tabOrder.get(index);
                return true;
            }
        }
        return super.mouseClicked(click, doubleClick);
    }

    private int tabIndexFor(float localX) {
        TextRenderer tr = minecraftService.getClient().textRenderer;
        float total = 0f;
        for (String key : tabOrder) {
            total += tr.getWidth(key) + TAB_PAD_X * 2f;
        }
        total += TAB_GAP * (tabOrder.size() - 1);

        float cursorX = (width - total) / 2f;
        for (int i = 0; i < tabOrder.size(); i++) {
            String key = tabOrder.get(i);
            float pillW = tr.getWidth(key) + TAB_PAD_X * 2f;
            if (localX >= cursorX && localX <= cursorX + pillW) return i;
            cursorX += pillW + TAB_GAP;
        }
        return -1;
    }

    public void selectTab(String key) {
        if (tabs.containsKey(key)) {
            activeTabKey = key;
        }
    }

    public boolean isInsideWindow(double mouseX, double mouseY) {
        float renderAnim = Math.max(getAnimValue(), 0.001f);
        float scaledW = width * renderAnim * screenScale;
        float scaledH = height * renderAnim * screenScale;
        return mouseX >= x && mouseX <= x + scaledW
                && mouseY >= y && mouseY <= y + scaledH;
    }
}