package me.yuugao.holymoderation.client.util.serviceLocator.service;

import me.yuugao.holymoderation.client.modules.drawable.DrawableModule;

import java.util.ArrayList;

import lombok.Getter;

@Getter
public class GuiManagerService extends Service {
    private final ArrayList<DrawableModule<?>> drawableModules = new ArrayList<>();

    public void addDrawableModule(DrawableModule<?> drawableModule) {
        for (int i = 0; i < drawableModules.size(); i++) {
            if (drawableModule.getRenderPriority() < drawableModules.get(i).getRenderPriority()) {
                drawableModules.add(i, drawableModule);
                return;
            }
        }
        drawableModules.add(drawableModule);
    }

    public void clearDrawableModules() {
        drawableModules.clear();
    }
}