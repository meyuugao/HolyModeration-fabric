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

public class NotificationService extends Service {
    private final List<Notification> notificationPool = new ArrayList<>();
    private long lastNano = System.nanoTime();

    public void addNotification(NotificationType type, String title, String text, float liveTime) {
        notificationPool.add(new Notification(type, title, text, liveTime));
    }

    public void renderNotifications(DrawContext drawContext) {
        Render2DService r = ServiceLocator.getRender2DService();
        var mc = ServiceLocator.getMinecraftService().getClient();
        TextRenderer tr = mc.textRenderer;

        float screenW = drawContext.getScaledWindowWidth();
        float screenH = drawContext.getScaledWindowHeight();

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
            n.titleLines = tr.wrapLines(Text.literal(n.title), wrap);
            n.bodyLines = tr.wrapLines(Text.literal(n.text), wrap);
            n.width = width;
            n.height = padding
                    + n.titleLines.size() * tr.fontHeight
                    + 4f
                    + n.bodyLines.size() * tr.fontHeight
                    + padding;
        }

        float yCursor = screenH - margin;
        for (int i = notificationPool.size() - 1; i >= 0; i--) {
            Notification n = notificationPool.get(i);
            n.targetY = yCursor - n.height;
            yCursor -= n.height + spacing;
            n.targetX = screenW - n.width - margin;
            if (n.state == State.HIDING) {
                n.targetX = screenW + n.width + 40f;
            }
        }

        for (Notification n : notificationPool) {
            if (n.state == State.SPAWNING && !n.initialized) {
                n.x = n.targetX + n.width + 20f;
                n.y = screenH + n.height + 20f;
                n.initialized = true;
            }
        }

        for (Notification n : notificationPool) {
            n.elapsed += delta;

            if (n.state == State.IDLE && n.elapsed >= n.liveTime) {
                n.state = State.HIDING;
            }

            float speedY = n.state == State.SPAWNING ? 14f : 8f;
            n.y += (n.targetY - n.y) * Math.min(1f, speedY * delta);

            float speedX = n.state == State.HIDING ? 12f : 10f;
            n.x += (n.targetX - n.x) * Math.min(1f, speedX * delta);

            if (n.state == State.SPAWNING && Math.abs(n.y - n.targetY) < 0.5f) {
                n.state = State.IDLE;
            }

            if (n.state == State.HIDING && n.x > screenW + n.width * 0.5f) {
                n.remove = true;
            }
        }

        notificationPool.removeIf(n -> n.remove);

        for (Notification n : notificationPool) {
            MatrixStack matrices = drawContext.getMatrices();

            Color bg = n.type.bg();
            Color ol = n.type.outline();

            r.renderSoftRoundedRectOutline(
                    matrices,
                    n.x,
                    n.y,
                    n.width,
                    n.height,
                    radius,
                    bg,
                    ol,
                    outline,
                    blur
            );

            float tx = n.x + padding;
            float ty = n.y + padding;

            for (OrderedText line : n.titleLines) {
                tr.draw(line, tx, ty, 0xFFFFFF, false,
                        matrices.peek().getPositionMatrix(),
                        drawContext.getVertexConsumers(),
                        TextRenderer.TextLayerType.NORMAL,
                        0, 15728880);
                ty += tr.fontHeight;
            }

            ty += 4f;

            for (OrderedText line : n.bodyLines) {
                tr.draw(line, tx, ty, 0xD0D0D0, false,
                        matrices.peek().getPositionMatrix(),
                        drawContext.getVertexConsumers(),
                        TextRenderer.TextLayerType.NORMAL,
                        0, 15728880);
                ty += tr.fontHeight;
            }
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
        boolean remove;
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