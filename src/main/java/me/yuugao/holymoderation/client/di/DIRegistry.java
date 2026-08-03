package me.yuugao.holymoderation.client.di;

import me.yuugao.holymoderation.client.di.module.DIModule;

import java.util.ArrayList;
import java.util.List;

public class DIRegistry {
    private final List<DIModule> modules = new ArrayList<>();

    public DIRegistry addModule(DIModule module) {
        modules.add(module);
        return this;
    }

    public DIContainer build() {
        DIContainer container = new DIContainer();
        for (DIModule module : modules) {
            module.configure(container);
        }
        return container;
    }
}