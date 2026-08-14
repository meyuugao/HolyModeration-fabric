package me.yuugao.holymoderation.client.gui.drawable.element.impl.impl.singleton;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.gui.drawable.element.impl.StatefulDrawableElement;
import me.yuugao.holymoderation.client.gui.drawable.element.state.impl.NotificationsRenderState;
import me.yuugao.holymoderation.client.gui.drawable.element.state.provider.impl.NotificationsRenderStateProvider;
import me.yuugao.holymoderation.client.gui.drawable.render.PivotMode;
import me.yuugao.holymoderation.client.gui.drawable.render.RenderMode;
import me.yuugao.holymoderation.client.util.service.AnimationService;
import me.yuugao.holymoderation.client.util.service.NotificationsService;
import me.yuugao.holymoderation.client.util.service.config.ConfigManagerService;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;

@Singleton
public class NotificationsDrawableElement extends StatefulDrawableElement<NotificationsRenderState> {
    private final NotificationsService notificationsService;
    private RenderMode currentMode = RenderMode.LIVE;

    @Inject
    public NotificationsDrawableElement(AnimationService animationService, NotificationsService notificationsService,
                                        ConfigManagerService configManagerService,
                                        NotificationsRenderStateProvider notificationsRenderStateProvider) {
        super(animationService, PivotMode.RIGHT_DOWN, configManagerService, notificationsRenderStateProvider);

        this.notificationsService = notificationsService;
    }

    @Override
    public String getHudElementId() {
        return "notifications";
    }

    @Override
    protected void initPosition(DrawContext ctx) {
        setRelativePos(pivotMode.getXFactor(), pivotMode.getYFactor());
    }

    @Override
    protected void render(DrawContext ctx, int z, NotificationsRenderState state) {
    }

    @Override
    public void updateRenderForScreen(DrawContext ctx, int z, RenderMode mode) {
        this.currentMode = mode;
        super.updateRenderForScreen(ctx, z, mode);
    }

    @Override
    public void updateRender(DrawContext ctx, float parentW, float parentH, int z, float screenScale) {
        this.screenScale = screenScale;

        if (currentMode == RenderMode.CONFIG) {
            notificationsService.showPreview();
        } else {
            notificationsService.hidePreview();
        }

        MatrixStack ms = ctx.getMatrices();
        ms.push();
        ms.scale(screenScale, screenScale, 1f);

        float w = parentW / screenScale;
        float h = parentH / screenScale;
        float notifBaseWidth = parentW / 6f;
        float[] cfg = getConfig();
        notificationsService.renderNotificationsLocal(
                ctx, z, cfg[0], cfg[1], cfg[2], w, h, notifBaseWidth);

        ms.pop();

        float stackHeight = notificationsService.getNotificationsStackHeight(10f);
        setWidth(notifBaseWidth * screenScale);
        setHeight(stackHeight * screenScale);
    }

    @Override
    public boolean isMouseOver(float parentW, float parentH, double mouseX, double mouseY) {
        if (getHeight() <= 0f) return false;

        float margin = 8f * screenScale;
        float x1 = parentW - getWidth() - margin;
        float y1 = parentH - getHeight() - margin;

        return mouseX >= x1 && mouseX <= parentW && mouseY >= y1 && mouseY <= parentH;
    }

    private float[] getConfig() {
        return switch (getPivotMode()) {
            case RIGHT_DOWN, RIGHT, DOWN, UP, CENTER -> new float[]{-1f, 1f, 0f};
            case RIGHT_UP -> new float[]{1f, 1f, 0f};
            case LEFT_DOWN, LEFT -> new float[]{-1f, -1f, 0f};
            case LEFT_UP -> new float[]{1f, -1f, 0f};
        };
    }
}