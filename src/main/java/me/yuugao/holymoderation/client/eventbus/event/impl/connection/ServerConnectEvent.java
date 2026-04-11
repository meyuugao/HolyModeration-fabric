package me.yuugao.holymoderation.client.eventbus.event.impl.connection;

import me.yuugao.holymoderation.client.eventbus.event.Event;

import net.minecraft.client.network.ServerInfo;

import lombok.Getter;

@Getter
public class ServerConnectEvent extends Event {
    private final ServerInfo serverInfo;
    private final boolean isSwitch;

    public ServerConnectEvent(ServerInfo serverInfo, boolean isSwitch) {
        this.serverInfo = serverInfo;
        this.isSwitch = isSwitch;
    }
}