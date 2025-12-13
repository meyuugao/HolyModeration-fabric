package obfuscator.modules;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.security.SecureRandom;
import java.util.Random;

public class GarbageInjector {
    private static final SecureRandom secureRandom = new SecureRandom();

    public void injectGarbage(MethodNode methodNode, Random rnd) {
        if ((methodNode.access & Opcodes.ACC_ABSTRACT) != 0) return;
        if ((methodNode.access & Opcodes.ACC_NATIVE) != 0) return;
        if ("<init>".equals(methodNode.name)) return;

        InsnList instructions = generateGarbage(methodNode);

        if (methodNode.instructions == null || methodNode.instructions.size() == 0) {
            methodNode.instructions = instructions;
            methodNode.instructions.add(new InsnNode(Opcodes.RETURN));
        } else {
            methodNode.instructions.insert(instructions);
        }
    }

    private InsnList generateGarbage(MethodNode methodNode) {
        InsnList instructions = new InsnList();
        int localIndex = methodNode.maxLocals;
        int blocks = 8 + secureRandom.nextInt(8);
        for (int b = 0; b < blocks; b++) {
            LabelNode skip = new LabelNode();
            instructions.add(new InsnNode(Opcodes.ICONST_0));
            instructions.add(new JumpInsnNode(Opcodes.IFEQ, skip));
            int ops = 6 + secureRandom.nextInt(20);
            for (int o = 0; o < ops; o++) {
                int kind = secureRandom.nextInt(4);
                if (kind == 0) {
                    int val = secureRandom.nextInt();
                    instructions.add(new LdcInsnNode(val));
                    instructions.add(new VarInsnNode(Opcodes.ISTORE, localIndex));
                    instructions.add(new VarInsnNode(Opcodes.ILOAD, localIndex));
                    int val2 = secureRandom.nextInt(1000) + 1;
                    instructions.add(new LdcInsnNode(val2));
                    instructions.add(new InsnNode(Opcodes.IMUL));
                    instructions.add(new VarInsnNode(Opcodes.ISTORE, localIndex));
                    localIndex++;
                } else if (kind == 1) {
                    long lval = Math.abs(secureRandom.nextLong());
                    instructions.add(new LdcInsnNode(lval));
                    instructions.add(new VarInsnNode(Opcodes.LSTORE, localIndex));
                    instructions.add(new VarInsnNode(Opcodes.LLOAD, localIndex));
                    instructions.add(new LdcInsnNode(secureRandom.nextLong()));
                    instructions.add(new InsnNode(Opcodes.LXOR));
                    instructions.add(new VarInsnNode(Opcodes.LSTORE, localIndex));
                    localIndex += 2;
                } else if (kind == 2) {
                    String s = NameGenerator.generateRandomChinese(secureRandom.nextInt(10));
                    instructions.add(new LdcInsnNode(s));
                    instructions.add(new VarInsnNode(Opcodes.ASTORE, localIndex));
                    instructions.add(new VarInsnNode(Opcodes.ALOAD, localIndex));
                    instructions.add(new InsnNode(Opcodes.POP));
                    localIndex++;
                } else {
                    double d = secureRandom.nextDouble() * secureRandom.nextInt(Integer.MAX_VALUE);
                    instructions.add(new LdcInsnNode(d));
                    instructions.add(new VarInsnNode(Opcodes.DSTORE, localIndex));
                    instructions.add(new VarInsnNode(Opcodes.DLOAD, localIndex));
                    instructions.add(new LdcInsnNode(secureRandom.nextDouble()));
                    instructions.add(new InsnNode(Opcodes.DADD));
                    instructions.add(new VarInsnNode(Opcodes.DSTORE, localIndex));
                    localIndex += 2;
                }
            }
            instructions.add(skip);
        }

        methodNode.maxLocals = Math.max(methodNode.maxLocals, localIndex);

        return instructions;
    }
}