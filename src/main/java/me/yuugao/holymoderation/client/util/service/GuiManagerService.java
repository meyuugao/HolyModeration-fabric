package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.modules.DrawableModule;

import java.util.ArrayList;
import java.util.Comparator;

import lombok.Getter;

@Getter
@Singleton
public class GuiManagerService {
    private final ArrayList<DrawableModule<?>> drawableModules = new ArrayList<>();

    public void addDrawableModule(DrawableModule<?> drawableModule) {
        if (drawableModules.stream().anyMatch(m -> m.getClass() == drawableModule.getClass())) return;

        drawableModules.add(drawableModule);
        drawableModules.sort(Comparator.comparingInt(DrawableModule::getRenderPriority));
    }

    public void removeDrawableModule(DrawableModule<?> drawableModule) {
        drawableModules.remove(drawableModule);
    }
}