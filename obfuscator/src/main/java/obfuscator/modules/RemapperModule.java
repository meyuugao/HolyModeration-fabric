package obfuscator.modules;

import static obfuscator.modules.LoggerModule.log;


import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;

import obfuscator.ObfContext;

public class RemapperModule {
    public static List<String> parseMethodDescriptor(String descriptor) {
        if (descriptor == null || descriptor.length() < 3 || descriptor.charAt(0) != '(') return null;

        List<String> paramTypes = new ArrayList<>();
        int i = 1;
        while (i < descriptor.length() && descriptor.charAt(i) != ')') {
            char c = descriptor.charAt(i);
            if (c == 'L') {
                int end = descriptor.indexOf(';', i);
                if (end == -1) return null;
                paramTypes.add(descriptor.substring(i, end + 1));
                i = end + 1;
            } else if (c == '[') {
                int end = i + 1;
                while (descriptor.charAt(end) == '[') end++;
                if (descriptor.charAt(end) == 'L') {
                    int semi = descriptor.indexOf(';', end);
                    if (semi == -1) return null;
                    paramTypes.add(descriptor.substring(i, semi + 1));
                    i = semi + 1;
                } else {
                    paramTypes.add(descriptor.substring(i, end + 1));
                    i = end + 1;
                }
            } else {
                paramTypes.add(String.valueOf(c));
                i++;
            }
        }
        return paramTypes;
    }

    public static void run(File inputJar) {
        ObfContext ctx = new ObfContext();
        if (!inputJar.exists()) {
            log("Обфускатор не был запущен, т.к. не обнаружен jar файл по указанному пути.");
            return;
        }

        File outputJar = new File(
                inputJar.getParentFile(),
                "%s-obfuscated.jar".formatted(inputJar.getName().replace(".jar", ""))
        );

        try (JarFile originalJar = new JarFile(inputJar)) {
            log("Обфускатор запущен. Полученная jar: %s".formatted(originalJar.getName()));

            AssetsObfuscatorModule.obfuscateAssets(originalJar, ctx);

            ClassScannerModule.scanJar(originalJar, ctx);
            MappingGeneratorModule.generate(ctx);

            for (Map.Entry<String, byte[]> e : ctx.classBytes.entrySet()) {
                ClassReader cr = new ClassReader(e.getValue());
                ClassNode cn = new ClassNode();
                cr.accept(cn, ClassReader.SKIP_FRAMES);
                Set<String> parents = new HashSet<>();
                if (cn.superName != null) parents.add(cn.superName);
                if (cn.interfaces != null) parents.addAll(cn.interfaces);
                ctx.superClasses.put(e.getKey(), parents);
            }

            ObfRemapperModule remapper = new ObfRemapperModule(ctx);

            Map<String, byte[]> transformedBytes = new HashMap<>();
            for (Map.Entry<String, byte[]> entry : ctx.classBytes.entrySet()) {
                byte[] transformed = ClassTransformerModule.transform(entry.getValue(), ctx, remapper);
                transformedBytes.put(entry.getKey(), transformed);
            }

            JarFile jarFile = new JarFile(inputJar);
            JarOutputStream out = new JarOutputStream(new FileOutputStream(outputJar));

            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                InputStream is = jarFile.getInputStream(entry);
                byte[] bytes = is.readAllBytes();
                is.close();

                String entryName = entry.getName();
                String newEntryName = entryName;
                byte[] entryBytes = bytes;

                if (entry.isDirectory()) {
                    ZipEntry newEntry = new ZipEntry(newEntryName);
                    newEntry.setTime(entry.getTime());
                    out.putNextEntry(newEntry);
                    out.closeEntry();
                    continue;
                }

                if (ctx.assetsMap.containsKey(entryName)) {
                    newEntryName = ctx.assetsMap.get(entryName);
                    entryBytes = ctx.assetsBytes.get(newEntryName);

                    String directory = newEntryName.substring(0, newEntryName.lastIndexOf('/') + 1);
                    ZipEntry dirEntry = new ZipEntry(directory);
                    dirEntry.setTime(System.currentTimeMillis());
                    try {
                        out.putNextEntry(dirEntry);
                        out.closeEntry();
                    } catch (Exception ignored) {
                    }

                    ZipEntry newEntry = new ZipEntry(newEntryName);
                    newEntry.setTime(System.currentTimeMillis());
                    out.putNextEntry(newEntry);
                    out.write(entryBytes);
                    out.closeEntry();
                    continue;
                } else if (entryName.endsWith(".class") && entryName.startsWith(ctx.mainPrefix)) {
                    String internalName = entryName.replace(".class", "");

                    if (transformedBytes.containsKey(internalName)) {
                        entryBytes = transformedBytes.get(internalName);

                        String newInternalName;

                        if (internalName.contains("$")) {
                            String outer = internalName.substring(0, internalName.indexOf('$'));
                            String inner = internalName.substring(internalName.indexOf('$'));

                            String mappedOuter = ctx.classMap.getOrDefault(outer, outer);

                            if (ctx.dontObfClasses.contains(internalName)) {
                                newInternalName = "%s%s".formatted(mappedOuter, inner);
                            } else {
                                newInternalName = ctx.classMap.getOrDefault(internalName, "%s%s".formatted(mappedOuter, inner));
                            }
                        } else {
                            newInternalName = ctx.classMap.getOrDefault(internalName, internalName);
                        }

                        newEntryName = "%s.class".formatted(newInternalName);
                    }
                }

                if (entryName.equals("fabric.mod.json")
                        || entryName.equals(ctx.mixinFile)
                        || entryName.equals(ctx.refmapFile)) {
                    entryBytes = JsonHandlerModule.handleJson(entry, entryBytes, ctx);
                }

                if (entryName.startsWith("assets/")) continue;

                ZipEntry newEntry = new ZipEntry(newEntryName);
                newEntry.setTime(entry.getTime());
                out.putNextEntry(newEntry);
                out.write(entryBytes);
                out.closeEntry();
            }

            jarFile.close();
            out.close();

            LoggerModule.writeMappings(
                    inputJar,
                    outputJar,
                    ctx
            );
        } catch (Exception e) {
            log("Исключение при выполнении: %s".formatted(e));
        }
    }
}