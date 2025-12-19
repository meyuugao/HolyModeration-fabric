package obfuscator.modules;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

import obfuscator.ObfContext;

public class AnnotationCleanupModule {
    public static void clean(ClassNode cn, ObfContext ctx) {
        removeFromList(cn.visibleAnnotations, ctx);
        removeFromList(cn.invisibleAnnotations, ctx);

        for (MethodNode mn : cn.methods) {
            removeFromList(mn.visibleAnnotations, ctx);
            removeFromList(mn.invisibleAnnotations, ctx);
        }

        for (FieldNode fn : cn.fields) {
            removeFromList(fn.visibleAnnotations, ctx);
            removeFromList(fn.invisibleAnnotations, ctx);
        }
    }

    private static void removeFromList(List<AnnotationNode> anList, ObfContext ctx) {
        if (anList == null) return;

        anList.removeIf(an -> an.desc.contains(ctx.dontObfAnnotationClass));
    }
}
