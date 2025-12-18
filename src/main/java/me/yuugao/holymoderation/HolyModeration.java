package me.yuugao.holymoderation;

import obfuscator.DontObf;
import obfuscator.ObfRule;

import net.fabricmc.api.ModInitializer;

public class HolyModeration implements ModInitializer {
    @Override
    @DontObf(ObfRule.RENAME_METHOD)
    public void onInitialize() {
    }
}