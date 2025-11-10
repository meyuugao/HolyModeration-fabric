package me.yuugao.holymoderation.client.eventbus.event;

import net.minecraft.client.network.ServerInfo;

import lombok.Getter;

@Getter
public class ServerConnectEvent extends Event {
    private final ServerInfo serverInfo;

    public ServerConnectEvent(ServerInfo serverInfo) {
        this.serverInfo = serverInfo;
    }
}