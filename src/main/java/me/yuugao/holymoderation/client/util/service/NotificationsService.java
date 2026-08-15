package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.util.Colors;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

import org.joml.Matrix3x2fStack;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class NotificationsService {
    private final SoundService soundService;
    private final LoggerService loggerService;
    private final MinecraftService minecraftService;
    private final Render2DService render2DService;

    private final List<Notification> notificationPool = new ArrayList<>();
    private long lastNano = System.nanoTime();

    public void addNotification(NotificationType type, String title, String text, float liveTime) {
        notificationPool.add(new Notification(type, title, text, liveTime));
        soundService.playSound(type.getSoundName());
        if (type.equals(NotificationType.EXCEPTION)) {
            loggerService.exception("Исключение из уведомлений: %s".formatted(text));
        }
    }

    public void addNotification(NotificationType type, String title, String text, float liveTime, String soundName) {
        notificationPool.add(new Notification(type, title, text, liveTime));
        if (!soundName.isEmpty()) {
            soundService.playSound(soundName);
        }
    }

    public void clearNotifications() {
        notificationPool.clear();
    }

    public void showPreview() {
        if (notificationPool.stream().noneMatch(n -> n.isPreview)) {
            Notification n = new Notification(NotificationType.SUCCESS,
                    "%s%sПример уведомления".formatted(Colors.GREEN, Colors.BOLD),
                    "Скролл меняет размер уведомлений", Float.MAX_VALUE);
            n.isPreview = true;
            notificationPool.add(n);
        }
    }

    public void hidePreview() {
        notificationPool.removeIf(n -> n.isPreview);
    }

    public float getNotificationsStackHeight(float spacing) {
        float total = 0f;
        for (Notification n : notificationPool) {
            if (n.state != State.HIDING) {
                total += n.height + spacing;
            }
        }
        return Math.max(0f, total - spacing);
    }

    public void error(String text) {
        addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(Colors.RED, Colors.BOLD), text, 5f);
    }

    public void success(String text) {
        addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(Colors.GREEN, Colors.BOLD), text, 5f);
    }

    public void warning(String text) {
        addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(Colors.GOLD, Colors.BOLD), text, 5f);
    }

    public void error(String text, float liveTime) {
        addNotification(NotificationType.ERROR, "%s%sОшибка".formatted(Colors.RED, Colors.BOLD), text, liveTime);
    }

    public void success(String text, float liveTime) {
        addNotification(NotificationType.SUCCESS, "%s%sУспех".formatted(Colors.GREEN, Colors.BOLD), text, liveTime);
    }

    public void warning(String text, float liveTime) {
        addNotification(NotificationType.WARNING, "%s%sПредупреждение".formatted(Colors.GOLD, Colors.BOLD), text, liveTime);
    }

    public void renderNotificationsLocal(DrawContext ctx, int z, float stackDirY, float hideDirX, float hideDirY,
                                         float screenWidth, float screenHeight, float notifBaseWidth) {
        TextRenderer tr = minecraftService.getClient().textRenderer;
        long now = System.nanoTime();
        float delta = (now - lastNano) / 1_000_000_000f;
        lastNano = now;

        float margin = 8f;
        float spacing = 10f;
        float width = notifBaseWidth;
        float radius = 6f;
        float blur = 6f;
        float outline = 1.5f;
        float padding = 8f;

        for (Notification n : notificationPool) {
            n.ensureLayout(tr, width, padding);
        }

        for (Notification n : notificationPool) {
            n.elapsed += delta;
            if (n.state == State.IDLE && !n.isPreview && n.elapsed >= n.liveTime) {
                n.state = State.HIDING;
            }
        }

        notificationPool.removeIf(n -> {
            if (n.state != State.HIDING) return false;
            if (hideDirX > 0) return n.x > screenWidth + n.width;
            if (hideDirX < 0) return n.x + n.width < 0;
            if (hideDirY < 0) return n.y + n.height < 0;
            if (hideDirY > 0) return n.y > screenHeight;
            return false;
        });

        float cursor = margin;
        for (int i = notificationPool.size() - 1; i >= 0; i--) {
            Notification n = notificationPool.get(i);
            if (n.state != State.HIDING) {
                if (hideDirX > 0) {
                    n.targetX = screenWidth - margin - n.width;
                } else if (hideDirX < 0) {
                    n.targetX = margin;
                } else {
                    n.targetX = (screenWidth - n.width) / 2;
                }
                if (stackDirY < 0) {
                    n.targetY = screenHeight - cursor - n.height;
                } else if (stackDirY > 0) {
                    n.targetY = cursor;
                } else {
                    n.targetY = (screenHeight - n.height) / 2;
                }
                cursor += n.height + spacing;
            }
            if (n.state == State.HIDING) {
                if (hideDirX > 0) n.targetX = screenWidth + n.width + margin;
                else if (hideDirX < 0) n.targetX = -n.width - margin;
                if (hideDirY < 0) n.targetY = -n.height - margin;
                else if (hideDirY > 0) n.targetY = screenHeight + n.height + margin;
            }
        }

        for (Notification n : notificationPool) {
            if (n.state == State.SPAWNING && !n.initialized) {
                n.x = n.targetX;
                if (stackDirY < 0) n.y = screenHeight + n.height;
                else if (stackDirY > 0) n.y = -n.height;
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
            if (n.y + n.height < 0 || n.y > screenHeight) continue;
            Matrix3x2fStack ms = ctx.getMatrices();
            Color bg = n.type.getBg();
            Color ol = n.type.getOutline();
            render2DService.setupRender();
            ms.pushMatrix();
            render2DService.renderSoftRoundedRectOutline(ctx, n.x, n.y, n.width, n.height, z, radius, bg, ol, outline, blur);
            ms.translate(n.x, n.y);
            float ty = padding;
            for (OrderedText line : n.titleLines) {
                render2DService.renderText(tr, line, (int) padding, (int) ty, z, 0xFFFFFFFF, false, ctx);
                ty += tr.fontHeight;
            }
            ty += 4f;
            for (OrderedText line : n.bodyLines) {
                render2DService.renderText(tr, line, (int) padding, (int) ty, z, 0xFFFFFFFF, false, ctx);
                ty += tr.fontHeight;
            }
            ms.popMatrix();
            render2DService.endRender();
        }
    }

    private enum State {SPAWNING, IDLE, HIDING}

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
        boolean isPreview;
        State state = State.SPAWNING;
        List<OrderedText> titleLines = new ArrayList<>();
        List<OrderedText> bodyLines = new ArrayList<>();
        private int lastWrapWidth = Integer.MIN_VALUE;

        Notification(NotificationType type, String title, String text, float liveTime) {
            this.type = type;
            this.title = title;
            this.text = text;
            this.liveTime = liveTime;
        }

        void ensureLayout(TextRenderer tr, float notifWidth, float padding) {
            this.width = notifWidth;
            int wrap = Math.max(1, (int) (notifWidth - padding * 2));
            if (wrap == lastWrapWidth) return;
            lastWrapWidth = wrap;

            titleLines.clear();
            for (String s : title.split("\n")) {
                titleLines.addAll(tr.wrapLines(Text.literal(s), wrap));
            }
            bodyLines.clear();
            for (String s : text.split("\n")) {
                bodyLines.addAll(tr.wrapLines(Text.literal(s), wrap));
            }
            height = padding + titleLines.size() * tr.fontHeight + 4f + bodyLines.size() * tr.fontHeight + padding;
        }
    }
}