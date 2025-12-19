package obfuscator.modules;

import static obfuscator.modules.LoggerModule.log;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;

import obfuscator.ObfContext;
import obfuscator.ObfRule;

public final class MappingGeneratorModule {
    private MappingGeneratorModule() {
    }

    public static void generate(ObfContext ctx) {
        for (Map.Entry<String, byte[]> entry : ctx.classBytes.entrySet()) {
            String internalName = entry.getKey();
            if (internalName.equals(ctx.dontObfAnnotationClass)) continue;
            ClassNode cn = ctx.classNodes.get(internalName);
            int idx = internalName.lastIndexOf('/');
            String packageName = idx > 0 ? internalName.substring(0, idx) : "";

            List<String> prefixes = new ArrayList<>(Arrays.asList(ctx.protectedPrefixes));
            prefixes.sort((a, b) -> Integer.compare(b.length(), a.length()));

            if (!ctx.classMap.containsKey(internalName)) {
                if (internalName.contains("$")) {
                    String outer = internalName.substring(0, internalName.indexOf('$'));
                    String innerName = internalName.substring(internalName.indexOf('$') + 1);

                    EnumSet<ObfRule> classRules =
                            ctx.dontObfRules.getOrDefault(internalName, EnumSet.noneOf(ObfRule.class));
                    boolean blockRename = classRules.contains(ObfRule.MAP_CLASS);

                    String mappedOuter = ctx.classMap.get(outer);
                    if (mappedOuter == null) {
                        String pkg = outer.contains("/") ? outer.substring(0, outer.lastIndexOf('/')) : "";
                        String genOuter = NameGeneratorModule.generateLatName();

                        boolean matched = false;
                        for (String prefix : prefixes) {
                            if (pkg.startsWith(prefix)) {
                                String[] parts = pkg.substring(prefix.length()).split("/");
                                StringBuilder sb = new StringBuilder(prefix);
                                for (String p : parts)
                                    if (!p.isEmpty()) sb.append("/").append(NameGeneratorModule.generateLatName());
                                sb.append("/").append(genOuter);
                                mappedOuter = sb.toString();
                                matched = true;
                                break;
                            }
                        }

                        if (!matched) {
                            StringBuilder sb = new StringBuilder();
                            for (String p : pkg.split("/"))
                                if (!p.isEmpty()) sb.append(NameGeneratorModule.generateLatName()).append("/");
                            sb.append(genOuter);
                            mappedOuter = sb.toString();
                        }

                        ctx.classMap.put(outer, mappedOuter);
                        log("Новый класс: %s -> %s".formatted(outer, mappedOuter));
                    }

                    String mappedInner = blockRename
                            ? innerName
                            : NameGeneratorModule.generateLatName();

                    String finalName = "%s$%s".formatted(mappedOuter, mappedInner);
                    ctx.classMap.put(internalName, finalName);

                    log("Новый класс: %s -> %s".formatted(internalName, finalName));
                } else {
                    String newName = "";
                    if (packageName.startsWith(ctx.mixinPrefix)) {
                        ctx.classMap.computeIfAbsent(ctx.mixinPrefix, k -> "%s/%s".formatted(ctx.mainClientPrefix, NameGeneratorModule.generateLatName()));
                        newName = "%s/%s".formatted(ctx.classMap.get(ctx.mixinPrefix), NameGeneratorModule.generateLatName());
                    } else {
                        boolean matched = false;
                        for (String prefix : prefixes) {
                            if (packageName.startsWith(prefix)) {
                                String[] parts = packageName.substring(prefix.length()).split("/");
                                StringBuilder sb = new StringBuilder(prefix);
                                for (String p : parts)
                                    if (!p.isEmpty()) sb.append("/").append(NameGeneratorModule.generateLatName());
                                newName = "%s/%s".formatted(sb, NameGeneratorModule.generateLatName());
                                matched = true;
                                break;
                            }
                        }
                        if (!matched) {
                            StringBuilder sb = new StringBuilder();
                            for (String p : packageName.split("/"))
                                if (!p.isEmpty()) sb.append(NameGeneratorModule.generateLatName()).append("/");
                            newName = "%s%s".formatted(sb, NameGeneratorModule.generateLatName());
                        }
                    }
                    ctx.classMap.put(internalName, newName);
                    log("Новый класс: %s -> %s".formatted(internalName, newName));
                }
            } else {
                log("Маппинг уже существует: %s -> %s".formatted(internalName, ctx.classMap.get(internalName)));
            }

            for (FieldNode fn : cn.fields) {
                if ((fn.access & Opcodes.ACC_SYNTHETIC) != 0) continue;

                String fieldKeyFull = "%s.%s".formatted(internalName, fn.name);
                if (getCombinedRules(ctx, internalName, fieldKeyFull).contains(ObfRule.MAP_FIELD)) {
                    ctx.dontObfFields.add(fieldKeyFull);
                    continue;
                }

                if (!ctx.fieldMap.containsKey(fieldKeyFull)) {
                    String genField = NameGeneratorModule.generateChineseName();
                    ctx.fieldMap.put(fieldKeyFull, genField);
                    String mappedOwner = ctx.classMap.getOrDefault(internalName, internalName);
                    String fieldKeyMapped = "%s.%s".formatted(mappedOwner, fn.name);
                    ctx.fieldMap.put(fieldKeyMapped, genField);
                    log("Новое поле: %s -> %s".formatted(fn.name, genField));
                }
            }

            for (MethodNode mn : cn.methods) {
                String methodKey = "%s.%s%s".formatted(internalName, mn.name, mn.desc);
                EnumSet<ObfRule> combinedRules = getCombinedRules(ctx, internalName, methodKey);
                if (combinedRules.contains(ObfRule.MAP_METHOD)) {
                    ctx.dontObfMethods.add(methodKey);
                    continue;
                }

                if (!Objects.equals(mn.name, "<init>") && !Objects.equals(mn.name, "<clinit>")) {
                    if (!ctx.methodMap.containsKey(methodKey)) {
                        String genName = NameGeneratorModule.generateChineseName();
                        ctx.methodMap.put(methodKey, genName);
                        String mappedOwner = ctx.classMap.getOrDefault(internalName, internalName);
                        String genKeyMapped = "%s.%s%s".formatted(mappedOwner, mn.name, mn.desc);
                        ctx.methodMap.put(genKeyMapped, genName);
                        log("Новый метод: %s -> %s".formatted(methodKey, ctx.methodMap.get(methodKey)));
                    }
                }

                List<String> paramTypes = RemapperModule.parseMethodDescriptor(mn.desc);
                boolean blockLocalRename = combinedRules.contains(ObfRule.MAP_LOCALVARS);
                if (paramTypes != null && !paramTypes.isEmpty() && mn.localVariables != null && !blockLocalRename) {
                    int paramIndex = ((mn.access & Opcodes.ACC_STATIC) == 0) ? 1 : 0;
                    for (LocalVariableNode localVar : mn.localVariables) {
                        int idx1 = localVar.index;
                        if (idx1 >= paramIndex && idx1 < paramIndex + paramTypes.size()) {
                            String obfClassName = ctx.classMap.getOrDefault(internalName, internalName);
                            String obfMethodName = ctx.methodMap.containsKey(methodKey) ? ctx.methodMap.get(methodKey) : mn.name;
                            String paramKey = "%s.%s.%d".formatted(obfClassName, obfMethodName, idx1 - paramIndex);
                            String originalParamKey = "%s.%s.%d".formatted(internalName, obfMethodName, idx1 - paramIndex);
                            if (!ctx.paramMap.containsKey(paramKey)) {
                                String genParam = NameGeneratorModule.generateChineseName();
                                ctx.paramMap.put(paramKey, genParam);
                                ctx.paramMap.put(originalParamKey, genParam);
                                log("Новый аргумент: %s %s -> %s".formatted(obfMethodName, localVar.name, genParam));
                            }
                        }
                    }
                }
            }
        }
    }

    private static EnumSet<ObfRule> getCombinedRules(ObfContext ctx, String internalName, String key) {
        EnumSet<ObfRule> methodRules =
                ctx.dontObfRules.getOrDefault(key, EnumSet.noneOf(ObfRule.class));
        EnumSet<ObfRule> classRules =
                ctx.dontObfRules.getOrDefault(internalName, EnumSet.noneOf(ObfRule.class));
        EnumSet<ObfRule> combinedMethodRules = EnumSet.copyOf(classRules);
        combinedMethodRules.addAll(methodRules);
        return combinedMethodRules;
    }
}