package obfuscator.modules;

import org.objectweb.asm.commons.Remapper;
import obfuscator.ObfRule;
import java.util.HashSet;
import java.util.Set;

public class ObfRemapper extends Remapper {
    private final ObfContext ctx;

    public ObfRemapper(ObfContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public String map(String internalName) {
        if (!internalName.startsWith(ctx.mainPrefix)) return internalName;
        java.util.EnumSet<ObfRule> classRules = ctx.dontObfRules.getOrDefault(internalName, java.util.EnumSet.noneOf(ObfRule.class));
        if (classRules.contains(ObfRule.RENAME_CLASS)) return internalName;
        if (ctx.classMap.containsKey(internalName)) return ctx.classMap.get(internalName);
        return internalName;
    }

    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        if (!owner.startsWith(ctx.mainPrefix)) return name;
        Set<String> ownersToCheck = new HashSet<>();
        ownersToCheck.add(owner);
        if (ctx.superClasses.containsKey(owner)) ownersToCheck.addAll(ctx.superClasses.get(owner));
        for (String o : ownersToCheck) {
            java.util.EnumSet<ObfRule> classRules = ctx.dontObfRules.getOrDefault(o, java.util.EnumSet.noneOf(ObfRule.class));
            String key = o + "." + name + descriptor;
            java.util.EnumSet<ObfRule> methodRules = ctx.dontObfRules.getOrDefault(key, java.util.EnumSet.noneOf(ObfRule.class));
            java.util.EnumSet<ObfRule> combined = java.util.EnumSet.copyOf(classRules);
            combined.addAll(methodRules);
            if (combined.contains(ObfRule.RENAME_METHOD)) return name;
            if (ctx.methodMap.containsKey(key)) return ctx.methodMap.get(key);
        }
        return name;
    }

    @Override
    public String mapFieldName(String owner, String name, String descriptor) {
        if (!owner.startsWith(ctx.mainPrefix)) return name;
        Set<String> ownersToCheck = new HashSet<>();
        ownersToCheck.add(owner);
        if (ctx.superClasses.containsKey(owner)) ownersToCheck.addAll(ctx.superClasses.get(owner));
        for (String o : ownersToCheck) {
            java.util.EnumSet<ObfRule> classRules = ctx.dontObfRules.getOrDefault(o, java.util.EnumSet.noneOf(ObfRule.class));
            String key = o + "." + name;
            java.util.EnumSet<ObfRule> fieldRules = ctx.dontObfRules.getOrDefault(key, java.util.EnumSet.noneOf(ObfRule.class));
            java.util.EnumSet<ObfRule> combined = java.util.EnumSet.copyOf(classRules);
            combined.addAll(fieldRules);
            if (combined.contains(ObfRule.RENAME_FIELD)) return name;
            if (ctx.fieldMap.containsKey(key)) return ctx.fieldMap.get(key);
        }
        return name;
    }
}