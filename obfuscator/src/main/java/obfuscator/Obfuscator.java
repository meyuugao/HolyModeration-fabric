package obfuscator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

import java.awt.Color;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;

import obfuscator.modules.GarbageInjector;
import obfuscator.modules.NameGenerator;

public class Obfuscator {
    private File inputJar, outputJar;

    private final Map<String, String> classMapping = new HashMap<>();
    private final Map<String, String> methodMapping = new HashMap<>();
    private final Map<String, String> fieldMapping = new HashMap<>();
    private final Map<String, String> paramMapping = new HashMap<>();
    private final Map<String, Set<String>> superClasses = new HashMap<>();
    private final Set<String> dontObfClasses = new HashSet<>();
    private final Set<String> dontObfMethods = new HashSet<>();
    private final Set<String> dontObfFields = new HashSet<>();
    private final Map<String, byte[]> classData = new HashMap<>();

    private final String mainPrefix = "me/yuugao/holymoderation/";
    private final String dontObfAnnotationClass = "me/yuugao/holymoderation/obfuscation/DontObf";
    private final String[] protectedPrefixes = new String[]{"me/yuugao/holymoderation", "me/yuugao/holymoderation/client"};
    private final String mixinPrefix = "me/yuugao/holymoderation/client/mixin";

    private Obfuscator() {
    }

    public static void main(String[] args) {
        Obfuscator obfuscator = new Obfuscator();
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        obfuscator.run(args[0]);
    }

    public void run(String path) {
        inputJar = new File(path);
        if (!inputJar.exists()) {
            log("Обфускатор не был запущен. Не обнаружен jar файл по указанному пути.");
            return;
        }
        outputJar = new File(inputJar.getParentFile(), inputJar.getName().replace(".jar", "") + "-obfuscated.jar");

        try (JarFile originalJar = new JarFile(inputJar.getAbsolutePath())) {
            log("Обфускатор запущен. Полученная jar: " + originalJar.getName());

            Enumeration<JarEntry> entriesEnum = originalJar.entries();
            while (entriesEnum.hasMoreElements()) {
                JarEntry entry = entriesEnum.nextElement();
                if (entry.getName().endsWith(".class") && entry.getName().startsWith(mainPrefix)) {
                    InputStream is = originalJar.getInputStream(entry);
                    byte[] bytes = is.readAllBytes();
                    is.close();

                    String internalName = entry.getName().replace(".class", "");
                    classData.put(internalName, bytes);

                    ClassReader classReader = new ClassReader(bytes);
                    ClassNode classNode = new ClassNode();
                    classReader.accept(classNode, ClassReader.EXPAND_FRAMES);

                    boolean hasClassDontObf = classNode.visibleAnnotations != null && classNode.visibleAnnotations.stream().anyMatch(an -> an.desc.contains(dontObfAnnotationClass));
                    if (hasClassDontObf) {
                        dontObfClasses.add(internalName);
                    }

                    int idx = internalName.lastIndexOf('/');
                    String packageName = internalName.substring(0, idx);

                    if (!internalName.equals(dontObfAnnotationClass)) {
                        if (internalName.contains("$")) {
                            String outer = internalName.substring(0, internalName.indexOf('$'));
                            String inner = internalName.substring(internalName.indexOf('$'));

                            if (classMapping.containsKey(outer)) {
                                classMapping.put(internalName, classMapping.get(outer) + inner);
                            } else {
                                String genOuter = NameGenerator.generateLatName();
                                String newOuter = "";

                                if (outer.startsWith(mixinPrefix)) {
                                    classMapping.computeIfAbsent(
                                            mixinPrefix,
                                            k -> protectedPrefixes[1] + "/" + NameGenerator.generateLatName()
                                    );
                                    newOuter = classMapping.get(mixinPrefix) + "/" + genOuter;
                                } else {
                                    boolean matched = false;
                                    for (String prefix : protectedPrefixes) {
                                        if (outer.startsWith(prefix)) {
                                            String[] parts = outer.substring(prefix.length()).split("/");
                                            StringBuilder sb = new StringBuilder(prefix);
                                            for (String p : parts)
                                                if (!p.isEmpty())
                                                    sb.append("/").append(NameGenerator.generateLatName());
                                            newOuter = sb + "/" + genOuter;
                                            matched = true;
                                            break;
                                        }
                                    }
                                    if (!matched) {
                                        String pkg = outer.substring(0, outer.lastIndexOf('/'));
                                        StringBuilder sb = new StringBuilder();
                                        for (String p : pkg.split("/"))
                                            if (!p.isEmpty())
                                                sb.append(NameGenerator.generateLatName()).append("/");
                                        newOuter = sb + genOuter;
                                    }
                                }

                                classMapping.put(outer, newOuter);
                                classMapping.put(internalName, newOuter + inner);
                            }

                        } else {
                            if (!classMapping.containsKey(internalName)) {
                                String newName = "";

                                if (packageName.startsWith(mixinPrefix)) {
                                    classMapping.computeIfAbsent(
                                            mixinPrefix,
                                            k -> protectedPrefixes[1] + "/" + NameGenerator.generateLatName()
                                    );
                                    newName = classMapping.get(mixinPrefix) + "/" + NameGenerator.generateLatName();
                                } else {
                                    boolean matched = false;
                                    for (String prefix : protectedPrefixes) {
                                        if (packageName.startsWith(prefix)) {
                                            String[] parts = packageName.substring(prefix.length()).split("/");
                                            StringBuilder sb = new StringBuilder(prefix);
                                            for (String p : parts)
                                                if (!p.isEmpty())
                                                    sb.append("/").append(NameGenerator.generateLatName());
                                            newName = sb + "/" + NameGenerator.generateLatName();
                                            matched = true;
                                            break;
                                        }
                                    }
                                    if (!matched) {
                                        StringBuilder sb = new StringBuilder();
                                        for (String p : packageName.split("/"))
                                            if (!p.isEmpty())
                                                sb.append(NameGenerator.generateLatName()).append("/");
                                        newName = sb + NameGenerator.generateLatName();
                                    }
                                }

                                classMapping.put(internalName, newName);
                                log("НОВЫЙ КЛАСС: " + internalName + " -> " + newName);
                            } else {
                                log("МАППИНГ УЖЕ СУЩЕСТВУЕТ: " + internalName + " -> " + classMapping.get(internalName));
                            }
                        }
                    }

                    for (FieldNode field : classNode.fields) {
                        String fieldKeyFull = internalName + "." + field.name;
                        boolean hasFieldDontObf = field.visibleAnnotations != null && field.visibleAnnotations.stream().anyMatch(an -> an.desc.contains(dontObfAnnotationClass));
                        if (hasFieldDontObf) {
                            dontObfFields.add(fieldKeyFull);
                        }
                        if (!hasFieldDontObf && !hasClassDontObf) {
                            if (!fieldMapping.containsKey(fieldKeyFull)) {
                                String genField = NameGenerator.generateChineseName();
                                fieldMapping.put(fieldKeyFull, genField);
                                String mappedOwner = classMapping.getOrDefault(internalName, internalName);
                                String fieldKeyMapped = mappedOwner + "." + field.name;
                                fieldMapping.put(fieldKeyMapped, genField);
                                log("НОВОЕ ПОЛЕ: " + field.name + " -> " + genField);
                            }
                        }
                    }

                    for (MethodNode method : classNode.methods) {
                        String methodKey = internalName + "." + method.name + method.desc;
                        boolean hasMethodDontObf = method.visibleAnnotations != null && method.visibleAnnotations.stream().anyMatch(an -> an.desc.contains(dontObfAnnotationClass));
                        if (hasMethodDontObf) {
                            dontObfMethods.add(methodKey);
                        }
                        if (!hasMethodDontObf && !Objects.equals(method.name, "<init>") && !Objects.equals(method.name, "<clinit>") && !hasClassDontObf) {
                            String genKey = internalName + "." + method.name + method.desc;
                            if (!methodMapping.containsKey(genKey)) {
                                String genName = NameGenerator.generateChineseName();
                                methodMapping.put(genKey, genName);
                                String mappedOwner = classMapping.getOrDefault(internalName, internalName);
                                String genKeyMapped = mappedOwner + "." + method.name + method.desc;
                                methodMapping.put(genKeyMapped, genName);
                                log("НОВЫЙ МЕТОД: " + genKey + " -> " + methodMapping.get(genKey));
                            }

                            List<String> paramTypes = parseMethodDescriptor(method.desc);
                            if (paramTypes != null && !paramTypes.isEmpty() && method.localVariables != null) {
                                int paramIndex = ((method.access & Opcodes.ACC_STATIC) == 0) ? 1 : 0;
                                for (LocalVariableNode localVar : method.localVariables) {
                                    int idx1 = localVar.index;
                                    if (idx1 >= paramIndex && idx1 < paramIndex + paramTypes.size()) {
                                        String obfClassName = classMapping.getOrDefault(internalName, internalName);
                                        String obfMethodName = methodMapping.containsKey(methodKey) ? methodMapping.get(methodKey) : method.name;
                                        String paramKey = obfClassName + "." + obfMethodName + "." + (idx1 - paramIndex);
                                        String originalParamKey = internalName + "." + obfMethodName + "." + (idx1 - paramIndex);
                                        if (!paramMapping.containsKey(paramKey)) {
                                            String genParam = NameGenerator.generateChineseName();
                                            paramMapping.put(paramKey, genParam);
                                            paramMapping.put(originalParamKey, genParam);
                                            log("НОВЫЙ АРГУМЕНТ: " + obfMethodName + " " + localVar.name + " -> " + genParam);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            for (Map.Entry<String, byte[]> entryPair : classData.entrySet()) {
                String internalName = entryPair.getKey();
                ClassReader reader = new ClassReader(entryPair.getValue());
                ClassNode node = new ClassNode();
                reader.accept(node, ClassReader.SKIP_FRAMES);
                Set<String> parents = new HashSet<>();
                if (node.superName != null) parents.add(node.superName);
                if (node.interfaces != null) parents.addAll(node.interfaces);
                superClasses.put(internalName, parents);
            }

            log("Сгенерировано маппингов классов: " + classMapping.size());
            log("Сгенерировано маппингов методов: " + methodMapping.size());
            log("Сгенерировано маппингов полей: " + fieldMapping.size());
            log("Сгенерировано маппингов параметров: " + paramMapping.size());
            log("Классов С @DontObf: " + dontObfClasses.size());
            log("Методов С @DontObf: " + dontObfMethods.size());
            log("Полей С @DontObf: " + dontObfFields.size());

            Map<String, Map<String, Map<Integer, String>>> localVarMapping = new HashMap<>();

            for (Map.Entry<String, byte[]> entryPair : classData.entrySet()) {
                String internalName = entryPair.getKey();
                byte[] bytes = entryPair.getValue();
                ClassReader reader = new ClassReader(bytes);
                ClassNode node = new ClassNode();
                reader.accept(node, ClassReader.EXPAND_FRAMES);
                localVarMapping.put(internalName, new HashMap<>());
                for (MethodNode method : node.methods) {
                    localVarMapping.get(internalName).put(method.name, new HashMap<>());
                    List<String> paramTypes = parseMethodDescriptor(method.desc);
                    if (paramTypes == null || method.localVariables == null) continue;
                    int paramIndex = ((method.access & Opcodes.ACC_STATIC) == 0) ? 1 : 0;
                    for (int i = 0; i < paramTypes.size(); i++) {
                        int lvIndex = paramIndex + i;
                        if (lvIndex < method.localVariables.size()) {
                            String paramKey = internalName + "." + method.name + "." + i;
                            if (paramMapping.containsKey(paramKey)) {
                                localVarMapping.get(internalName).get(method.name).put(lvIndex, paramMapping.get(paramKey));
                            }
                        }
                    }
                    if (method.localVariables != null) {
                        for (LocalVariableNode localVar : method.localVariables) {
                            if (localVar.name != null && !localVar.name.equals("this")) {
                                String oldName = localVar.name;
                                localVar.name = NameGenerator.generateChineseName();
                                log("ЛОКАЛЬНАЯ ПЕРЕМЕННАЯ: " + oldName + " -> " + localVar.name);
                            }
                        }
                    }
                }
            }

            Remapper remapper = new Remapper() {
                @Override
                public String map(String internalName) {
                    if (!internalName.startsWith(mainPrefix)) return internalName;
                    if (classMapping.containsKey(internalName)) return classMapping.get(internalName);
                    return internalName;
                }

                @Override
                public String mapMethodName(String owner, String name, String descriptor) {
                    if (!owner.startsWith(mainPrefix)) return name;
                    Set<String> ownersToCheck = new HashSet<>();
                    ownersToCheck.add(owner);
                    if (superClasses.containsKey(owner)) ownersToCheck.addAll(superClasses.get(owner));
                    for (String o : ownersToCheck) {
                        String key = o + "." + name + descriptor;
                        if (dontObfClasses.contains(o)) return name;
                        if (dontObfMethods.contains(key)) return name;
                        if (methodMapping.containsKey(key)) return methodMapping.get(key);
                    }
                    return name;
                }

                @Override
                public String mapFieldName(String owner, String name, String descriptor) {
                    if (!owner.startsWith(mainPrefix)) return name;
                    Set<String> ownersToCheck = new HashSet<>();
                    ownersToCheck.add(owner);
                    if (superClasses.containsKey(owner)) ownersToCheck.addAll(superClasses.get(owner));
                    for (String o : ownersToCheck) {
                        String key = o + "." + name;
                        if (dontObfClasses.contains(o)) return name;
                        if (dontObfFields.contains(key)) return name;
                        if (fieldMapping.containsKey(key)) return fieldMapping.get(key);
                    }
                    return name;
                }
            };

            JarFile jarFile = new JarFile(inputJar.getAbsolutePath());
            JarOutputStream tempJar = new JarOutputStream(new FileOutputStream(outputJar.getAbsolutePath()));

            Enumeration<JarEntry> jarEntries = jarFile.entries();
            while (jarEntries.hasMoreElements()) {
                JarEntry entry = (JarEntry) jarEntries.nextElement();
                InputStream is = jarFile.getInputStream(entry);
                byte[] bytes = is.readAllBytes();
                is.close();

                String newEntryName = entry.getName();
                byte[] entryBytes = bytes;

                if (entry.getName().endsWith(".class") && entry.getName().startsWith(mainPrefix)) {
                    String internalName = entry.getName().replace(".class", "");
                    try {
                        ClassReader reader = new ClassReader(bytes);
                        ClassNode classNode = new ClassNode();
                        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

                        for (MethodNode method : classNode.methods) {
                            if (dontObfMethods.contains(internalName + "." + method.name + method.desc) || dontObfClasses.contains(internalName))
                                continue;

                            if (method.localVariables != null && !method.localVariables.isEmpty()) {
                                int paramIndex = ((method.access & Opcodes.ACC_STATIC) == 0) ? 1 : 0;
                                for (int i = paramIndex; i < method.localVariables.size(); i++) {
                                    LocalVariableNode lv = method.localVariables.get(i);
                                    if (!Objects.equals(lv.name, "this")) {
                                        lv.name = NameGenerator.generateChineseName();
                                    }
                                }
                            }
                        }

                        for (MethodNode methodNode : classNode.methods) {
                            GarbageInjector.injectGarbage(methodNode);
                            GarbageInjector.insertArtLines(classNode, methodNode);
                        }
                        GarbageInjector.ensureClinitWithGarbage(classNode);

                        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
                        ClassRemapper classRemapper = new ClassRemapper(writer, remapper);
                        for (MethodNode m : classNode.methods) {
                            m.parameters = null;
                            m.signature = null;
                            m.access = m.access & ~Opcodes.ACC_SYNTHETIC;
                            m.access = m.access & ~Opcodes.ACC_MANDATED;
                        }
                        classNode.accept(classRemapper);

                        entryBytes = writer.toByteArray();
                        String newInternalName;
                        if (classMapping.containsKey(internalName)) {
                            newInternalName = classMapping.get(internalName);
                        } else if (internalName.contains("$")) {
                            String outerClass = internalName.substring(0, internalName.indexOf('$'));
                            String innerPart = internalName.substring(internalName.indexOf('$'));
                            if (classMapping.containsKey(outerClass)) {
                                newInternalName = classMapping.get(outerClass) + innerPart;
                            } else {
                                newInternalName = internalName;
                            }
                        } else {
                            newInternalName = classMapping.getOrDefault(internalName, internalName);
                        }
                        newEntryName = newInternalName + ".class";
                        log("ОБФУСКАЦИЯ: " + entry.getName() + " -> " + newEntryName);
                    } catch (Exception e) {
                        log("ОШИБКА при обработке " + entry.getName() + ": " + e.getMessage());
                    }
                }

                if (entry.getName().equals("fabric.mod.json") || entry.getName().equals("holymoderation.mixins.json") || entry.getName().equals("HolyModeration-refmap.json")) {
                    try {
                        String updatedContent = new String(entryBytes, StandardCharsets.UTF_8);

                        String mixinPrefix = "me/yuugao/holymoderation/client/mixin";

                        List<Map.Entry<String, String>> mappingEntries = new ArrayList<>(classMapping.entrySet());
                        mappingEntries.sort((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()));

                        String newMixinDirSimple = null;
                        if (classMapping.containsKey(mixinPrefix)) {
                            String mappedMixinDir = classMapping.get(mixinPrefix);
                            if (mappedMixinDir != null && mappedMixinDir.contains("/")) {
                                newMixinDirSimple = mappedMixinDir.substring(mappedMixinDir.lastIndexOf('/') + 1);
                            } else {
                                newMixinDirSimple = mappedMixinDir;
                            }
                        }

                        for (Map.Entry<String, String> mapEntry : mappingEntries) {
                            String oldClass = mapEntry.getKey();
                            String newClass = mapEntry.getValue();

                            if (entry.getName().equals("HolyModeration-refmap.json")) {
                                updatedContent = updatedContent.replaceAll(Pattern.quote(oldClass), newClass);
                            } else {
                                String oldClassName = oldClass.replace("/", ".");
                                String newClassName = newClass.replace("/", ".");
                                updatedContent = updatedContent.replaceAll("\\b" + Pattern.quote(oldClassName) + "\\b", newClassName);
                            }

                            if (entry.getName().equals("holymoderation.mixins.json") && oldClass.startsWith(mixinPrefix + "/")) {
                                String oldSimple = oldClass.substring(oldClass.lastIndexOf('/') + 1);
                                String newSimple = newClass.substring(newClass.lastIndexOf('/') + 1);
                                updatedContent = updatedContent.replace("\"" + oldSimple + "\"", "\"" + newSimple + "\"");
                            }
                        }

                        if (entry.getName().equals("holymoderation.mixins.json") && newMixinDirSimple != null) {
                            updatedContent = updatedContent.replace("\"me.yuugao.holymoderation.client.mixin\"", "\"me.yuugao.holymoderation.client." + newMixinDirSimple + "\"");
                        }

                        entryBytes = updatedContent.getBytes(StandardCharsets.UTF_8);
                        log("ОБНОВЛЕН JSON: " + entry.getName());
                    } catch (Exception e) {
                        log("ОШИБКА при обновлении " + entry.getName() + ": " + e.getMessage());
                    }
                }


                ZipEntry newEntry = new ZipEntry(newEntryName);
                newEntry.setTime(entry.getTime());
                tempJar.putNextEntry(newEntry);
                tempJar.write(entryBytes);
                tempJar.closeEntry();
            }

            jarFile.close();
            tempJar.close();

            File mappingFile = new File(inputJar.getParentFile(), "obfuscation-mapping.txt");

            PrintWriter writer = new PrintWriter(new FileWriter(mappingFile));
            writer.println("=== МАППИНГ КЛАССОВ ===");
            for (Map.Entry<String, String> e : classMapping.entrySet()) {
                writer.println(e.getKey() + " -> " + e.getValue());
            }

            writer.println("\n=== МАППИНГ МЕТОДОВ ===");
            for (Map.Entry<String, String> e : methodMapping.entrySet()) {
                writer.println(e.getKey() + " -> " + e.getValue());
            }

            writer.println("\n=== МАППИНГ ПОЛЕЙ ===");
            for (Map.Entry<String, String> e : fieldMapping.entrySet()) {
                writer.println(e.getKey() + " -> " + e.getValue());
            }

            writer.println("\n=== МАППИНГ ПАРАМЕТРОВ ===");
            for (Map.Entry<String, String> e : paramMapping.entrySet()) {
                writer.println(e.getKey() + " -> " + e.getValue());
            }

            writer.println("\n=== ИСКЛЮЧЕНИЯ @dontobf ===");
            writer.println("Классы: " + String.join(", ", dontObfClasses));
            writer.println("Методы: " + String.join(", ", dontObfMethods));
            writer.println("Поля: " + String.join(", ", dontObfFields));

            System.out.println("=== ОБФУСКАЦИЯ ЗАВЕРШЕНА ===");
            System.out.println("Обфусцированный JAR: " + outputJar.getName());
            System.out.println("Маппинг сохранен в: " + mappingFile.getName());
            System.out.println("Обработано классов: " + classMapping.size());
            System.out.println("Обработано методов: " + methodMapping.size());
            System.out.println("Обработано полей: " + fieldMapping.size());
        } catch (Exception e) {
            log(Color.RED + "Исключение при выполнении: " + e);
        }
    }

    List<String> parseMethodDescriptor(String descriptor) {
        if (descriptor == null || descriptor.length() < 3 || !String.valueOf(descriptor.charAt(0)).equals("(")) {
            return null;
        }
        List<String> paramTypes = new ArrayList<>();
        int i = 1;
        while (i < descriptor.length() && !String.valueOf(descriptor.charAt(i)).equals(")")) {
            String type = String.valueOf(descriptor.charAt(i));
            if (type.equals("L")) {
                int end = descriptor.indexOf(";", i);
                if (end == -1) return null;
                paramTypes.add(descriptor.substring(i, end + 1));
                i = end + 1;
            } else if (type.equals("[")) {
                int end = i + 1;
                while (end < descriptor.length() && String.valueOf(descriptor.charAt(end)).equals("[")) end++;
                if (end >= descriptor.length()) return null;
                if (String.valueOf(descriptor.charAt(end)).equals("L")) {
                    int semiEnd = descriptor.indexOf(";", end);
                    if (semiEnd == -1) return null;
                    paramTypes.add(descriptor.substring(i, semiEnd + 1));
                    i = semiEnd + 1;
                } else {
                    paramTypes.add(descriptor.substring(i, end + 1));
                    i = end + 1;
                }
            } else {
                paramTypes.add(type);
                i++;
            }
        }
        return paramTypes;
    }

    private void log(String message) {
        System.out.println("[OBFUSCATOOR] " + message);
    }
}