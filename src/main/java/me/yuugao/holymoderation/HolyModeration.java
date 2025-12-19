package me.yuugao.holymoderation;

import net.fabricmc.api.ModInitializer;

import obfuscator.DontObf;
import obfuscator.ObfRule;

public class HolyModeration implements ModInitializer {
    @Override
    @DontObf(ObfRule.MAP_METHOD)
    public void onInitialize() {
    }
}