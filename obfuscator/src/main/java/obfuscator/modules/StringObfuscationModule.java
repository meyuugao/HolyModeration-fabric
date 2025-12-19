package obfuscator.modules;

import static obfuscator.modules.LoggerModule.log;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.AbstractMap;

public class StringObfuscationModule {
    private static final SecureRandom secureRandom = new SecureRandom();

    public static void obfuscateClass(ClassNode cn, String decoderMethodName) {
        if ((cn.access & Opcodes.ACC_ANNOTATION) != 0) return;

        for (MethodNode mn : cn.methods) {
            if (mn == null || mn.name.equals(decoderMethodName)) continue;

            obfuscateMethod(cn, mn, decoderMethodName);
        }
        obfuscateFields(cn, decoderMethodName);

        log("Модуль StringEncryption обфусцировал класс %s".formatted(cn.name));
    }

    private static void obfuscateMethod(ClassNode cn, MethodNode mn, String decoderMethodName) {
        if (mn.instructions == null) return;

        for (AbstractInsnNode insn : mn.instructions.toArray()) {
            if (!(insn instanceof LdcInsnNode ldcInsnNode) || !(ldcInsnNode.cst instanceof String s) || s.isEmpty())
                continue;

            AbstractMap.SimpleEntry<byte[], Integer> encWithKey = getEncWithKey(s);
            byte[] enc = encWithKey.getKey();
            int key = encWithKey.getValue();

            InsnList insnList = buildObfuscatedInsn(cn.name, decoderMethodName, enc, key);
            mn.instructions.insert(insn, insnList);
            mn.instructions.remove(insn);
        }
    }

    private static void obfuscateFields(ClassNode cn, String decoderMethodName) {
        for (FieldNode fn : cn.fields) {
            if (!"Ljava/lang/String;".equals(fn.desc) || !(fn.value instanceof String s) || s.isEmpty()) continue;

            AbstractMap.SimpleEntry<byte[], Integer> encWithKey = getEncWithKey(s);
            byte[] enc = encWithKey.getKey();
            int key = encWithKey.getValue();

            fn.value = null;
            boolean isStatic = (fn.access & Opcodes.ACC_STATIC) != 0;
            MethodNode init = getOrCreateInit(cn, isStatic);
            InsnList insnList = buildObfuscatedInsn(cn.name, decoderMethodName, enc, key);
            if (isStatic) {
                insnList.add(new FieldInsnNode(Opcodes.PUTSTATIC, cn.name, fn.name, fn.desc));
            } else {
                insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
                insnList.add(new InsnNode(Opcodes.SWAP));
                insnList.add(new FieldInsnNode(Opcodes.PUTFIELD, cn.name, fn.name, fn.desc));
            }
            init.instructions.insertBefore(init.instructions.getLast(), insnList);
        }
    }

    private static InsnList buildObfuscatedInsn(String owner, String decoder, byte[] enc, int key) {
        InsnList insnList = new InsnList();
        insnList.add(new LdcInsnNode(enc.length));
        insnList.add(new IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_BYTE));
        for (int i = 0; i < enc.length; i++) {
            insnList.add(new InsnNode(Opcodes.DUP));
            insnList.add(new LdcInsnNode(i));
            insnList.add(new LdcInsnNode(enc[i]));
            insnList.add(new InsnNode(Opcodes.BASTORE));
        }
        insnList.add(new LdcInsnNode(key));
        insnList.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, decoder, "([BI)Ljava/lang/String;", false));

        return insnList;
    }

    private static MethodNode getOrCreateInit(ClassNode cn, boolean isStatic) {
        String name = isStatic ? "<clinit>" : "<init>";
        for (MethodNode mn : cn.methods) if (mn.name.equals(name)) return mn;

        MethodNode mn = new MethodNode(isStatic ? Opcodes.ACC_STATIC : Opcodes.ACC_PUBLIC, name, "()V", null, null);
        InsnList insnList = mn.instructions;
        if (!isStatic) {
            insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
            insnList.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false));
        }
        insnList.add(new InsnNode(Opcodes.RETURN));
        mn.maxStack = 6;
        mn.maxLocals = isStatic ? 0 : 1;
        cn.methods.add(mn);

        return mn;
    }

    public static void injectDecoder(ClassNode cn, String name) {
        if ((cn.access & Opcodes.ACC_ANNOTATION) != 0) return;
        MethodNode mn = new MethodNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, name, "([BI)Ljava/lang/String;", null, null);
        InsnList insnList = mn.instructions;
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insnList.add(new InsnNode(Opcodes.ARRAYLENGTH));
        insnList.add(new IntInsnNode(Opcodes.NEWARRAY, Opcodes.T_BYTE));
        insnList.add(new VarInsnNode(Opcodes.ASTORE, 2));
        insnList.add(new InsnNode(Opcodes.ICONST_0));
        insnList.add(new VarInsnNode(Opcodes.ISTORE, 3));
        LabelNode loop = new LabelNode();
        LabelNode end = new LabelNode();
        insnList.add(loop);
        insnList.add(new VarInsnNode(Opcodes.ILOAD, 3));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insnList.add(new InsnNode(Opcodes.ARRAYLENGTH));
        insnList.add(new JumpInsnNode(Opcodes.IF_ICMPGE, end));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 2));
        insnList.add(new VarInsnNode(Opcodes.ILOAD, 3));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 0));
        insnList.add(new VarInsnNode(Opcodes.ILOAD, 3));
        insnList.add(new InsnNode(Opcodes.BALOAD));
        insnList.add(new VarInsnNode(Opcodes.ILOAD, 1));
        insnList.add(new InsnNode(Opcodes.IXOR));
        insnList.add(new InsnNode(Opcodes.I2B));
        insnList.add(new InsnNode(Opcodes.BASTORE));
        insnList.add(new IincInsnNode(3, 1));
        insnList.add(new JumpInsnNode(Opcodes.GOTO, loop));
        insnList.add(end);
        insnList.add(new TypeInsnNode(Opcodes.NEW, "java/lang/String"));
        insnList.add(new InsnNode(Opcodes.DUP));
        insnList.add(new VarInsnNode(Opcodes.ALOAD, 2));
        insnList.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/nio/charset/StandardCharsets", "UTF_8", "Ljava/nio/charset/Charset;"));
        insnList.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/String", "<init>", "([BLjava/nio/charset/Charset;)V", false));
        insnList.add(new InsnNode(Opcodes.ARETURN));
        mn.maxStack = 6;
        mn.maxLocals = 4;
        cn.methods.add(mn);
    }

    private static AbstractMap.SimpleEntry<byte[], Integer> getEncWithKey(String s) {
        byte[] data = s.getBytes(StandardCharsets.UTF_8);
        int key = secureRandom.nextInt(255) + 1;
        byte[] enc = new byte[data.length];
        for (int i = 0; i < data.length; i++) enc[i] = (byte) ((data[i] & 0xFF) ^ key);
        return new AbstractMap.SimpleEntry<>(enc, key);
    }
}