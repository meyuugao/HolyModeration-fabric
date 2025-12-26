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
                    EnumSet<ObfRule> classRules =
                            ctx.dontObfRules.getOrDefault(internalName, EnumSet.noneOf(ObfRule.class));
                    if (classRules.contains(ObfRule.MAP_CLASS)) {
                        ctx.classMap.put(internalName, internalName);
                        log("Новый класс: %s -> %s".formatted(internalName, internalName));
                        continue;
                    }

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

                String owner = findFieldOwner(ctx, cn, fn.name, fn.desc);
                if (owner == null) owner = cn.name;

                String fieldKey = "%s.%s".formatted(owner, fn.name + fn.desc);

                if (getCombinedRules(ctx, cn.name, fieldKey).contains(ObfRule.MAP_FIELD)) {
                    ctx.dontObfFields.add(fieldKey);
                    continue;
                }

                if (!ctx.fieldMap.containsKey(fieldKey)) {
                    String genField = NameGeneratorModule.generateChineseName();
                    ctx.fieldMap.put(fieldKey, genField);
                    log("Новое поле: %s -> %s".formatted(fieldKey, genField));
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
                        String overriddenOwner = findOverriddenMethodOwner(ctx, cn, mn);

                        if (overriddenOwner != null) {
                            String parentKey = "%s.%s%s".formatted(overriddenOwner, mn.name, mn.desc);
                            String mappedName = ctx.methodMap.get(parentKey);

                            if (mappedName != null) {
                                ctx.methodMap.put(methodKey, mappedName);
                            }
                        } else {
                            if (!ctx.methodMap.containsKey(methodKey)) {
                                String genName = NameGeneratorModule.generateChineseName();
                                ctx.methodMap.put(methodKey, genName);

                                String mappedOwner = ctx.classMap.getOrDefault(internalName, internalName);
                                ctx.methodMap.put("%s.%s%s".formatted(mappedOwner, mn.name, mn.desc), genName);

                                propagateMethodToChildren(ctx, internalName, mn.name, mn.desc, genName);
                            }
                        }

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

    private static String findOverriddenMethodOwner(ObfContext ctx, ClassNode cn, MethodNode mn) {
        Queue<String> toCheck = new ArrayDeque<>();
        if (cn.superName != null) toCheck.add(cn.superName);
        if (cn.interfaces != null) toCheck.addAll(cn.interfaces);

        while (!toCheck.isEmpty()) {
            String parent = toCheck.poll();
            ClassNode parentNode = ctx.classNodes.get(parent);
            if (parentNode == null) continue;

            for (MethodNode pm : parentNode.methods) {
                if (pm.name.equals(mn.name) && pm.desc.equals(mn.desc)) {
                    return parent;
                }
            }

            if (parentNode.superName != null) toCheck.add(parentNode.superName);
            if (parentNode.interfaces != null) toCheck.addAll(parentNode.interfaces);
        }
        return null;
    }

    private static String findFieldOwner(ObfContext ctx, ClassNode cn, String fieldName, String desc) {
        Queue<String> q = new ArrayDeque<>();
        q.add(cn.name);

        while (!q.isEmpty()) {
            String cur = q.poll();
            ClassNode node = ctx.classNodes.get(cur);
            if (node == null) continue;

            for (FieldNode fn : node.fields) {
                if (fn.name.equals(fieldName) && fn.desc.equals(desc)) {
                    return cur;
                }
            }

            if (node.superName != null) q.add(node.superName);
            if (node.interfaces != null) q.addAll(node.interfaces);
        }
        return null;
    }

    private static void propagateMethodToChildren(ObfContext ctx, String owner, String name, String desc, String mappedName) {
        for (ClassNode cn : ctx.classNodes.values()) {
            if (cn.superName == null) continue;

            if (isSubclassOf(ctx, cn.name, owner)) {
                String key = "%s.%s%s".formatted(cn.name, name, desc);
                String mappedOwner = ctx.classMap.getOrDefault(cn.name, cn.name);
                ctx.methodMap.putIfAbsent(key, mappedName);
                ctx.methodMap.putIfAbsent("%s.%s".formatted(mappedOwner, name + desc), mappedName);
            }
        }
    }

    private static boolean isSubclassOf(ObfContext ctx, String child, String parent) {
        while (true) {
            ClassNode cn = ctx.classNodes.get(child);
            if (cn == null || cn.superName == null) return false;
            if (cn.superName.equals(parent)) return true;
            child = cn.superName;
        }
    }
}