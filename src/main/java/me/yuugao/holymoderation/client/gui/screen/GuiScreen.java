package me.yuugao.holymoderation.client.gui.screen;

import me.yuugao.holymoderation.client.gui.tabs.Tab;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.HashMap;

import lombok.Getter;
import obfuscator.DontObf;
import obfuscator.ObfRule;

public abstract class GuiScreen extends Screen {
    @Getter
    protected float x, y, width, height;
    protected final ServiceContext serviceContext;
    protected final HashMap<String, Tab> tabs = new HashMap<>();

    protected GuiScreen(Text title, ServiceContext serviceContext) {
        super(title);

        this.serviceContext = serviceContext;
    }

    @Override
    @DontObf(ObfRule.MAP_METHOD)
    public void render(DrawContext context, int mouseX, int mouseY, float tickDelta) {
        tabs.forEach((key, tab) -> tab.onRender(context, mouseX, mouseY, tickDelta));

        super.render(context, mouseX, mouseY, tickDelta);
    }
}