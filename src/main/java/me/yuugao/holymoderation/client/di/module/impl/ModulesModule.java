package me.yuugao.holymoderation.client.di.module.impl;

import me.yuugao.holymoderation.client.di.DIContainer;
import me.yuugao.holymoderation.client.di.module.DIModule;
import me.yuugao.holymoderation.client.modules.impl.*;

public class ModulesModule implements DIModule {
    @Override
    public void configure(DIContainer container) {
        container.register(StateModule.class, StateModule.class);
        container.register(GuiManagerModule.class, GuiManagerModule.class);
        container.register(MainGuiModule.class, MainGuiModule.class);
        container.register(CheckoutsModule.class, CheckoutsModule.class);
        container.register(NotificationsModule.class, NotificationsModule.class);
        container.register(KeyBindingModule.class, KeyBindingModule.class);
        container.register(MessageModule.class, MessageModule.class);
        container.register(NetModule.class, NetModule.class);
        container.register(PunishmentsModule.class, PunishmentsModule.class);
        container.register(ReportsParserModule.class, ReportsParserModule.class);
        container.register(SettingsModule.class, SettingsModule.class);
        container.register(SpyModule.class, SpyModule.class);
        container.register(TwinksCheckModule.class, TwinksCheckModule.class);
        container.register(WaterMarkModule.class, WaterMarkModule.class);
    }
}