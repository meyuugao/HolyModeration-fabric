package me.yuugao.holymoderation.client.util.service.state;

import me.yuugao.holymoderation.client.di.annotations.Inject;
import me.yuugao.holymoderation.client.di.annotations.Singleton;
import me.yuugao.holymoderation.client.modules.impl.CheckoutsModule;
import me.yuugao.holymoderation.client.modules.impl.GuiManagerModule;
import me.yuugao.holymoderation.client.modules.impl.NetModule;
import me.yuugao.holymoderation.client.modules.impl.NotificationsModule;
import me.yuugao.holymoderation.client.modules.impl.StateModule;
import me.yuugao.holymoderation.client.util.service.LoggerService;
import me.yuugao.holymoderation.client.util.service.ModuleManagerService;
import me.yuugao.holymoderation.client.util.service.NotificationType;
import me.yuugao.holymoderation.client.util.service.NotificationsService;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@RequiredArgsConstructor(onConstructor_ = @Inject)
@Getter
@Setter
@Singleton
public class ModStateService {
    @Getter(AccessLevel.NONE)
    private final LoggerService loggerService;
    @Getter(AccessLevel.NONE)
    private final ModuleManagerService moduleManagerService;
    @Getter(AccessLevel.NONE)
    private final NotificationsService notificationsService;

    private boolean debugEnabled = false;
    private boolean enabled = true;
    private boolean blocked = false;
    private boolean forceBlocked = false;
    private boolean onlineMode = false;
    private boolean updateBlocked = false;

    public void enableDebug() {
        this.debugEnabled = true;
        ((Logger) LogManager.getRootLogger()).setLevel(Level.DEBUG);
        loggerService.setLevel(Level.DEBUG);
    }

    public void disableDebug() {
        this.debugEnabled = false;
        loggerService.setLevel(Level.INFO);
    }

    public boolean enable() {
        if (updateBlocked) return false;
        if (!enabled) {
            enabled = true;
            registerEventListeners();

            return true;
        } else {
            return false;
        }
    }

    public boolean disable() {
        if (updateBlocked) return false;
        if (enabled) {
            enabled = false;
            unregisterEventListeners();

            return true;
        } else {
            return false;
        }
    }

    public void forceBlock() {
        forceBlocked = blocked = true;
        moduleManagerService.unregisterAll();
    }

    public void block() {
        blocked = true;
        unregisterEventListeners();
    }

    public void unblock() {
        blocked = false;
        registerEventListeners();
    }

    public void blockForUpdate() {
        if (updateBlocked) return;
        updateBlocked = true;
        blocked = true;
        moduleManagerService.unregisterAll();
        moduleManagerService.register(StateModule.class);
        moduleManagerService.register(GuiManagerModule.class);
        moduleManagerService.register(CheckoutsModule.class);
        moduleManagerService.register(NotificationsModule.class);
        moduleManagerService.register(NetModule.class);

        notificationsService.addNotification(NotificationType.WARNING,
                "Ограниченный режим",
                "Доступна новая версия мода, поэтому часть функционала заблокирована. " +
                        "Остались доступны только проверки (заморозка, автобан). " +
                        "Обновите мод, чтобы вернуть весь функционал.",
                3600f);
    }

    public void unblockForUpdate() {
        updateBlocked = false;
        blocked = false;
        registerEventListeners();

        notificationsService.addNotification(NotificationType.SUCCESS,
                "Мод обновлён",
                "Весь функционал снова доступен!",
                5f);
    }

    public void registerEventListeners() {
        moduleManagerService.registerAll();
    }

    public void unregisterEventListeners() {
        moduleManagerService.unregisterAll();
        moduleManagerService.register(StateModule.class);
        moduleManagerService.register(GuiManagerModule.class);
        moduleManagerService.register(NotificationsModule.class);
    }
}
