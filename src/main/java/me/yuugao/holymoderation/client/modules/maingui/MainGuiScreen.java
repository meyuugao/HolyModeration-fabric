package me.yuugao.holymoderation.client.modules.maingui;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class MainGuiScreen extends Screen {
    private float progress = 0f;
    private float animValue = 0f;
    private boolean opening = true;

    private ScheduledFuture<?> task;

    public MainGuiScreen() {
        super(Text.of("HolyModeration Main Gui Screen"));
    }

    @Override
    protected void init() {
        progress = 0f;
        animValue = 0f;
        opening = true;

        startAnimation();
    }

    private void startAnimation() {
        stopAnimation();

        task = ServiceLocator.getSchedulerService().getInstance().scheduleAtFixedRate(() -> {
            float speed = 0.05f;

            if (opening) {
                progress += speed;
                if (progress > 1f) progress = 1f;
            } else {
                progress -= speed;
                if (progress < 0f) {
                    progress = 0f;
                    onFullyClosed();
                }
            }

            float t = progress;
            float overshoot = 1.25f;

            float v;
            if (t < 0.5f) {
                float k = t / 0.5f;
                v = k * overshoot;
            } else {
                float k = (t - 0.5f) / 0.5f;
                v = overshoot - k * (overshoot - 1f);
            }

            animValue = v;
        }, 0, 16, TimeUnit.MILLISECONDS);
    }

    private void stopAnimation() {
        if (task != null && !task.isCancelled()) {
            task.cancel(false);
        }
    }

    private void onFullyClosed() {
        stopAnimation();
        ServiceLocator.getMinecraftService().getClient().execute(super::close);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float tickDelta) {
        float targetW = (float) context.getScaledWindowWidth() / 2.2f;
        float targetH = (float) context.getScaledWindowHeight() / 1.8f;

        float w = targetW * animValue;
        float h = targetH * animValue;

        float cx = (float) context.getScaledWindowWidth() / 2;
        float cy = (float) context.getScaledWindowHeight() / 2;

        float x = cx - w / 2;
        float y = cy - h / 2;

        float baseOutline = 1f;
        float scaleFactor = Math.min(w, h) / 100f;
        float scaledOutline = baseOutline * scaleFactor;

        ServiceLocator.getRender2DService().renderRoundedOutlinedRect(
                context.getMatrices(),
                x, y, w, h,
                10,
                new Color(0x002AFF),
                new Color(0xFFFFFF),
                scaledOutline
        );


        super.render(context, mouseX, mouseY, tickDelta);
    }

    @Override
    public void close() {
        opening = false;
    }

    @Override
    public void removed() {
        stopAnimation();
        super.removed();
    }
}
