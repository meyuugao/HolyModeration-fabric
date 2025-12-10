package me.yuugao.holymoderation.client.obfuscation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для исключения из обфускации.
 * Классы, методы и поля с этой аннотацией не будут обфусцированы.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.FIELD})
public @interface DontObf {
}
