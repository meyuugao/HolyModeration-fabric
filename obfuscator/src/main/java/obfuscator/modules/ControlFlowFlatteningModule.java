package obfuscator.modules;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ControlFlowFlatteningModule {
    public static void obfuscateClass(ClassNode cn) {
        for (MethodNode mn : cn.methods) {
            if (mn.name.startsWith("<")) continue;
            if (mn.instructions == null || mn.instructions.size() < 20) continue;
            int s1 = mn.maxLocals++;
            int s2 = mn.maxLocals++;
            InsnList junk = new InsnList();
            LabelNode entry = new LabelNode();
            LabelNode fake = new LabelNode();
            LabelNode exit = new LabelNode();
            junk.add(new InsnNode(Opcodes.ICONST_0));
            junk.add(new VarInsnNode(Opcodes.ISTORE, s1));
            junk.add(new InsnNode(Opcodes.ICONST_1));
            junk.add(new VarInsnNode(Opcodes.ISTORE, s2));
            junk.add(entry);
            junk.add(new VarInsnNode(Opcodes.ILOAD, s1));
            junk.add(new VarInsnNode(Opcodes.ILOAD, s2));
            junk.add(new InsnNode(Opcodes.IXOR));
            junk.add(new JumpInsnNode(Opcodes.IFNE, exit));
            junk.add(new JumpInsnNode(Opcodes.GOTO, fake));
            junk.add(fake);
            junk.add(new InsnNode(Opcodes.ICONST_1));
            junk.add(new VarInsnNode(Opcodes.ISTORE, s1));
            junk.add(new InsnNode(Opcodes.ICONST_1));
            junk.add(new VarInsnNode(Opcodes.ISTORE, s2));
            junk.add(new JumpInsnNode(Opcodes.GOTO, entry));
            junk.add(exit);
            mn.instructions.insert(junk);
            mn.maxStack += 2;
        }
    }
}