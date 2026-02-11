package me.yuugao.holymoderation.client.util.serviceLocator.service;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

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

    public void renderNotifications(DrawContext ctx, int z) {
        MinecraftService minecraftService = ServiceLocator.getMinecraftService();
        Render2DService render2DService = ServiceLocator.getRender2DService();

        TextRenderer tr = minecraftService.getClient().textRenderer;

        float screenW = ctx.getScaledWindowWidth();
        float screenH = ctx.getScaledWindowHeight();

        long now = System.nanoTime();
        float delta = (now - lastNano) / 1_000_000_000f;
        lastNano = now;

        float margin = 8f;
        float spacing = 10f;
        float width = screenW / 6f;
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

        notificationPool.removeIf(n ->
                n.state == State.HIDING && n.x > screenW
        );

        float yCursor = screenH - margin;
        for (int i = notificationPool.size() - 1; i >= 0; i--) {
            Notification n = notificationPool.get(i);

            if (n.state != State.HIDING) {
                n.targetY = yCursor - n.height;
                yCursor -= n.height + spacing;
            }

            n.targetX = screenW - margin - n.width;
            if (n.state == State.HIDING) {
                n.targetX = screenW + n.width * 2;
            }
        }

        for (Notification n : notificationPool) {
            if (n.state == State.SPAWNING && !n.initialized) {
                n.x = screenW + n.width;
                n.y = screenH + n.height;
                n.initialized = true;
            }

            float speedY = n.state == State.SPAWNING ? 14f : 8f;
            n.y += (n.targetY - n.y) * Math.min(1f, speedY * delta);

            float speedX = n.state == State.HIDING ? 12f : 10f;
            n.x += (n.targetX - n.x) * Math.min(1f, speedX * delta);

            if (n.state == State.SPAWNING && Math.abs(n.y - n.targetY) < 0.5f) {
                n.state = State.IDLE;
            }
        }

        for (Notification n : notificationPool) {
            if (n.y + n.height < 0) continue;

            MatrixStack ms = ctx.getMatrices();
            Color bg = n.type.getBg();
            Color ol = n.type.getOutline();

            render2DService.setupRender();

            render2DService.renderSoftRoundedRectOutline(
                    ms, n.x, n.y, n.width, n.height, z,
                    radius, bg, ol, outline, blur
            );

            ms.push();
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

    private static class Notification {
        NotificationType type;
        String title;
        String text;
        float liveTime;
        float elapsed;
        float x;
        float y;
        float targetX;
        float targetY;
        float width;
        float height;
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

    private enum State {
        SPAWNING,
        IDLE,
        HIDING
    }
}