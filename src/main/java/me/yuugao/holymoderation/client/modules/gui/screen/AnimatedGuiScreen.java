package me.yuugao.holymoderation.client.modules.gui.screen;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import net.minecraft.text.Text;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import obfuscator.DontObf;
import obfuscator.ObfRule;

public class AnimatedGuiScreen extends GuiScreen {
    private final float speed = 0.05f;

    private float progress = 0f;
    private boolean opening = true;

    @Getter
    private float animValue = 0f;

    private ScheduledFuture<?> task;

    protected AnimatedGuiScreen(Text title, ServiceContext serviceContext) {
        super(title, serviceContext);
    }

    @Override
    @DontObf(ObfRule.MAP_METHOD)
    protected void init() {
        progress = 0f;
        animValue = 0f;
        opening = true;

        startAnimation();
    }

    private void startAnimation() {
        stopAnimation();

        task = serviceContext.getSchedulerService().getInstance().scheduleAtFixedRate(() -> {
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
        serviceContext.getMinecraftService().getClient().execute(super::close);
    }

    @Override
    @DontObf(ObfRule.MAP_METHOD)
    public void close() {
        opening = false;
    }

    @Override
    @DontObf(ObfRule.MAP_METHOD)
    public void removed() {
        stopAnimation();
        super.removed();
    }
}