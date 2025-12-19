package obfuscator.modules;

import static obfuscator.modules.LoggerModule.log;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.security.SecureRandom;

public class PrimitiveObfuscationModule {
    private static final SecureRandom secureRandom = new SecureRandom();

    public static void obfuscateClass(ClassNode cn) {
        for (MethodNode mn : cn.methods) {
            if (mn.name.startsWith("<") || mn.instructions == null) continue;

            obfuscateMethod(mn);
        }

        log("Модуль PrimitiveObfuscation обфусцировал класс %s".formatted(cn.name));
    }

    private static void obfuscateMethod(MethodNode mn) {
        for (AbstractInsnNode insn : mn.instructions.toArray()) {
            if (insn instanceof LdcInsnNode ldcInsnNode) {
                Object c = ldcInsnNode.cst;
                if (c instanceof Integer i) replace(mn, insn, buildInt(i));
                else if (c instanceof Long l) replace(mn, insn, buildLong(l));
                else if (c instanceof Float f) replace(mn, insn, buildFloat(f));
                else if (c instanceof Double d) replace(mn, insn, buildDouble(d));
            }

            int op = insn.getOpcode();
            if (op == Opcodes.ICONST_0 || op == Opcodes.ICONST_1) {
                replace(mn, insn, buildBoolean(op == Opcodes.ICONST_1));
            }
        }
    }

    private static void replace(MethodNode mn, AbstractInsnNode previousInsn, InsnList insnList) {
        mn.instructions.insert(previousInsn, insnList);
        mn.instructions.remove(previousInsn);
    }

    private static InsnList buildInt(int i) {
        int k1 = secureRandom.nextInt();
        int k2 = secureRandom.nextInt();
        int enc = (i ^ k1) + k2;
        InsnList il = new InsnList();
        il.add(new LdcInsnNode(enc));
        il.add(new LdcInsnNode(k2));
        il.add(new InsnNode(Opcodes.ISUB));
        il.add(new LdcInsnNode(k1));
        il.add(new InsnNode(Opcodes.IXOR));
        il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false));
        il.add(new InsnNode(Opcodes.POP2));

        return il;
    }

    private static InsnList buildBoolean(boolean v) {
        int a = secureRandom.nextInt();
        int b = v ? a : ~a;
        LabelNode l1 = new LabelNode();
        LabelNode l2 = new LabelNode();
        InsnList il = new InsnList();
        il.add(new LdcInsnNode(b));
        il.add(new LdcInsnNode(a));
        il.add(new JumpInsnNode(Opcodes.IF_ICMPNE, l1));
        il.add(new InsnNode(Opcodes.ICONST_1));
        il.add(new JumpInsnNode(Opcodes.GOTO, l2));
        il.add(l1);
        il.add(new InsnNode(Opcodes.ICONST_0));
        il.add(l2);
        il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false));
        il.add(new InsnNode(Opcodes.POP2));

        return il;
    }

    private static InsnList buildLong(long l) {
        long k1 = secureRandom.nextLong();
        long k2 = secureRandom.nextLong();
        long enc = (l ^ k1) + k2;
        InsnList il = new InsnList();
        il.add(new LdcInsnNode(enc));
        il.add(new LdcInsnNode(k2));
        il.add(new InsnNode(Opcodes.LSUB));
        il.add(new LdcInsnNode(k1));
        il.add(new InsnNode(Opcodes.LXOR));
        il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false));
        il.add(new InsnNode(Opcodes.POP2));

        return il;
    }

    private static InsnList buildFloat(float f) {
        InsnList il = buildInt(Float.floatToIntBits(f));
        il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Float", "intBitsToFloat", "(I)F", false));

        return il;
    }

    private static InsnList buildDouble(double d) {
        InsnList il = buildLong(Double.doubleToLongBits(d));
        il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Double", "longBitsToDouble", "(J)D", false));

        return il;
    }
}