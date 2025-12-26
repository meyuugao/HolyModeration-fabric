package me.yuugao.holymoderation.client.gui.screen;

import me.yuugao.holymoderation.client.gui.tabs.main.GeneralTab;
import me.yuugao.holymoderation.client.util.serviceLocator.ServiceContext;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

import java.awt.Color;

import obfuscator.DontObf;
import obfuscator.ObfRule;

public class MainGuiScreen extends AnimatedGuiScreen {
    private final Color outlineColor = Color.WHITE;

    public MainGuiScreen(ServiceContext serviceContext) {
        super(Text.of("HolyModeration Main Gui Screen"), serviceContext);

        this.tabs.put("General", new GeneralTab(this, serviceContext));
    }

    @Override
    @DontObf(ObfRule.MAP_METHOD)
    public void render(DrawContext context, int mouseX, int mouseY, float tickDelta) {
        float targetW = (float) context.getScaledWindowWidth() / 2.2f;
        float targetH = (float) context.getScaledWindowHeight() / 1.8f;

        this.width = targetW * getAnimValue();
        this.height = targetH * getAnimValue();

        float cx = (float) context.getScaledWindowWidth() / 2;
        float cy = (float) context.getScaledWindowHeight() / 2;

        this.x = cx - this.width / 2;
        this.y = cy - this.height / 2;

        float baseOutline = 1f;
        float scaleFactor = Math.min(this.width, this.height) / 100f;
        float scaledOutline = baseOutline * scaleFactor;

        serviceContext.getRender2DService().renderSoftRoundedRectOutline(
                context.getMatrices(),
                this.x, this.y, Math.max(1, this.width), Math.max(1, this.height),
                10f,
                new Color(0x002AFF),
                outlineColor,
                scaledOutline, 3
        );

        super.render(context, mouseX, mouseY, tickDelta);
    }
}