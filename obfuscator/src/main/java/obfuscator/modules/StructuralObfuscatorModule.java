package obfuscator.modules;

import static obfuscator.modules.LoggerModule.log;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

public class StructuralObfuscatorModule {
    private static final SecureRandom secureRandom = new SecureRandom();

    public static void obfuscateClass(ClassNode cn) {
        try {
            for (MethodNode mn : cn.methods) {
                if (mn.name.startsWith("<") || (mn.access & Opcodes.ACC_ABSTRACT) != 0 || (mn.access & Opcodes.ACC_NATIVE) != 0)
                    continue;

                if (mn.instructions == null) {
                    mn.instructions = new InsnList();
                }
                if (mn.instructions.size() == 0) {
                    mn.instructions.add(new InsnNode(Opcodes.NOP));
                }

                injectFakeLocals(mn);
                injectTryCatchBlocks(mn);
            }
            duplicateMethods(cn);

            log("Модуль StructuralObfuscator обфусцировал класс %s".formatted(cn.name));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void injectTryCatchBlocks(MethodNode mn) {
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

        if (mn.tryCatchBlocks == null) {
            mn.tryCatchBlocks = new ArrayList<>();
        }

        mn.tryCatchBlocks.add(new TryCatchBlockNode(start, end, handler, "java/lang/Exception"));
    }

    private static void injectFakeLocals(MethodNode mn) {
        LabelNode start = new LabelNode();
        LabelNode end = new LabelNode();

        mn.instructions.insert(start);
        mn.instructions.add(end);
        for (int i = 0; i < 2; i++) {
            String fakeName = "fakeVar%d".formatted(Math.abs(secureRandom.nextInt()));
            mn.localVariables.add(new LocalVariableNode(fakeName, "Ljava/lang/Object;", null, start, end, mn.maxLocals++));
        }
    }

    private static void duplicateMethods(ClassNode cn) {
        for (MethodNode mn : cn.methods.toArray(new MethodNode[0])) {
            if (mn.name.startsWith("<")) continue;

            MethodNode dubMn = new MethodNode(Opcodes.ASM9, mn.access, NameGeneratorModule.generateChineseName(), mn.desc, mn.signature, mn.exceptions.toArray(new String[0]));
            InsnList copyInstructions = new InsnList();
            HashMap<LabelNode, LabelNode> labelMap = new HashMap<>();
            for (AbstractInsnNode abstractInsnNode : mn.instructions.toArray()) {
                if (abstractInsnNode instanceof LabelNode ln) {
                    labelMap.put(ln, new LabelNode());
                }
            }
            for (AbstractInsnNode insn : mn.instructions.toArray()) {
                copyInstructions.add(insn.clone(labelMap));
            }
            dubMn.instructions = copyInstructions;
            if (mn.localVariables != null) {
                dubMn.localVariables = new ArrayList<>();
                for (LocalVariableNode localVariableNode : mn.localVariables) {
                    LabelNode s = labelMap.getOrDefault(localVariableNode.start, localVariableNode.start);
                    LabelNode e = labelMap.getOrDefault(localVariableNode.end, localVariableNode.end);
                    dubMn.localVariables.add(new LocalVariableNode(localVariableNode.name, localVariableNode.desc, localVariableNode.signature, s, e, localVariableNode.index));
                }
            }
            if (mn.tryCatchBlocks != null) {
                dubMn.tryCatchBlocks = new ArrayList<>();
                for (TryCatchBlockNode tcbn : mn.tryCatchBlocks) {
                    LabelNode s = labelMap.getOrDefault(tcbn.start, tcbn.start);
                    LabelNode e = labelMap.getOrDefault(tcbn.end, tcbn.end);
                    LabelNode h = labelMap.getOrDefault(tcbn.handler, tcbn.handler);
                    dubMn.tryCatchBlocks.add(new TryCatchBlockNode(s, e, h, tcbn.type));
                }
            }
            cn.methods.add(dubMn);
        }
    }
}