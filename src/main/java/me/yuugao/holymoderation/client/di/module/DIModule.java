package me.yuugao.holymoderation.client.di.module;

import me.yuugao.holymoderation.client.di.DIContainer;
import me.yuugao.obfuscator.DontObf;
import me.yuugao.obfuscator.ObfRule;

public interface DIModule {
    void configure(DIContainer container);
}