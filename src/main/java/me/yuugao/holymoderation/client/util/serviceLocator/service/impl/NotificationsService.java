package me.yuugao.holymoderation.client.util.serviceLocator.service.impl;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;
import me.yuugao.holymoderation.client.util.serviceLocator.service.Service;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class NotificationsService extends Service {
    private final List<Notification> notificationPool = new ArrayList<>();
    private long lastNano = System.nanoTime();

    public void addNotification(NotificationType type, String title, String text, float liveTime) {
        SoundService soundService = ServiceLocator.getSoundService();
        LoggerService loggerService = ServiceLocator.getLoggerService();

        notificationPool.add(new Notification(type, title, text, liveTime));
        soundService.playSound(type.getSoundName());

        if (type.equals(NotificationType.EXCEPTION)) {
            loggerService.exception("Исключение из уведомлений: %s".formatted(text));
        }
    }

    public void addNotification(NotificationType type, String title, String text, float liveTime, String soundName) {
        SoundService soundService = ServiceLocator.getSoundService();

        notificationPool.add(new Notification(type, title, text, liveTime));
        if (!soundName.isEmpty()) {
            soundService.playSound(soundName);
        }
    }

    public void clearNotifications() {
        notificationPool.clear();
    }

    public void renderNotificationsLocal(DrawContext ctx, int z, float stackDirY, float hideDirX, float hideDirY,
                                         float screenLeft, float screenRight, float screenTop, float screenBottom,
                                         float localW) {

        MinecraftService minecraftService = ServiceLocator.getMinecraftService();
        Render2DService render2DService = ServiceLocator.getRender2DService();

        TextRenderer tr = minecraftService.getClient().textRenderer;

        long now = System.nanoTime();
        float delta = (now - lastNano) / 1_000_000_000f;
        lastNano = now;

        float margin = 8f;
        float spacing = 10f;
        float width = localW / 6f;
        float radius = 6f;
        float blur = 6f;
        float outline = 1.5f;
        float padding = 8f;

        for (Notification n : notificationPool) {
            int wrap = Math.max(1, (int) (width - padding * 2));
            n.titleLines.clear();
            for (String s : n.title.split("\n")) {
                n.titleLines.addAll(tr.wrapLines(Text.literal(s), wrap));
            }
            n.bodyLines.clear();
            for (String s : n.text.split("\n")) {
                n.bodyLines.addAll(tr.wrapLines(Text.literal(s), wrap));
            }
            n.width = width;
            n.height = padding + n.titleLines.size() * tr.fontHeight + 4f + n.bodyLines.size() * tr.fontHeight + padding;
        }

        for (Notification n : notificationPool) {
            n.elapsed += delta;
            if (n.state == State.IDLE && n.elapsed >= n.liveTime) {
                n.state = State.HIDING;
            }
        }

        notificationPool.removeIf(n -> {
            if (n.state != State.HIDING) return false;
            if (hideDirX > 0) return n.x > screenRight + n.width;
            if (hideDirX < 0) return n.x + n.width < screenLeft;
            if (hideDirY < 0) return n.y + n.height < screenTop;
            if (hideDirY > 0) return n.y > screenBottom;
            return false;
        });

        float cursor = margin;
        for (int i = notificationPool.size() - 1; i >= 0; i--) {
            Notification n = notificationPool.get(i);

            if (n.state != State.HIDING) {
                if (hideDirX > 0) {
                    n.targetX = screenRight - margin - n.width;
                } else if (hideDirX < 0) {
                    n.targetX = screenLeft + margin;
                } else {
                    n.targetX = (screenLeft + screenRight - n.width) / 2;
                }

                if (stackDirY < 0) {
                    n.targetY = screenBottom - cursor - n.height;
                } else if (stackDirY > 0) {
                    n.targetY = screenTop + cursor;
                } else {
                    n.targetY = (screenTop + screenBottom - n.height) / 2;
                }
                cursor += n.height + spacing;
            }

            if (n.state == State.HIDING) {
                if (hideDirX > 0) n.targetX = screenRight + n.width + margin;
                else if (hideDirX < 0) n.targetX = screenLeft - n.width - margin;
                if (hideDirY < 0) n.targetY = screenTop - n.height - margin;
                else if (hideDirY > 0) n.targetY = screenBottom + n.height + margin;
            }
        }

        for (Notification n : notificationPool) {
            if (n.state == State.SPAWNING && !n.initialized) {
                n.x = n.targetX;
                if (stackDirY < 0) n.y = screenBottom + n.height;
                else if (stackDirY > 0) n.y = screenTop - n.height;
                else n.y = n.targetY;
                n.initialized = true;
            }

            n.y += (n.targetY - n.y) * Math.min(1f, (n.state == State.SPAWNING ? 14f : 8f) * delta);
            n.x += (n.targetX - n.x) * Math.min(1f, (n.state == State.HIDING ? 12f : 10f) * delta);

            if (n.state == State.SPAWNING && Math.abs(n.y - n.targetY) < 0.5f && Math.abs(n.x - n.targetX) < 0.5f) {
                n.state = State.IDLE;
            }
        }

        for (Notification n : notificationPool) {
            if (n.y + n.height < screenTop || n.y > screenBottom) continue;

            MatrixStack ms = ctx.getMatrices();
            Color bg = n.type.getBg();
            Color ol = n.type.getOutline();

            render2DService.setupRender();

            ms.push();

            render2DService.renderSoftRoundedRectOutline(ms, n.x, n.y, n.width, n.height, z, radius, bg, ol, outline, blur);

            ms.translate(n.x, n.y, 0);

            float ty = padding;
            for (OrderedText line : n.titleLines) {
                render2DService.renderText(tr, line, (int) padding, (int) ty, z, 0xFFFFFF, false, ctx);
                ty += tr.fontHeight;
            }
            ty += 4f;
            for (OrderedText line : n.bodyLines) {
                render2DService.renderText(tr, line, (int) padding, (int) ty, z, 0xFFFFFF, false, ctx);
                ty += tr.fontHeight;
            }

            ms.pop();

            render2DService.endRender();
        }
    }

    private enum State {
        SPAWNING,
        IDLE,
        HIDING
    }

    private static class Notification {
        NotificationType type;
        String title;
        String text;
        float liveTime;
        float elapsed;
        float x, y;
        float targetX, targetY;
        float width, height;
        boolean initialized;
        State state = State.SPAWNING;
        List<OrderedText> titleLines = new ArrayList<>();
        List<OrderedText> bodyLines = new ArrayList<>();

        Notification(NotificationType type, String title, String text, float liveTime) {
            this.type = type;
            this.title = title;
            this.text = text;
            this.liveTime = liveTime;
        }
    }
}