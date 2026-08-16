package me.yuugao.holymoderation.client.gui.screen;

import me.yuugao.holymoderation.client.gui.tabs.Tab;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.HashMap;

import lombok.Getter;

public abstract class GuiScreen extends Screen {
    protected final HashMap<String, Tab<? extends GuiScreen>> tabs = new HashMap<>();
    @Getter
    protected float x, y, width, height;
    @Getter
    protected float screenScale;

    protected GuiScreen(Text title) {
        super(title);
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
    }

    protected void renderTabs(DrawContext ctx, int relMouseX, int relMouseY, float tickDelta) {
        tabs.forEach((key, tab) -> tab.onRender(ctx, relMouseX, relMouseY, tickDelta));
    }
}