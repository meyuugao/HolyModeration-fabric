package obfuscator.modules;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;

public class StringEncryptionModule {
    private static final SecureRandom secureRandom = new SecureRandom();

    public static void obfuscateClass(ClassNode cn, String decoderName) {
        if ((cn.access & Opcodes.ACC_ANNOTATION) != 0) return;
        for (MethodNode mn : cn.methods) {
            if (mn == null) continue;
            if (mn.name.equals(decoderName)) continue;
            obfuscateMethod(cn, mn, decoderName);
        }
        obfuscateFields(cn, decoderName);
    }

    private static void obfuscateMethod(ClassNode cn, MethodNode mn, String decoderName) {
        if (mn.instructions == null) return;
        for (AbstractInsnNode insn : mn.instructions.toArray()) {
            if (!(insn instanceof LdcInsnNode ldc)) continue;
            if (!(ldc.cst instanceof String s)) continue;
            if (s.isEmpty()) continue;
            byte[] data = s.getBytes(StandardCharsets.UTF_8);
            int key = secureRandom.nextInt(255) + 1;
            byte[] enc = new byte[data.length];
            for (int i = 0; i < data.length; i++) enc[i] = (byte) ((data[i] & 0xFF) ^ key);
            InsnList il = buildObfuscatedInsn(cn.name, decoderName, enc, key);
            mn.instructions.insert(insn, il);
            mn.instructions.remove(insn);
        }
    }

    private static void obfuscateFields(ClassNode cn, String decoderName) {
        for (FieldNode fn : cn.fields) {
            if (!"Ljava/lang/String;".equals(fn.desc)) continue;
            if (!(fn.value instanceof String s)) continue;
            if (s.isEmpty()) continue;
            byte[] data = s.getBytes(StandardCharsets.UTF_8);
            int key = secureRandom.nextInt(255) + 1;
            byte[] enc = new byte[data.length];
            for (int i = 0; i < data.length; i++) enc[i] = (byte) ((data[i] & 0xFF) ^ key);
            fn.value = null;
            boolean isStatic = (fn.access & Opcodes.ACC_STATIC) != 0;
            MethodNode init = getOrCreateInit(cn, isStatic);
            InsnList il = buildObfuscatedInsn(cn.name, decoderName, enc, key);
            if (isStatic) {
                il.add(new FieldInsnNode(Opcodes.PUTSTATIC, cn.name, fn.name, fn.desc));
            } else {
                il.add(new VarInsnNode(Opcodes.ALOAD, 0));
                il.add(new InsnNode(Opcodes.SWAP));
                il.add(new FieldInsnNode(Opcodes.PUTFIELD, cn.name, fn.name, fn.desc));
            }
            init.instructions.insertBefore(init.instructions.getLast(), il);
        }
    }

    private static InsnList buildObfuscatedInsn(String owner, String decoder, byte[] enc, int key) {
        InsnList il = new InsnList();
        il.add(new LdcInsnNode(enc.length));
        il.add(new IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_BYTE));
        for (int i = 0; i < enc.length; i++) {
            il.add(new InsnNode(Opcodes.DUP));
            il.add(new LdcInsnNode(i));
            il.add(new LdcInsnNode(enc[i]));
            il.add(new InsnNode(Opcodes.BASTORE));
        }
        il.add(new LdcInsnNode(key));
        il.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, decoder, "([BI)Ljava/lang/String;", false));
        return il;
    }

    private static MethodNode getOrCreateInit(ClassNode cn, boolean isStatic) {
        String name = isStatic ? "<clinit>" : "<init>";
        for (MethodNode m : cn.methods) if (m.name.equals(name)) return m;
        MethodNode m = new MethodNode(isStatic ? Opcodes.ACC_STATIC : Opcodes.ACC_PUBLIC, name, "()V", null, null);
        InsnList il = m.instructions;
        if (!isStatic) {
            il.add(new VarInsnNode(Opcodes.ALOAD, 0));
            il.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false));
        }
        il.add(new InsnNode(Opcodes.RETURN));
        m.maxStack = 6;
        m.maxLocals = isStatic ? 0 : 1;
        cn.methods.add(m);
        return m;
    }

    public static void injectDecoder(ClassNode cn, String name) {
        if ((cn.access & Opcodes.ACC_ANNOTATION) != 0) return;
        MethodNode m = new MethodNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, name, "([BI)Ljava/lang/String;", null, null);
        InsnList il = m.instructions;
        il.add(new VarInsnNode(Opcodes.ALOAD, 0));
        il.add(new InsnNode(Opcodes.ARRAYLENGTH));
        il.add(new IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_BYTE));
        il.add(new VarInsnNode(Opcodes.ASTORE, 2));
        il.add(new InsnNode(Opcodes.ICONST_0));
        il.add(new VarInsnNode(Opcodes.ISTORE, 3));
        LabelNode loop = new LabelNode();
        LabelNode end = new LabelNode();
        il.add(loop);
        il.add(new VarInsnNode(Opcodes.ILOAD, 3));
        il.add(new VarInsnNode(Opcodes.ALOAD, 0));
        il.add(new InsnNode(Opcodes.ARRAYLENGTH));
        il.add(new JumpInsnNode(Opcodes.IF_ICMPGE, end));
        il.add(new VarInsnNode(Opcodes.ALOAD, 2));
        il.add(new VarInsnNode(Opcodes.ILOAD, 3));
        il.add(new VarInsnNode(Opcodes.ALOAD, 0));
        il.add(new VarInsnNode(Opcodes.ILOAD, 3));
        il.add(new InsnNode(Opcodes.BALOAD));
        il.add(new VarInsnNode(Opcodes.ILOAD, 1));
        il.add(new InsnNode(Opcodes.IXOR));
        il.add(new InsnNode(Opcodes.I2B));
        il.add(new InsnNode(Opcodes.BASTORE));
        il.add(new IincInsnNode(3, 1));
        il.add(new JumpInsnNode(Opcodes.GOTO, loop));
        il.add(end);
        il.add(new TypeInsnNode(Opcodes.NEW, "java/lang/String"));
        il.add(new InsnNode(Opcodes.DUP));
        il.add(new VarInsnNode(Opcodes.ALOAD, 2));
        il.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/nio/charset/StandardCharsets", "UTF_8", "Ljava/nio/charset/Charset;"));
        il.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/String", "<init>", "([BLjava/nio/charset/Charset;)V", false));
        il.add(new InsnNode(Opcodes.ARETURN));
        m.maxStack = 6;
        m.maxLocals = 4;
        cn.methods.add(m);
    }
}