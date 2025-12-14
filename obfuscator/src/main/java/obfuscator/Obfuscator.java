package obfuscator;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.awt.Color;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

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
    private final String[] protectedPrefixes = new String[] {"me/yuugao/holymoderation", "me/yuugao/holymoderation/client"};
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
                                System.out.println("КЛАСС: " + internalName + " -> " + newName);
                            } else {
                                System.out.println("МАППИНГ УЖЕ СУЩЕСТВУЕТ: " + internalName + " -> " + classMapping.get(internalName));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log(Color.RED + "Исключение при выполнении: " + e);
        }
    }

    private void log(String message) {
        System.out.println("[OBFUSCATOOR] " + message);
    }
}