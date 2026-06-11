package me.yuugao.holymoderation.client.util.service.eventbus;

import me.yuugao.obfuscator.DontObf;
import me.yuugao.obfuscator.ObfRule;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@DontObf({ObfRule.GARBAGE_INJECT, ObfRule.MAP_METHOD})
public @interface Subscribe {
    int priority() default 0;
}