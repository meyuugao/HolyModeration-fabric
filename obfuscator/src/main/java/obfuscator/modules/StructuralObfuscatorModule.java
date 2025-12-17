package obfuscator.modules;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.security.SecureRandom;
import java.util.HashMap;

public class StructuralObfuscatorModule {
    private static final SecureRandom secureRandom = new SecureRandom();

    public static void obfuscateClass(ClassNode cn) {
        for (MethodNode mn : cn.methods) {
            if (mn.name.startsWith("<")) continue;
            if ((mn.access & Opcodes.ACC_ABSTRACT) != 0) continue;
            if ((mn.access & Opcodes.ACC_NATIVE) != 0) continue;

            injectFakeLocals(mn);
            injectTryCatchBlocks(mn);
        }
        duplicateMethods(cn);
    }

    private static void injectTryCatchBlocks(MethodNode method) {
        if (method.instructions == null) method.instructions = new InsnList();
        if (method.instructions.size() == 0) method.instructions.add(new InsnNode(Opcodes.NOP));

        LabelNode start = new LabelNode();
        LabelNode end = new LabelNode();
        LabelNode handler = new LabelNode();

        method.instructions.insert(start);
        method.instructions.add(end);
        method.instructions.add(handler);
        method.instructions.add(new TypeInsnNode(Opcodes.NEW, "java/lang/Exception"));
        method.instructions.add(new InsnNode(Opcodes.DUP));
        method.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Exception", "<init>", "()V", false));
        method.instructions.add(new InsnNode(Opcodes.ATHROW));

        if (method.tryCatchBlocks == null) method.tryCatchBlocks = new java.util.ArrayList<>();
        method.tryCatchBlocks.add(new TryCatchBlockNode(start, end, handler, "java/lang/Exception"));
    }

    private static void injectFakeLocals(MethodNode method) {
        if (method.localVariables == null) method.localVariables = new java.util.ArrayList<>();
        if (method.instructions == null) method.instructions = new InsnList();
        if (method.instructions.size() == 0) method.instructions.add(new InsnNode(Opcodes.NOP));

        LabelNode start = new LabelNode();
        LabelNode end = new LabelNode();

        method.instructions.insert(start);
        method.instructions.add(end);

        for (int i = 0; i < 2; i++) {
            String fakeName = "fakeVar" + secureRandom.nextInt(1000);
            method.localVariables.add(new LocalVariableNode(fakeName, "Ljava/lang/Object;", null, start, end, method.maxLocals++));
        }
    }

    private static void duplicateMethods(ClassNode cn) {
        for (MethodNode method : cn.methods.toArray(new MethodNode[0])) {
            if (method.name.startsWith("<")) continue;

            MethodNode dup = new MethodNode(
                    Opcodes.ASM9,
                    method.access,
                    NameGenerator.generateChineseName(),
                    method.desc,
                    method.signature,
                    method.exceptions.toArray(new String[0])
            );

            InsnList copyInstructions = new InsnList();
            HashMap<LabelNode, LabelNode> labelMap = new HashMap<>();

            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (insn instanceof LabelNode) labelMap.put((LabelNode) insn, new LabelNode());
            }

            for (AbstractInsnNode insn : method.instructions.toArray()) {
                if (insn != null) copyInstructions.add(insn.clone(labelMap));
            }

            dup.instructions = copyInstructions;

            if (method.localVariables != null) {
                dup.localVariables = new java.util.ArrayList<>();
                for (LocalVariableNode var : method.localVariables) {
                    LabelNode s = labelMap.getOrDefault(var.start, var.start);
                    LabelNode e = labelMap.getOrDefault(var.end, var.end);
                    dup.localVariables.add(new LocalVariableNode(var.name, var.desc, var.signature, s, e, var.index));
                }
            }

            if (method.tryCatchBlocks != null) {
                dup.tryCatchBlocks = new java.util.ArrayList<>();
                for (TryCatchBlockNode tcb : method.tryCatchBlocks) {
                    LabelNode s = labelMap.getOrDefault(tcb.start, tcb.start);
                    LabelNode e = labelMap.getOrDefault(tcb.end, tcb.end);
                    LabelNode h = labelMap.getOrDefault(tcb.handler, tcb.handler);
                    dup.tryCatchBlocks.add(new TryCatchBlockNode(s, e, h, tcb.type));
                }
            }

            cn.methods.add(dup);
        }
    }
}