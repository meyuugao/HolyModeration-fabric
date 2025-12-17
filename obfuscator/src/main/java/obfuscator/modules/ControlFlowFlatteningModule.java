package obfuscator.modules;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class ControlFlowFlatteningModule {
    public static void obfuscate(MethodNode mn) {
        if (mn.name.startsWith("<")) return;
        if (mn.instructions.size() < 20) return;

        InsnList insns = mn.instructions;
        InsnList junk = new InsnList();

        int s1 = mn.maxLocals++;
        int s2 = mn.maxLocals++;

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

        insns.insert(junk);

        mn.maxStack += 2;
    }
}