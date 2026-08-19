package me.yuugao.holymoderation.client.gui.screen;

import me.yuugao.holymoderation.client.gui.tabs.Tab;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;

import lombok.Getter;

public abstract class GuiScreen extends Screen {
    protected final HashMap<String, Tab<? extends GuiScreen>> tabs = new HashMap<>();
    protected final ArrayList<String> tabOrder = new ArrayList<>();
    @Getter
    protected String activeTabKey;
    @Getter
    protected float x, y, width, height;
    @Getter
    protected float screenScale;

    protected GuiScreen(Text title) {
        super(title);
    }

    protected void addTab(String key, Tab<? extends GuiScreen> tab) {
        tabs.put(key, tab);
        if (!tabOrder.contains(key)) {
            tabOrder.add(key);
        }
        if (activeTabKey == null) {
            activeTabKey = key;
        }
    }

    @Override
    public void renderBackground(DrawContext context) {
    }

    protected void renderTabs(DrawContext ctx, int relMouseX, int relMouseY, float tickDelta) {
        Tab<?> tab = tabs.get(activeTabKey);
        if (tab != null) {
            tab.onRender(ctx, relMouseX, relMouseY, tickDelta);
        }
    }

    protected float toLocalX(double mouseX) {
        if (screenScale <= 0f) return (float) mouseX;
        return (float) ((mouseX - x) / screenScale);
    }

    protected float toLocalY(double mouseY) {
        if (screenScale <= 0f) return (float) mouseY;
        return (float) ((mouseY - y) / screenScale);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        Tab<?> tab = tabs.get(activeTabKey);
        if (tab == null) return false;
        return tab.onMouseClick(toLocalX(mouseX), toLocalY(mouseY));
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Tab<?> tab = tabs.get(activeTabKey);
        if (tab == null) return false;
        tab.onMouseRelease();
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        Tab<?> tab = tabs.get(activeTabKey);
        if (tab == null) return false;
        tab.onMouseDrag(toLocalX(mouseX), toLocalY(mouseY));
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
        Tab<?> tab = tabs.get(activeTabKey);
        if (tab == null) return false;
        tab.onMouseScroll(0, amount, toLocalX(mouseX), toLocalY(mouseY));
        return false;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        Tab<?> tab = tabs.get(activeTabKey);
        if (tab != null) {
            tab.onMouseMoved(toLocalX(mouseX), toLocalY(mouseY));
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        Tab<?> tab = tabs.get(activeTabKey);
        if (tab != null && tab.onKeyPress(keyCode, scanCode, GLFW.GLFW_PRESS, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        Tab<?> tab = tabs.get(activeTabKey);
        if (tab != null && tab.onCharTyped(chr)) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }
}
