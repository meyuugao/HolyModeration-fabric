package obfuscator.modules;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.security.SecureRandom;

public class PrimitiveObfuscationModule {
    private static final SecureRandom secureRandom = new SecureRandom();

    public static void obfuscate(MethodNode mn) {
        if (mn.instructions == null) return;

        for (AbstractInsnNode insn : mn.instructions.toArray()) {

            if (insn instanceof LdcInsnNode ldc) {
                Object c = ldc.cst;

                if (c instanceof Integer i) {
                    replace(mn, insn, buildInt(i));
                } else if (c instanceof Long l) {
                    replace(mn, insn, buildLong(l));
                } else if (c instanceof Float f) {
                    replace(mn, insn, buildFloat(f));
                } else if (c instanceof Double d) {
                    replace(mn, insn, buildDouble(d));
                }
            }

            if (insn.getOpcode() == Opcodes.ICONST_0 || insn.getOpcode() == Opcodes.ICONST_1) {
                int v = insn.getOpcode() == Opcodes.ICONST_1 ? 1 : 0;
                replace(mn, insn, buildBoolean(v == 1));
            }
        }
    }

    private static void replace(MethodNode mn, AbstractInsnNode old, InsnList il) {
        mn.instructions.insert(old, il);
        mn.instructions.remove(old);
    }

    private static InsnList buildInt(int v) {
        int k1 = secureRandom.nextInt();
        int k2 = secureRandom.nextInt();
        int enc = (v ^ k1) + k2;

        InsnList il = new InsnList();
        il.add(new LdcInsnNode(enc));
        il.add(new LdcInsnNode(k2));
        il.add(new InsnNode(Opcodes.ISUB));
        il.add(new LdcInsnNode(k1));
        il.add(new InsnNode(Opcodes.IXOR));

        runtime(il);

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

        runtime(il);

        return il;
    }

    private static InsnList buildLong(long v) {
        long k1 = secureRandom.nextLong();
        long k2 = secureRandom.nextLong();
        long enc = (v ^ k1) + k2;

        InsnList il = new InsnList();
        il.add(new LdcInsnNode(enc));
        il.add(new LdcInsnNode(k2));
        il.add(new InsnNode(Opcodes.LSUB));
        il.add(new LdcInsnNode(k1));
        il.add(new InsnNode(Opcodes.LXOR));

        runtime(il);

        return il;
    }

    private static InsnList buildFloat(float v) {
        InsnList il = buildInt(Float.floatToIntBits(v));
        il.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "java/lang/Float",
                "intBitsToFloat",
                "(I)F",
                false
        ));
        return il;
    }

    private static InsnList buildDouble(double v) {
        InsnList il = buildLong(Double.doubleToLongBits(v));
        il.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "java/lang/Double",
                "longBitsToDouble",
                "(J)D",
                false
        ));
        return il;
    }

    private static void flow(InsnList il) {
        LabelNode l = new LabelNode();
        il.add(new InsnNode(Opcodes.DUP));
        il.add(new JumpInsnNode(Opcodes.IFGE, l));
        il.add(new InsnNode(Opcodes.POP));
        il.add(l);
    }

    private static void runtime(InsnList il) {
        il.add(new MethodInsnNode(
                Opcodes.INVOKESTATIC,
                "java/lang/System",
                "nanoTime",
                "()J",
                false
        ));
        il.add(new InsnNode(Opcodes.POP2));
    }
}