package me.yuugao.holymoderation.client.gui.screen;

import me.yuugao.holymoderation.client.gui.tabs.Tab;

import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
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
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
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
    public boolean mouseClicked(Click click, boolean doubleClick) {
        if (click.button() != 0) return false;
        Tab<?> tab = tabs.get(activeTabKey);
        if (tab == null) return false;
        return tab.onMouseClick(toLocalX(click.x()), toLocalY(click.y()));
    }

    @Override
    public boolean mouseReleased(Click click) {
        Tab<?> tab = tabs.get(activeTabKey);
        if (tab == null) return false;
        tab.onMouseRelease();
        return false;
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        Tab<?> tab = tabs.get(activeTabKey);
        if (tab == null) return false;
        tab.onMouseDrag(toLocalX(click.x()), toLocalY(click.y()));
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        Tab<?> tab = tabs.get(activeTabKey);
        if (tab == null) return false;
        tab.onMouseScroll(horizontalAmount, verticalAmount, toLocalX(mouseX), toLocalY(mouseY));
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
    public boolean keyPressed(KeyInput input) {
        Tab<?> tab = tabs.get(activeTabKey);
        if (tab != null && tab.onKeyPress(input.key(), input.scancode(), GLFW.GLFW_PRESS, input.modifiers())) {
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        Tab<?> tab = tabs.get(activeTabKey);
        if (tab != null && tab.onCharTyped((char) input.codepoint())) {
            return true;
        }
        return super.charTyped(input);
    }
}
