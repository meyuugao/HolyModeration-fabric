package obfuscator.modules;

import static obfuscator.modules.LoggerModule.log;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.security.SecureRandom;

public class GarbageInjectorModule {
    private static final SecureRandom secureRandom = new SecureRandom();

    public static void obfuscateClass(ClassNode cn) {
        ensureClinitWithGarbage(cn);

        for (MethodNode mn : cn.methods) {
            injectGarbage(mn);
        }

        log("GarbageInjector обфусцировал класс %s".formatted(cn.name));
    }

    private static void injectGarbage(MethodNode mn) {
        if (mn == null || mn.name.equals("<init>")) return;
        if ((mn.access & Opcodes.ACC_ABSTRACT) != 0 || (mn.access & Opcodes.ACC_NATIVE) != 0) return;

        if (mn.instructions == null || mn.instructions.size() == 0) {
            mn.instructions = generateGarbage(mn);
            mn.instructions.add(new InsnNode(Opcodes.RETURN));
        } else {
            mn.instructions.insert(generateGarbage(mn));
        }
    }

    private static InsnList generateGarbage(MethodNode mn) {
        InsnList insnList = new InsnList();
        int localIndex = mn.maxLocals;
        int blocks = 2 + secureRandom.nextInt(2); // минимальные блоки

        for (int b = 0; b < blocks; b++) {
            LabelNode skip = new LabelNode();
            insnList.add(new InsnNode(Opcodes.ICONST_0));
            insnList.add(new JumpInsnNode(Opcodes.IFEQ, skip));

            int ops = 2 + secureRandom.nextInt(4); // минимальные операции
            for (int o = 0; o < ops; o++) {
                int kind = secureRandom.nextInt(4);
                switch (kind) {
                    case 0 -> { // int
                        int val = secureRandom.nextInt();
                        insnList.add(new LdcInsnNode(val));
                        insnList.add(new VarInsnNode(Opcodes.ISTORE, localIndex));
                        insnList.add(new VarInsnNode(Opcodes.ILOAD, localIndex));
                        insnList.add(new InsnNode(Opcodes.POP));
                        localIndex++;
                    }
                    case 1 -> { // long
                        long val = secureRandom.nextLong();
                        insnList.add(new LdcInsnNode(val));
                        insnList.add(new VarInsnNode(Opcodes.LSTORE, localIndex));
                        insnList.add(new VarInsnNode(Opcodes.LLOAD, localIndex));
                        insnList.add(new InsnNode(Opcodes.POP2));
                        localIndex += 2;
                    }
                    case 2 -> { // String
                        insnList.add(new LdcInsnNode("garbage"));
                        insnList.add(new VarInsnNode(Opcodes.ASTORE, localIndex));
                        insnList.add(new VarInsnNode(Opcodes.ALOAD, localIndex));
                        insnList.add(new InsnNode(Opcodes.POP));
                        localIndex++;
                    }
                    case 3 -> { // double
                        double val = secureRandom.nextDouble();
                        insnList.add(new LdcInsnNode(val));
                        insnList.add(new VarInsnNode(Opcodes.DSTORE, localIndex));
                        insnList.add(new VarInsnNode(Opcodes.DLOAD, localIndex));
                        insnList.add(new InsnNode(Opcodes.POP2));
                        localIndex += 2;
                    }
                }
            }
            insnList.add(skip);
        }

        mn.maxLocals = Math.max(mn.maxLocals, localIndex);
        return insnList;
    }

    private static void ensureClinitWithGarbage(ClassNode cn) {
        MethodNode clinit = cn.methods.stream()
                .filter(mn -> mn.name.equals("<clinit>"))
                .findFirst()
                .orElseGet(() -> {
                    MethodNode mn = new MethodNode(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
                    cn.methods.add(mn);
                    return mn;
                });
        injectGarbage(clinit);
    }
}