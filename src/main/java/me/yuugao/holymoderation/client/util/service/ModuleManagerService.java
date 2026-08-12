package me.yuugao.holymoderation.client.util.service;

import me.yuugao.holymoderation.client.di.DIContainer;
import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.modules.DrawableModule;
import me.yuugao.holymoderation.client.modules.impl.*;
import me.yuugao.holymoderation.client.util.command.CommandProvider;
import me.yuugao.holymoderation.client.util.command.CommandRegistry;
import me.yuugao.holymoderation.client.util.service.eventbus.EventBusService;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Singleton
public class ModuleManagerService {
    private final EventBusService eventBusService;
    private final GuiManagerService guiManagerService;
    private final LoggerService loggerService;
    private final DIContainer di;
    private final CommandRegistry commandRegistry;

    private final Map<Class<?>, Object> activeModules = new LinkedHashMap<>();

    public void registerAll() {
        register(StateModule.class);
        register(GuiManagerModule.class);
        register(MainGuiModule.class);
        register(CheckoutsModule.class);
        register(NotificationsModule.class);
        register(KeyBindingModule.class);
        register(MessageModule.class);
        register(NetModule.class);
        register(PunishmentsModule.class);
        register(ReportsParserModule.class);
        register(SettingsModule.class);
        register(SpyModule.class);
        register(TwinksCheckModule.class);
        register(WaterMarkModule.class);
        loggerService.info("All modules registered.");
    }

    public <T> void register(Class<T> moduleClass) {
        if (activeModules.containsKey(moduleClass)) {
            activeModules.get(moduleClass);
            return;
        }

        T module = di.get(moduleClass);
        eventBusService.getEventBus().register(module);
        activeModules.put(moduleClass, module);

        if (module instanceof DrawableModule<?> drawable) {
            guiManagerService.addDrawableModule(drawable);
        }

        if (module instanceof CommandProvider provider) {
            provider.registerCommands(commandRegistry);
        }

        loggerService.debug("Module registered: %s".formatted(moduleClass.getSimpleName()));
    }

    public void unregister(Class<?> moduleClass) {
        Object module = activeModules.remove(moduleClass);
        if (module != null) {
            eventBusService.getEventBus().unregister(module);
            if (module instanceof DrawableModule<?> drawable) {
                guiManagerService.removeDrawableModule(drawable);
            }
            loggerService.debug("Module unregistered: %s".formatted(moduleClass.getSimpleName()));
        }
    }

    public void unregisterAll() {
        for (Class<?> moduleClass : new ArrayList<>(activeModules.keySet())) {
            unregister(moduleClass);
        }
        loggerService.info("All modules unregistered.");
    }

    @SuppressWarnings("unchecked")
    public <T> T getModule(Class<T> moduleClass) {
        return (T) activeModules.get(moduleClass);
    }

    public boolean isRegistered(Class<?> moduleClass) {
        return activeModules.containsKey(moduleClass);
    }
}