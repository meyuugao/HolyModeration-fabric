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

import org.joml.Matrix3x2fStack;

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
        PivotMode configured = configManagerService.getGuiConfig().getPivotMode(getHudElementId());
        if (configured != PivotMode.CENTER) {
            setPivotMode(configured);
        }
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

        Matrix3x2fStack ms = ctx.getMatrices();
        ms.pushMatrix();
        ms.scale(screenScale, screenScale);

        float w = parentW / screenScale;
        float h = parentH / screenScale;
        float notifBaseWidth = parentW / 6f;
        float[] cfg = getConfig();
        notificationsService.renderNotificationsLocal(
                ctx, z, cfg[0], cfg[1], cfg[2], w, h, notifBaseWidth);

        ms.popMatrix();

        float stackHeight = notificationsService.getNotificationsStackHeight(10f);
        setWidth(notifBaseWidth);
        setHeight(stackHeight);
    }

    @Override
    public boolean isMouseOver(float parentW, float parentH, double mouseX, double mouseY) {
        if (getScaledHeight() <= 0f) return false;

        float margin = 8f * screenScale;
        float[] cfg = getConfig();
        float stackDirY = cfg[0];
        float hideDirX = cfg[1];

        float w = getScaledWidth();
        float h = getScaledHeight();

        float x1, x2, y1, y2;
        if (hideDirX > 0) {
            x1 = parentW - w - margin;
            x2 = parentW + margin;
        } else if (hideDirX < 0) {
            x1 = -margin;
            x2 = w + margin;
        } else {
            x1 = (parentW - w) / 2f - margin;
            x2 = (parentW + w) / 2f + margin;
        }

        if (stackDirY < 0) {
            y1 = parentH - h - margin;
            y2 = parentH + margin;
        } else if (stackDirY > 0) {
            y1 = -margin;
            y2 = h + margin;
        } else {
            y1 = (parentH - h) / 2f - margin;
            y2 = (parentH + h) / 2f + margin;
        }

        return mouseX >= x1 && mouseX <= x2 && mouseY >= y1 && mouseY <= y2;
    }

    private float[] getConfig() {
        float xFactor = pivotMode.getXFactor();
        float yFactor = pivotMode.getYFactor();
        float hideDirX = xFactor > 0.5f ? 1f : (xFactor < 0.5f ? -1f : 0f);
        float stackDirY = yFactor > 0.5f ? -1f : (yFactor < 0.5f ? 1f : 0f);
        return new float[]{stackDirY, hideDirX, 0f};
    }
}