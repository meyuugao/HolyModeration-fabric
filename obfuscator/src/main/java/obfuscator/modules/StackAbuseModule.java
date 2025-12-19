package obfuscator.modules;

import static obfuscator.modules.LoggerModule.log;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class StackAbuseModule {
    public static void obfuscateClass(ClassNode cn) {
        for (MethodNode mn : cn.methods) {
            if (mn.name.startsWith("<") || mn.instructions == null || mn.instructions.size() < 5) continue;

            for (AbstractInsnNode insn : mn.instructions.toArray()) {
                int op = insn.getOpcode();
                if (op >= Opcodes.IRETURN && op <= Opcodes.RETURN) {
                    InsnList junk = new InsnList();
                    junk.add(new InsnNode(Opcodes.ACONST_NULL));
                    junk.add(new InsnNode(Opcodes.POP));
                    junk.add(new InsnNode(Opcodes.ICONST_0));
                    junk.add(new InsnNode(Opcodes.ICONST_1));
                    junk.add(new InsnNode(Opcodes.IADD));
                    junk.add(new InsnNode(Opcodes.POP));
                    mn.instructions.insertBefore(insn, junk);
                }
            }
            mn.maxStack += 2;
        }

        log("Модуль StackAbuse обфусцировал класс %s".formatted(cn.name));
    }
}