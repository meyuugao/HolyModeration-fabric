package me.yuugao.holymoderation.client.modules.maingui;

import me.yuugao.holymoderation.client.util.ColorPicker;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;
import me.yuugao.holymoderation.obfuscation.DontObf;

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

    private Color outlineColor = Color.WHITE;

    public MainGuiScreen() {
        super(Text.of("HolyModeration Main Gui Screen"));
    }

    @Override
    @DontObf
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
    @DontObf
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

        ServiceLocator.getRender2DService().renderSoftRoundedRectOutline(
                context.getMatrices(),
                x, y, Math.max(1, w), Math.max(1, h),
                10f,
                new Color(0x002AFF),
                outlineColor,
                scaledOutline, 3
        );

        super.render(context, mouseX, mouseY, tickDelta);

        float radius = 25f;
        ColorPicker picker = new ColorPicker(x + w / 2, y + h / 2, radius * animValue, new Color(0x000000), 3);
        picker.render(context.getMatrices());
        picker.updateColorFromMouse(mouseX, mouseY);
        outlineColor = picker.getSelectedColor();
    }

    @Override
    @DontObf
    public void close() {
        opening = false;
    }

    @Override
    @DontObf
    public void removed() {
        stopAnimation();
        super.removed();
    }
}
