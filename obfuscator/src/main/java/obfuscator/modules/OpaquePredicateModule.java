package obfuscator.modules;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class OpaquePredicateModule {
    public static void obfuscateClass(ClassNode cn) {
        for (MethodNode mn : cn.methods) {
            if (mn.name.startsWith("<")) continue;
            if (mn.instructions == null || mn.instructions.size() < 10) continue;
            int v = mn.maxLocals++;
            LabelNode ok = new LabelNode();
            InsnList il = new InsnList();
            il.add(new InsnNode(Opcodes.ICONST_1));
            il.add(new VarInsnNode(Opcodes.ISTORE, v));
            il.add(new VarInsnNode(Opcodes.ILOAD, v));
            il.add(new InsnNode(Opcodes.ICONST_1));
            il.add(new InsnNode(Opcodes.IXOR));
            il.add(new JumpInsnNode(Opcodes.IFEQ, ok));
            il.add(new InsnNode(Opcodes.ACONST_NULL));
            il.add(new InsnNode(Opcodes.ATHROW));
            il.add(ok);
            mn.instructions.insert(il);
            mn.maxStack += 2;
        }
    }
}