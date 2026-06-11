package me.yuugao.holymoderation.client.gui.drawable.render;

import me.yuugao.obfuscator.DontObf;
import me.yuugao.obfuscator.ObfRule;

@DontObf({ObfRule.MAP_CLASS, ObfRule.MAP_METHOD, ObfRule.MAP_FIELD, ObfRule.MAP_LOCALVARS})
public enum RenderMode {
    LIVE,
    CONFIG
}