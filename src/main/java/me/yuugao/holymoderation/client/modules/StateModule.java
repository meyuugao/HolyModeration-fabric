package me.yuugao.holymoderation.client.modules;

import me.yuugao.holymoderation.client.eventbus.Subscribe;
import me.yuugao.holymoderation.client.eventbus.event.ServerConnectEvent;
import me.yuugao.holymoderation.client.eventbus.event.ServerDisconnectEvent;

public class StateModule extends Module {
    @Subscribe
    public void onServerConnect(ServerConnectEvent event) {
        stateService.setOnHW(event.getServerInfo().address.matches("(?i).*hol(l)?yworld.*"));
        stateService.setConnected(true);
    }

    @Subscribe
    public void onServerDisconnect(ServerDisconnectEvent event) {
        if (stateService.isConnected()) {
            stateService.reset();
        }
    }
}