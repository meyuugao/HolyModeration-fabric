package obfuscator.modules;

import org.objectweb.asm.commons.Remapper;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import obfuscator.ObfContext;
import obfuscator.ObfRule;

public class ObfRemapperModule extends Remapper {
    private final ObfContext ctx;

    public ObfRemapperModule(ObfContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public String map(String internalName) {
        if (!internalName.startsWith(ctx.mainPrefix)) return internalName;

        EnumSet<ObfRule> classRules = ctx.dontObfRules.getOrDefault(internalName, EnumSet.noneOf(ObfRule.class));
        if (classRules.contains(ObfRule.MAP_CLASS)) return internalName;

        if (ctx.classMap.containsKey(internalName)) return ctx.classMap.get(internalName);

        return internalName;
    }

    @Override
    public String mapMethodName(String owner, String name, String descriptor) {
        if (!owner.startsWith(ctx.mainPrefix)) return name;

        for (String o : getOwnersToCheck(owner)) {
            EnumSet<ObfRule> classRules = ctx.dontObfRules.getOrDefault(o, EnumSet.noneOf(ObfRule.class));
            String key = "%s.%s%s".formatted(o, name, descriptor);
            EnumSet<ObfRule> methodRules = ctx.dontObfRules.getOrDefault(key, EnumSet.noneOf(ObfRule.class));
            EnumSet<ObfRule> combined = EnumSet.copyOf(classRules);
            combined.addAll(methodRules);

            if (combined.contains(ObfRule.MAP_METHOD)) return name;

            if (ctx.methodMap.containsKey(key)) return ctx.methodMap.get(key);
        }

        return name;
    }

    @Override
    public String mapFieldName(String owner, String name, String descriptor) {
        if (!owner.startsWith(ctx.mainPrefix)) return name;

        for (String o : getOwnersToCheck(owner)) {
            EnumSet<ObfRule> classRules = ctx.dontObfRules.getOrDefault(o, EnumSet.noneOf(ObfRule.class));
            String key = "%s.%s".formatted(o, name);
            EnumSet<ObfRule> fieldRules = ctx.dontObfRules.getOrDefault(key, EnumSet.noneOf(ObfRule.class));
            EnumSet<ObfRule> combined = EnumSet.copyOf(classRules);
            combined.addAll(fieldRules);

            if (combined.contains(ObfRule.MAP_FIELD)) return name;

            if (ctx.fieldMap.containsKey(key)) return ctx.fieldMap.get(key);
        }

        return name;
    }

    private Set<String> getOwnersToCheck(String owner) {
        Set<String> ownersToCheck = new HashSet<>();
        ownersToCheck.add(owner);
        if (ctx.superClasses.containsKey(owner)) {
            ownersToCheck.addAll(ctx.superClasses.get(owner));
        }

        return ownersToCheck;
    }
}