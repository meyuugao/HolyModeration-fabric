package obfuscator.modules;

import static obfuscator.modules.LoggerModule.log;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.security.SecureRandom;
import java.util.ArrayList;

public class StructuralObfuscatorModule {
    private static final SecureRandom secureRandom = new SecureRandom();

    public static void obfuscateClass(ClassNode cn) {
        try {
            for (MethodNode mn : cn.methods) {
                if (mn.name.startsWith("<") || (mn.access & Opcodes.ACC_ABSTRACT) != 0 || (mn.access & Opcodes.ACC_NATIVE) != 0)
                    continue;

                if (mn.instructions == null) mn.instructions = new InsnList();
                if (mn.instructions.size() == 0) mn.instructions.add(new InsnNode(Opcodes.NOP));

                injectFakeLocals(mn);
                injectSingleTryCatch(mn);
            }

            log("StructuralObfuscator обфусцировал класс %s".formatted(cn.name));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void injectSingleTryCatch(MethodNode mn) {
        LabelNode start = new LabelNode();
        LabelNode end = new LabelNode();
        LabelNode handler = new LabelNode();

        mn.instructions.insert(start);
        mn.instructions.add(end);
        mn.instructions.add(handler);
        mn.instructions.add(new TypeInsnNode(Opcodes.NEW, "java/lang/Exception"));
        mn.instructions.add(new InsnNode(Opcodes.DUP));
        mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Exception", "<init>", "()V", false));
        mn.instructions.add(new InsnNode(Opcodes.ATHROW));

        if (mn.tryCatchBlocks == null) mn.tryCatchBlocks = new ArrayList<>();
        mn.tryCatchBlocks.add(new TryCatchBlockNode(start, end, handler, "java/lang/Exception"));
    }

    private static void injectFakeLocals(MethodNode mn) {
        LabelNode start = new LabelNode();
        LabelNode end = new LabelNode();

        mn.instructions.insert(start);
        mn.instructions.add(end);

        for (int i = 0; i < 2; i++) {
            String fakeName = "fakeVar" + Math.abs(secureRandom.nextInt());
            mn.localVariables.add(new LocalVariableNode(fakeName, "Ljava/lang/Object;", null, start, end, mn.maxLocals++));
        }
    }
}