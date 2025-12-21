package me.yuugao.holymoderation.client.eventbus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import obfuscator.DontObf;
import obfuscator.ObfRule;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@DontObf({ObfRule.GARBAGE_INJECT, ObfRule.MAP_METHOD})
public @interface Subscribe {
    int priority() default 0;
}