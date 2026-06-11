package me.yuugao.holymoderation.client.util.service.eventbus.event.impl.connection;

import me.yuugao.holymoderation.client.util.service.eventbus.event.Event;

import net.minecraft.client.network.ServerInfo;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class ServerConnectEvent extends Event {
    private final ServerInfo serverInfo;
    private final boolean isSwitch;
}