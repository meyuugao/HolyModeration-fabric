package obfuscator.modules;

import org.objectweb.asm.commons.Remapper;

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
            String key = o + "." + name + descriptor;
            if (ctx.dontObfClasses.contains(o)) return name;
            if (ctx.dontObfMethods.contains(key)) return name;
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
            String key = o + "." + name;
            if (ctx.dontObfClasses.contains(o)) return name;
            if (ctx.dontObfFields.contains(key)) return name;
            if (ctx.fieldMap.containsKey(key)) return ctx.fieldMap.get(key);
        }
        return name;
    }
}