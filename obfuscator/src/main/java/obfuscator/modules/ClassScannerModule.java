package obfuscator.modules;

import static obfuscator.modules.LoggerModule.log;


import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import obfuscator.ObfContext;

public final class ClassScannerModule {
    private ClassScannerModule() {
    }

    public static void scanJar(JarFile jarFile, ObfContext ctx) throws Exception {
        Enumeration<JarEntry> entriesEnum = jarFile.entries();
        while (entriesEnum.hasMoreElements()) {
            JarEntry entry = entriesEnum.nextElement();
            if (!entry.getName().endsWith(".class")) continue;
            if (!entry.getName().startsWith(ctx.mainPrefix)) continue;

            InputStream is = jarFile.getInputStream(entry);
            byte[] bytes = is.readAllBytes();
            is.close();
            String internalName = entry.getName().replace(".class", "");
            ctx.classBytes.put(internalName, bytes);
            ClassReader cr = new ClassReader(bytes);
            ClassNode cn = new ClassNode();
            cr.accept(cn, ClassReader.EXPAND_FRAMES);
            ctx.classNodes.put(internalName, cn);

            if (cn.visibleAnnotations != null) {
                for (AnnotationNode an : cn.visibleAnnotations) {
                    if (an.desc != null && an.desc.contains(ctx.dontObfAnnotationClass)) {
                        ctx.dontObfRules.put(internalName, AnnotationUtilModule.readRules(an));
                    }
                }
            }

            if (cn.methods != null) {
                for (MethodNode mn : cn.methods) {
                    if (mn.visibleAnnotations == null) continue;
                    String key = "%s.%s%s".formatted(internalName, mn.name, mn.desc);
                    for (AnnotationNode an : mn.visibleAnnotations) {
                        if (an.desc != null && an.desc.contains(ctx.dontObfAnnotationClass)) {
                            ctx.dontObfRules.put(key, AnnotationUtilModule.readRules(an));
                        }
                    }
                }
            }

            if (cn.fields != null) {
                for (FieldNode fn : cn.fields) {
                    if (fn.visibleAnnotations == null) continue;
                    String key = "%s.%s".formatted(internalName, fn.name);
                    for (AnnotationNode an : fn.visibleAnnotations) {
                        if (an.desc != null && an.desc.contains(ctx.dontObfAnnotationClass)) {
                            ctx.dontObfRules.put(key, AnnotationUtilModule.readRules(an));
                        }
                    }
                }
            }

            log("Скан класса %s".formatted(internalName));
        }
    }
}