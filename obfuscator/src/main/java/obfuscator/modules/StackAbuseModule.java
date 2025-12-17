package obfuscator.modules;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class StackAbuseModule {
    public static void obfuscate(MethodNode mn) {
        if (mn.instructions == null || mn.instructions.size() < 5) return;
        if (mn.name.startsWith("<")) return;

        InsnList list = mn.instructions;

        for (AbstractInsnNode insn : list.toArray()) {
            int op = insn.getOpcode();
            if (op >= Opcodes.IRETURN && op <= Opcodes.RETURN) {
                InsnList junk = new InsnList();
                junk.add(new InsnNode(Opcodes.ACONST_NULL));
                junk.add(new InsnNode(Opcodes.POP));
                junk.add(new InsnNode(Opcodes.ICONST_0));
                junk.add(new InsnNode(Opcodes.ICONST_1));
                junk.add(new InsnNode(Opcodes.IADD));
                junk.add(new InsnNode(Opcodes.POP));
                list.insertBefore(insn, junk);
            }
        }

        mn.maxStack += 2;
    }
}