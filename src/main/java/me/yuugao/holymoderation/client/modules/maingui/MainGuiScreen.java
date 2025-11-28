package me.yuugao.holymoderation.client.modules.maingui;

import me.yuugao.holymoderation.client.util.serviceLocator.ServiceLocator;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.awt.Color;

public class MainGuiScreen extends Screen {
    protected MainGuiScreen() {
        super(Text.of("HolyModeration Main Gui Screen"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float tickDelta) {
        float mainGuiWidth = (float) context.getScaledWindowWidth() / 3;
        float mainGuiHeight = (float) context.getScaledWindowHeight() / 3;
        ServiceLocator.getRender2DService().drawRoundedRect(context.getMatrices(), (float) context.getScaledWindowWidth() / 2 - mainGuiWidth / 2, (float) context.getScaledWindowHeight() / 2 - mainGuiHeight / 2, mainGuiWidth, mainGuiHeight, 10, new Color(0x193AC5), 4);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        //tip
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}