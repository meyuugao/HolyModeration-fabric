package me.yuugao.holymoderation.client.di.annotations;

import me.yuugao.obfuscator.DontObf;
import me.yuugao.obfuscator.ObfRule;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Singleton {
}