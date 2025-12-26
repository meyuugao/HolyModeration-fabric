package obfuscator.modules;

import static obfuscator.modules.LoggerModule.log;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class SwitchBombModule {
    public static void obfuscateClass(ClassNode cn) {
        for (MethodNode mn : cn.methods) {
            if (mn.name.startsWith("<") || mn.instructions == null || mn.instructions.size() < 10) continue;

            int v = mn.maxLocals++;
            LabelNode d = new LabelNode();
            LabelNode l0 = new LabelNode();
            LabelNode l1 = new LabelNode();
            LabelNode l2 = new LabelNode();
            LabelNode l3 = new LabelNode();
            InsnList il = new InsnList();
            il.add(new InsnNode(Opcodes.ICONST_0));
            il.add(new VarInsnNode(Opcodes.ISTORE, v));
            il.add(new VarInsnNode(Opcodes.ILOAD, v));
            il.add(new TableSwitchInsnNode(0, 3, d, l0, l1, l2, l3));
            il.add(l0);
            il.add(new JumpInsnNode(Opcodes.GOTO, d));
            il.add(l1);
            il.add(new JumpInsnNode(Opcodes.GOTO, d));
            il.add(l2);
            il.add(new JumpInsnNode(Opcodes.GOTO, d));
            il.add(l3);
            il.add(new JumpInsnNode(Opcodes.GOTO, d));
            il.add(d);
            mn.instructions.insert(il);
            mn.maxStack += 1;
        }

        log("Модуль SwitchBomb обфусцировал класс %s".formatted(cn.name));
    }
}