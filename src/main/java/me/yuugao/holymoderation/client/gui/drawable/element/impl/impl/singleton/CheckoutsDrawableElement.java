package me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.singleton;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.StatefulDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.state.impl.CheckoutsRenderState;
import me.yuugao.holymoderation.client.gui.drawable.element.state.provider.impl.CheckoutsRenderStateProvider;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.service.AnimationService;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.Render2DService;
import me.yuugao.holymoderation.client.util.service.ThemePalette;
import me.yuugao.holymoderation.client.util.service.ThemeService;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import org.apache.commons.lang3.StringUtils;

@Singleton
public class CheckoutsDrawableElement extends StatefulDrawableElement<CheckoutsRenderState> {
    private final AnimationService.Value currentWidth;
    private final AnimationService.Value currentHeight;
    private final MinecraftService minecraftService;
    private final Render2DService render2DService;
    private final ConfigManagerService configManagerService;
    private final ThemeService themeService;

    private String lastPlayer = StringUtils.EMPTY;

    private float animTarget = 0f;
    private long checkoutStartMillis = 0L;

    @Inject
    public CheckoutsDrawableElement(AnimationService animationService, MinecraftService minecraftService,
                                    Render2DService render2DService, ConfigManagerService configManagerService,
                                    ThemeService themeService,
                                    CheckoutsRenderStateProvider checkoutsRenderStateProvider) {
        super(animationService, PivotMode.DOWN, configManagerService, checkoutsRenderStateProvider);

        this.currentWidth = animationService.createValue(1f);
        this.currentHeight = animationService.createValue(1f);

        this.minecraftService = minecraftService;
        this.render2DService = render2DService;
        this.configManagerService = configManagerService;
        this.themeService = themeService;
    }

    @Override
    public String getHudElementId() {
        return "checkouts";
    }

    @Override
    protected void initPosition(DrawContext ctx) {
        setRelativePos(0.5f, 0.9f);
        this.scale.reset(0f);
    }

    @Override
    protected void render(DrawContext ctx, int z, CheckoutsRenderState renderState) {
        String player = renderState.checkoutPlayer();
        if (!lastPlayer.equals(player)) {
            if (!player.isEmpty()) {
                checkoutStartMillis = System.currentTimeMillis();
                animTarget = 1f;
            } else {
                checkoutStartMillis = 0L;
                animTarget = 0f;
            }
            lastPlayer = player;
        }

        this.scale.setTarget(animTarget);
        this.scale.update();

        if (scale.get() < 0.01f) return;

        long elapsed = checkoutStartMillis == 0L
                ? 0L
                : (System.currentTimeMillis() - checkoutStartMillis) / 1000L;

        String content = "Текущая проверка: %s | %s:%s".formatted(
                player.isEmpty() ? lastPlayer : player,
                elapsed / 60,
                String.format("%02d", elapsed % 60)
        );

        renderContent(content, ctx, z);
    }

    private void renderContent(String display, DrawContext ctx, int z) {
        TextRenderer tr = minecraftService.getClient().textRenderer;
        MatrixStack ms = ctx.getMatrices();
        ThemePalette palette = themeService.getPalette();

        float targetWidth = tr.getWidth(display) + 16f;
        float targetHeight = tr.fontHeight + 12f;

        this.currentWidth.setTarget(targetWidth).setSpeed(1.2f);
        this.currentHeight.setTarget(targetHeight).setSpeed(1.2f);
        this.currentWidth.update();
        this.currentHeight.update();

        setWidth(Math.max(1f, currentWidth.get()));
        setHeight(Math.max(1f, currentHeight.get()));

        render2DService.setupRender();

        render2DService.renderSoftRoundedRectOutline(
                ms, 0f, 0f, getWidth(), getHeight(), z,
                10f, palette.background, palette.primary, 1.5f, 3);

        render2DService.renderText(
                tr,
                display,
                (int) (getWidth() / 2f - tr.getWidth(display) / 2f),
                (int) (getHeight() / 2f - tr.fontHeight / 2f + 1f),
                z,
                0xffffffff,
                false,
                ctx
        );

        render2DService.endRender();
    }

    public void coStartForLocal(String player) {
        checkoutStartMillis = System.currentTimeMillis();
        lastPlayer = player;
        animTarget = 1f;
    }
}