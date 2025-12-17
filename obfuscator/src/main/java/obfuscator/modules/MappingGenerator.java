package obfuscator.modules;

import static obfuscator.modules.LoggerModule.log;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;

public final class MappingGenerator {
    private MappingGenerator() {
    }

    public static void generate(ObfContext ctx) {
        for (Map.Entry<String, byte[]> entry : ctx.classBytes.entrySet()) {
            String internalName = entry.getKey();
            if (internalName.equals(ctx.dontObfAnnotationClass)) continue;
            org.objectweb.asm.tree.ClassNode classNode = ctx.classNodes.get(internalName);
            boolean hasClassDontObf = ctx.dontObfClasses.contains(internalName);
            int idx = internalName.lastIndexOf('/');
            String packageName = idx > 0 ? internalName.substring(0, idx) : "";

            List<String> prefixes = new ArrayList<>(Arrays.asList(ctx.protectedPrefixes));
            prefixes.sort((a, b) -> Integer.compare(b.length(), a.length()));

            if (internalName.contains("$")) {
                String outer = internalName.substring(0, internalName.indexOf('$'));
                String inner = internalName.substring(internalName.indexOf('$'));

                String mappedOuter;

                if (ctx.classMap.containsKey(outer)) {
                    mappedOuter = ctx.classMap.get(outer);
                } else {
                    String genOuter = NameGenerator.generateLatName();

                    if (outer.startsWith(ctx.mixinPrefix)) {
                        ctx.classMap.computeIfAbsent(
                                ctx.mixinPrefix,
                                k -> ctx.protectedPrefixes[1] + "/" + NameGenerator.generateLatName()
                        );
                    } else {
                        boolean matched = false;
                        String pkg = outer.contains("/") ? outer.substring(0, outer.lastIndexOf('/')) : "";
                        for (String prefix : prefixes) {
                            if (pkg.startsWith(prefix)) {
                                String[] parts = pkg.substring(prefix.length()).split("/");
                                StringBuilder sb = new StringBuilder(prefix);
                                for (String p : parts)
                                    if (!p.isEmpty()) sb.append("/").append(NameGenerator.generateLatName());
                                matched = true;
                                ctx.classMap.put(outer, sb + "/" + genOuter);
                                break;
                            }
                        }
                        if (!matched) {
                            StringBuilder sb = new StringBuilder();
                            for (String p : pkg.split("/"))
                                if (!p.isEmpty()) sb.append(NameGenerator.generateLatName()).append("/");
                            ctx.classMap.put(outer, sb + genOuter);
                        }
                    }

                    mappedOuter = ctx.classMap.get(outer);
                    log("НОВЫЙ КЛАСС: " + outer + " -> " + mappedOuter);
                }

                String finalName = mappedOuter + inner;
                ctx.classMap.put(internalName, finalName);

                log("НОВЫЙ КЛАСС: " + internalName + " -> " + finalName);
                continue;
            } else {
                if (!ctx.classMap.containsKey(internalName)) {
                    String newName = "";
                    if (packageName.startsWith(ctx.mixinPrefix)) {
                        ctx.classMap.computeIfAbsent(ctx.mixinPrefix, k -> ctx.protectedPrefixes[1] + "/" + NameGenerator.generateLatName());
                        newName = ctx.classMap.get(ctx.mixinPrefix) + "/" + NameGenerator.generateLatName();
                    } else {
                        boolean matched = false;
                        for (String prefix : prefixes) {
                            if (packageName.startsWith(prefix)) {
                                String[] parts = packageName.substring(prefix.length()).split("/");
                                StringBuilder sb = new StringBuilder(prefix);
                                for (String p : parts)
                                    if (!p.isEmpty()) sb.append("/").append(NameGenerator.generateLatName());
                                newName = sb + "/" + NameGenerator.generateLatName();
                                matched = true;
                                break;
                            }
                        }
                        if (!matched) {
                            StringBuilder sb = new StringBuilder();
                            for (String p : packageName.split("/"))
                                if (!p.isEmpty()) sb.append(NameGenerator.generateLatName()).append("/");
                            newName = sb + NameGenerator.generateLatName();
                        }
                    }
                    ctx.classMap.put(internalName, newName);
                    log("НОВЫЙ КЛАСС: " + internalName + " -> " + newName);
                } else {
                    log("МАППИНГ УЖЕ СУЩЕСТВУЕТ: " + internalName + " -> " + ctx.classMap.get(internalName));
                }
            }

            for (FieldNode field : classNode.fields) {
                String fieldKeyFull = internalName + "." + field.name;
                boolean hasFieldDontObf = field.visibleAnnotations != null && field.visibleAnnotations.stream().anyMatch(an -> an.desc.contains(ctx.dontObfAnnotationClass));
                if (hasFieldDontObf) ctx.dontObfFields.add(fieldKeyFull);
                if (!hasFieldDontObf && !hasClassDontObf) {
                    if (!ctx.fieldMap.containsKey(fieldKeyFull)) {
                        String genField = NameGenerator.generateChineseName();
                        ctx.fieldMap.put(fieldKeyFull, genField);
                        String mappedOwner = ctx.classMap.getOrDefault(internalName, internalName);
                        String fieldKeyMapped = mappedOwner + "." + field.name;
                        ctx.fieldMap.put(fieldKeyMapped, genField);
                        log("НОВОЕ ПОЛЕ: " + field.name + " -> " + genField);
                    }
                }
            }

            for (MethodNode method : classNode.methods) {
                String methodKey = internalName + "." + method.name + method.desc;
                boolean hasMethodDontObf = method.visibleAnnotations != null && method.visibleAnnotations.stream().anyMatch(an -> an.desc.contains(ctx.dontObfAnnotationClass));
                if (hasMethodDontObf) ctx.dontObfMethods.add(methodKey);
                if (!hasMethodDontObf && !Objects.equals(method.name, "<init>") && !Objects.equals(method.name, "<clinit>") && !hasClassDontObf) {
                    String genKey = internalName + "." + method.name + method.desc;
                    if (!ctx.methodMap.containsKey(genKey)) {
                        String genName = NameGenerator.generateChineseName();
                        ctx.methodMap.put(genKey, genName);
                        String mappedOwner = ctx.classMap.getOrDefault(internalName, internalName);
                        String genKeyMapped = mappedOwner + "." + method.name + method.desc;
                        ctx.methodMap.put(genKeyMapped, genName);
                        log("НОВЫЙ МЕТОД: " + genKey + " -> " + ctx.methodMap.get(genKey));
                    }

                    List<String> paramTypes = RemapperModule.parseMethodDescriptor(method.desc);
                    if (paramTypes != null && !paramTypes.isEmpty() && method.localVariables != null) {
                        int paramIndex = ((method.access & Opcodes.ACC_STATIC) == 0) ? 1 : 0;
                        for (LocalVariableNode localVar : method.localVariables) {
                            int idx1 = localVar.index;
                            if (idx1 >= paramIndex && idx1 < paramIndex + paramTypes.size()) {
                                String obfClassName = ctx.classMap.getOrDefault(internalName, internalName);
                                String obfMethodName = ctx.methodMap.containsKey(methodKey) ? ctx.methodMap.get(methodKey) : method.name;
                                String paramKey = obfClassName + "." + obfMethodName + "." + (idx1 - paramIndex);
                                String originalParamKey = internalName + "." + obfMethodName + "." + (idx1 - paramIndex);
                                if (!ctx.paramMap.containsKey(paramKey)) {
                                    String genParam = NameGenerator.generateChineseName();
                                    ctx.paramMap.put(paramKey, genParam);
                                    ctx.paramMap.put(originalParamKey, genParam);
                                    log("НОВЫЙ АРГУМЕНТ: " + obfMethodName + " " + localVar.name + " -> " + genParam);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}