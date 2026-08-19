package me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.singleton;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.StatefulDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.state.impl.SpyRenderState;
import me.yuugao.holymoderation.client.gui.drawable.element.state.provider.impl.SpyRenderStateProvider;
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
public class SpyDrawableElement extends StatefulDrawableElement<SpyRenderState> {
    private final AnimationService.Value currentWidth;
    private final AnimationService.Value currentHeight;
    private final MinecraftService minecraftService;
    private final Render2DService render2DService;
    private final ConfigManagerService configManagerService;
    private final ThemeService themeService;

    @Inject
    public SpyDrawableElement(AnimationService animationService, MinecraftService minecraftService, Render2DService render2DService,
                              ConfigManagerService configManagerService, ThemeService themeService,
                              SpyRenderStateProvider spyRenderStateProvider) {
        super(animationService, PivotMode.UP, configManagerService, spyRenderStateProvider);

        this.currentWidth = animationService.createValue(1f);
        this.currentHeight = animationService.createValue(1f);

        this.minecraftService = minecraftService;
        this.render2DService = render2DService;
        this.configManagerService = configManagerService;
        this.themeService = themeService;
    }

    @Override
    public String getHudElementId() {
        return "spy";
    }

    @Override
    protected void initPosition(DrawContext ctx) {
        setRelativePos(0.5f, 0.02f);
        this.scale.reset(0f);
    }

    @Override
    protected void render(DrawContext ctx, int z, SpyRenderState renderState) {
        TextRenderer tr = minecraftService.getClient().textRenderer;
        MatrixStack ms = ctx.getMatrices();
        ThemePalette palette = themeService.getPalette();

        String[] current = renderState.stringsToRender();
        float animTarget;
        String display0, display1;
        if (!(current[0].isEmpty() && current[1].isEmpty())) {
            display0 = current[0];
            display1 = current[1];
            animTarget = 1f;
        } else {
            display0 = display1 = StringUtils.EMPTY;
            animTarget = 0f;
        }

        this.scale.setTarget(animTarget);
        this.scale.update();

        if (scale.get() < 0.01f) return;

        float targetWidth = Math.max(tr.getWidth(display0), tr.getWidth(display1)) + 16f;
        int lines = display1.isEmpty() ? 1 : 2;
        float textBlockHeight = lines * tr.fontHeight + (lines == 2 ? 4 : 0);
        float targetHeight = textBlockHeight + 12f;

        this.currentWidth.setTarget(targetWidth).setSpeed(1.2f);
        this.currentHeight.setTarget(targetHeight).setSpeed(1.2f);
        this.currentWidth.update();
        this.currentHeight.update();

        setWidth(Math.max(1f, currentWidth.get()));
        setHeight(Math.max(1f, currentHeight.get()));

        render2DService.setupRender();

        float baseY = (getHeight() - textBlockHeight) / 2f + 0.5f;
        render2DService.renderSoftRoundedRectOutline(ms, 0f, 0f, getWidth(), getHeight(), z,
                10f, themeService.fillColor(getHudElementId(), palette.background), themeService.accentColor(getHudElementId(), palette.primary), 1.5f, 3);

        render2DService.renderText(tr, display0, (int) (getWidth() / 2f - tr.getWidth(display0) / 2f),
                (int) baseY, z, 0xffffffff, false, ctx);
        if (!display1.isEmpty()) {
            render2DService.renderText(tr, display1, (int) (getWidth() / 2f - tr.getWidth(display1) / 2f),
                    (int) (baseY + tr.fontHeight + 4), z, 0xffffffff, false, ctx);
        }

        render2DService.endRender();
    }
}