package obfuscator.modules;

import org.objectweb.asm.tree.*;
import java.util.Iterator;

public class AnnotationCleanupModule {
    public static void clean(ClassNode cn, ObfContext oc) {
        removeFromList(cn.visibleAnnotations, oc);
        removeFromList(cn.invisibleAnnotations, oc);

        for (MethodNode mn : cn.methods) {
            removeFromList(mn.visibleAnnotations, oc);
            removeFromList(mn.invisibleAnnotations, oc);
        }

        for (FieldNode fn : cn.fields) {
            removeFromList(fn.visibleAnnotations, oc);
            removeFromList(fn.invisibleAnnotations, oc);
        }
    }

    private static void removeFromList(java.util.List<AnnotationNode> list, ObfContext oc) {
        if (list == null) return;

        list.removeIf(an -> an.desc.contains(oc.dontObfAnnotationClass));
    }
}
