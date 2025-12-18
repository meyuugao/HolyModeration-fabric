package obfuscator.modules;

import static obfuscator.modules.LoggerModule.log;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class ClassScanner {
    private ClassScanner() {
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
                for (AnnotationNode o : cn.visibleAnnotations) {
                    if (o.desc != null && o.desc.contains(ctx.dontObfAnnotationClass)) {
                        ctx.dontObfRules.put(internalName, AnnotationUtil.readRules(o));
                    }
                }
            }

            if (cn.methods != null) {
                for (MethodNode mn : cn.methods) {
                    if (mn.visibleAnnotations == null) continue;
                    String key = internalName + "." + mn.name + mn.desc;
                    for (AnnotationNode o : mn.visibleAnnotations) {
                        if (o.desc != null && o.desc.contains(ctx.dontObfAnnotationClass)) {
                            ctx.dontObfRules.put(key, AnnotationUtil.readRules(o));
                        }
                    }
                }
            }

            if (cn.fields != null) {
                for (FieldNode fn : cn.fields) {
                    if (fn.visibleAnnotations == null) continue;
                    String key = internalName + "." + fn.name;
                    for (AnnotationNode o : fn.visibleAnnotations) {
                        if (o.desc != null && o.desc.contains(ctx.dontObfAnnotationClass)) {
                            ctx.dontObfRules.put(key, AnnotationUtil.readRules(o));
                        }
                    }
                }
            }

            int idx = internalName.lastIndexOf('/');
            if (idx > 0) {
                String packageName = internalName.substring(0, idx);
                log("СКАН КЛАССА: " + internalName + " в пакете " + packageName);
            } else {
                log("СКАН КЛАССА: " + internalName);
            }
        }
    }
}