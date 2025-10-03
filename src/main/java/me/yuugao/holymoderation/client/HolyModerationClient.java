package me.yuugao.holymoderation.client;

import me.yuugao.holymoderation.client.config.Config;
import me.yuugao.holymoderation.client.util.Session;
import me.yuugao.holymoderation.client.util.State;

import net.fabricmc.api.ClientModInitializer;

public class HolyModerationClient implements ClientModInitializer {
    public static final Config CONFIG = new Config();
    public static final State STATE = new State();
    public static final Session SESSION = new Session();

    @Override
    public void onInitializeClient() {
    }
}