package obfuscator.modules;

import static obfuscator.modules.LoggerModule.log;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;

public class FakeExceptionFlowModule {
    public static void obfuscateClass(ClassNode cn) {
        for (MethodNode mn : cn.methods) {
            if (mn.name.startsWith("<") || mn.instructions == null || mn.instructions.size() < 10) continue;

            LabelNode start = new LabelNode();
            LabelNode end = new LabelNode();
            LabelNode handler = new LabelNode();
            LabelNode cont = new LabelNode();
            int ex = mn.maxLocals++;
            InsnList il = new InsnList();
            il.add(start);
            il.add(new InsnNode(Opcodes.ICONST_0));
            il.add(new JumpInsnNode(Opcodes.IFNE, cont));
            il.add(new TypeInsnNode(Opcodes.NEW, "java/lang/Exception"));
            il.add(new InsnNode(Opcodes.DUP));
            il.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Exception", "<init>", "()V", false));
            il.add(new InsnNode(Opcodes.ATHROW));
            il.add(cont);
            il.add(end);
            mn.instructions.insert(il);
            if (mn.tryCatchBlocks == null) mn.tryCatchBlocks = new ArrayList<>();
            mn.tryCatchBlocks.add(new TryCatchBlockNode(start, end, handler, "java/lang/Exception"));
            InsnList handlerCode = new InsnList();
            handlerCode.add(handler);
            handlerCode.add(new VarInsnNode(Opcodes.ASTORE, ex));
            handlerCode.add(new JumpInsnNode(Opcodes.GOTO, cont));
            mn.instructions.add(handlerCode);
            mn.maxStack += 2;
        }

        log("Модуль FakeExceptionFlow обфусцировал класс %s".formatted(cn.name));
    }
}