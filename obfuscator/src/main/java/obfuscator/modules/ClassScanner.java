package obfuscator.modules;

import static obfuscator.modules.LoggerModule.log;


import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

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
            boolean hasClassDontObf = cn.visibleAnnotations != null && cn.visibleAnnotations.stream().anyMatch(an -> an.desc.contains(ctx.dontObfAnnotationClass));
            if (hasClassDontObf) ctx.dontObfClasses.add(internalName);
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