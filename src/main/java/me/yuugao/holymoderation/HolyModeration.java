package me.yuugao.holymoderation;

import me.yuugao.obfuscator.DontObf;
import me.yuugao.obfuscator.ObfRule;

import net.fabricmc.api.ModInitializer;

public class HolyModeration implements ModInitializer {
    @Override
    @DontObf(ObfRule.MAP_METHOD)
    public void onInitialize() {
    }
}