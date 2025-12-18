package obfuscator.modules;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import java.util.ArrayList;

public class ExceptionStateLoopModule {
    public static void obfuscateClass(ClassNode cn) {
        for (MethodNode mn : cn.methods) {
            if (mn.instructions == null || mn.instructions.size() < 15) continue;
            if (mn.name.startsWith("<")) continue;
            LabelNode start = new LabelNode();
            LabelNode body = new LabelNode();
            LabelNode end = new LabelNode();
            LabelNode h1 = new LabelNode();
            LabelNode h2 = new LabelNode();
            int ex1 = mn.maxLocals++;
            int ex2 = mn.maxLocals++;
            InsnList il = new InsnList();
            il.add(start);
            il.add(new InsnNode(Opcodes.ICONST_1));
            il.add(new JumpInsnNode(Opcodes.IFEQ, body));
            il.add(new TypeInsnNode(Opcodes.NEW, "java/lang/RuntimeException"));
            il.add(new InsnNode(Opcodes.DUP));
            il.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "()V", false));
            il.add(new InsnNode(Opcodes.ATHROW));
            il.add(body);
            il.add(new InsnNode(Opcodes.NOP));
            il.add(end);
            mn.instructions.insert(il);
            if (mn.tryCatchBlocks == null) mn.tryCatchBlocks = new ArrayList<>();
            mn.tryCatchBlocks.add(new TryCatchBlockNode(start, end, h1, "java/lang/RuntimeException"));
            mn.tryCatchBlocks.add(new TryCatchBlockNode(start, end, h2, "java/lang/Throwable"));
            InsnList h1c = new InsnList();
            h1c.add(h1);
            h1c.add(new VarInsnNode(Opcodes.ASTORE, ex1));
            h1c.add(new JumpInsnNode(Opcodes.GOTO, body));
            InsnList h2c = new InsnList();
            h2c.add(h2);
            h2c.add(new VarInsnNode(Opcodes.ASTORE, ex2));
            h2c.add(new JumpInsnNode(Opcodes.GOTO, start));
            mn.instructions.add(h1c);
            mn.instructions.add(h2c);
            mn.maxStack += 2;
        }
    }
}