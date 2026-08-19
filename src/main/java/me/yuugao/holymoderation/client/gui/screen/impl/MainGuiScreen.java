package me.yuugao.holymoderation.client.gui.screen.impl;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.screen.AnimatedGuiScreen;
import me.yuugao.holymoderation.client.gui.tabs.impl.main.AutomationTab;
import me.yuugao.holymoderation.client.gui.tabs.impl.main.ElementsTab;
import me.yuugao.holymoderation.client.gui.tabs.impl.main.GeneralTab;
import me.yuugao.holymoderation.client.gui.tabs.impl.main.ThemeTab;
import me.yuugao.holymoderation.client.util.factory.DrawableElementFactory;
import me.yuugao.holymoderation.client.util.service.AnimationService;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.Render2DService;
import me.yuugao.holymoderation.client.util.service.ThemePalette;
import me.yuugao.holymoderation.client.util.service.ThemeService;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import lombok.Getter;

@Singleton
public class MainGuiScreen extends AnimatedGuiScreen {
    private static final float TAB_BAR_HEIGHT = 22f;

    @Getter
    private final int renderPriority = 2000;

    private final Render2DService render2DService;
    private final ConfigManagerService configManagerService;
    private final ThemeService themeService;
    private final MinecraftService minecraftService;

    @Inject
    public MainGuiScreen(AnimationService animationService, Render2DService render2DService,
                         ConfigManagerService configManagerService, DrawableElementFactory drawableElementFactory,
                         ThemeService themeService, MinecraftService minecraftService) {
        super(Text.of("HolyModeration Main Gui Screen"), animationService);

        this.render2DService = render2DService;
        this.configManagerService = configManagerService;
        this.themeService = themeService;
        this.minecraftService = minecraftService;

        addTab("Основные", new GeneralTab(this, themeService, configManagerService, minecraftService, render2DService, drawableElementFactory));
        addTab("Автоматика", new AutomationTab(this, themeService, configManagerService, minecraftService, render2DService, drawableElementFactory));
        addTab("Тема", new ThemeTab(this, themeService, configManagerService, minecraftService, render2DService, drawableElementFactory));
        addTab("Элементы", new ElementsTab(this, drawableElementFactory, themeService));
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float tickDelta) {
        super.render(ctx, mouseX, mouseY, tickDelta);

        MatrixStack ms = ctx.getMatrices();
        ThemePalette palette = themeService.getPalette();

        float targetWidth = 435f;
        float targetHeight = 300f;

        this.width = targetWidth * getAnimValue();
        this.height = targetHeight * getAnimValue();

        float baseOutline = 1f;
        float scaleFactor = Math.min(width, height) / 100f;
        float scaledOutline = baseOutline * scaleFactor;

        this.screenScale = Math.min(ctx.getScaledWindowWidth() / 1280f, ctx.getScaledWindowHeight() / 720f);

        render2DService.setupRender();

        ms.push();

        float windowWidth = ctx.getScaledWindowWidth();
        float windowHeight = ctx.getScaledWindowHeight();

        this.x = (windowWidth - width * screenScale) / 2f;
        this.y = (windowHeight - height * screenScale) / 2f;

        ms.translate(x, y, 0);
        ms.scale(screenScale, screenScale, 1f);

        render2DService.renderSoftRoundedRectOutline(
                ms, 0f, 0f,
                Math.max(1, this.width), Math.max(1, this.height),
                renderPriority, 10f,
                palette.background, palette.outline,
                scaledOutline, 3
        );

        renderTabBar(ctx, ms, palette);

        int relMouseX = (int) ((mouseX - x) / screenScale);
        int relMouseY = (int) ((mouseY - y) / screenScale);
        renderTabs(ctx, relMouseX, relMouseY, tickDelta);

        ms.pop();

        render2DService.endRender();
    }

    private void renderTabBar(DrawContext ctx, MatrixStack ms, ThemePalette palette) {
        TextRenderer tr = minecraftService.getClient().textRenderer;
        int tabCount = tabOrder.size();
        float tabWidth = width / tabCount;

        for (int i = 0; i < tabCount; i++) {
            String key = tabOrder.get(i);
            boolean active = key.equals(activeTabKey);

            float tabX = i * tabWidth + 4f;
            float tabY = 4f;
            float tabW = tabWidth - 8f;
            float tabH = TAB_BAR_HEIGHT - 8f;

            render2DService.renderSoftRoundedRectOutline(ms, tabX, tabY, tabW, tabH, renderPriority,
                    6f, active ? palette.primary : palette.surface, palette.outline, 1.5f, 2f);

            String label = key;
            int textWidth = tr.getWidth(label);
            float textX = tabX + (tabW - textWidth) / 2f;
            float textY = tabY + (tabH - tr.fontHeight) / 2f;
            render2DService.renderText(tr, Text.literal(label).asOrderedText(),
                    (int) textX, (int) textY, renderPriority, palette.textPrimary.getRGB(), false, ctx);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float localX = toLocalX(mouseX);
        float localY = toLocalY(mouseY);
        int tabCount = tabOrder.size();

        if (button == 0 && tabCount > 0 && width > 0f
                && localX >= 0f && localX <= width && localY >= 0f && localY <= TAB_BAR_HEIGHT) {
            int index = (int) (localX / (width / tabCount));
            index = Math.max(0, Math.min(tabCount - 1, index));
            activeTabKey = tabOrder.get(index);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
