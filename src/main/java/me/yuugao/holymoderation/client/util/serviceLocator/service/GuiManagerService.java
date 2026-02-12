package me.yuugao.holymoderation.client.util.serviceLocator.service;

import me.yuugao.holymoderation.client.modules.drawable.DrawableModule;

import java.util.ArrayList;
import java.util.Comparator;

import lombok.Getter;

@Getter
public class GuiManagerService extends Service {
    private final ArrayList<DrawableModule<?>> drawableModules = new ArrayList<>();

    public void addDrawableModule(DrawableModule<?> drawableModule) {
        if (drawableModules.stream().anyMatch(m -> m.getClass() == drawableModule.getClass())) return;

        drawableModules.add(drawableModule);
        drawableModules.sort(Comparator.comparingInt(DrawableModule::getRenderPriority));
    }
}