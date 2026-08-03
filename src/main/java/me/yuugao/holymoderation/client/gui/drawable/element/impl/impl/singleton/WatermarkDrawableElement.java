package me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.singleton;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.StatefulDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.state.impl.WatermarkRenderState;
import me.yuugao.holymoderation.client.gui.drawable.element.state.provider.impl.WatermarkRenderStateProvider;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.util.service.AnimationService;
import me.yuugao.holymoderation.client.util.service.MinecraftService;
import me.yuugao.holymoderation.client.util.service.Render2DService;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;
import me.yuugao.holymoderation.client.util.service.config.impl.GuiConfig;
import me.yuugao.holymoderation.client.util.service.state.UserStateService;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

import java.util.Random;

@Singleton
public class WatermarkDrawableElement extends StatefulDrawableElement<WatermarkRenderState> {
    private static final char[][] LEET = {
            {'a', '4'}, {'e', '3'}, {'i', '1'}, {'o', '0'}, {'s', '5'}, {'l', '1'}
    };
    private static final long TICK_MS = 300L;
    private final Random random = new Random();
    private final char[] baseAnimText = "made for holyworld".toCharArray();
    private final char[] animBuffer = baseAnimText.clone();
    private final ConfigManagerService configManagerService;
    private final UserStateService userStateService;
    private final MinecraftService minecraftService;
    private final Render2DService render2DService;
    private long lastTick;

    @Inject
    public WatermarkDrawableElement(AnimationService animationService, ConfigManagerService configManagerService,
                                    UserStateService userStateService, MinecraftService minecraftService,
                                    Render2DService render2DService, WatermarkRenderStateProvider watermarkRenderStateProvider) {
        super(animationService, PivotMode.LEFT_UP, watermarkRenderStateProvider);

        this.lastTick = System.currentTimeMillis();
        this.configManagerService = configManagerService;
        this.userStateService = userStateService;
        this.minecraftService = minecraftService;
        this.render2DService = render2DService;
    }

    @Override
    protected void initPosition(DrawContext ctx) {
        float rel = 0.01f;
        float minSide = Math.min(ctx.getScaledWindowWidth(), ctx.getScaledWindowHeight());
        float relX = rel * minSide / ctx.getScaledWindowWidth();
        float relY = rel * minSide / ctx.getScaledWindowHeight();
        setRelativePos(relX, relY);
        this.scale.reset(0f);
    }

    @Override
    protected void render(DrawContext ctx, int z, WatermarkRenderState state) {
        TextRenderer tr = minecraftService.getClient().textRenderer;
        MatrixStack ms = ctx.getMatrices();
        GuiConfig guiConfig = configManagerService.getGuiConfig();

        long now = System.currentTimeMillis();
        if (now - lastTick >= TICK_MS) {
            updateAnimText();
            lastTick = now;
        }

        float animTarget = state.animTarget();
        this.scale.setTarget(animTarget);
        this.scale.update();

        if (scale.get() < 0.01f) return;

        String text = "HolyModeration v%s | %s | %s".formatted(
                configManagerService.getApiConfig().getCurrentVersion(),
                userStateService.getUserNickname(),
                new String(animBuffer)
        );

        setWidth(tr.getWidth(text) + 12f);
        setHeight(tr.fontHeight + 8f);

        render2DService.setupRender();

        render2DService.renderSoftRoundedRectOutline(ms, 0f, 0f, getWidth(),
                getHeight(), z, 8f, guiConfig.getMainColor(), guiConfig.getSecondColor(), 1.2f, 3);

        render2DService.renderText(tr, text, (int) (getWidth() / 2f - tr.getWidth(text) / 2f),
                (int) (getHeight() / 2f - tr.fontHeight / 2f + 1f), z, 0xffffffff, false, ctx);

        render2DService.endRender();
    }

    private void updateAnimText() {
        int i = random.nextInt(animBuffer.length);
        char base = baseAnimText[i];
        if (base == ' ') {
            animBuffer[i] = base;
            return;
        }
        for (char[] map : LEET) {
            if (map[0] == base) {
                animBuffer[i] = animBuffer[i] == base && random.nextInt(4) == 0 ? map[1] : base;
                return;
            }
        }
        animBuffer[i] = base;
    }
}