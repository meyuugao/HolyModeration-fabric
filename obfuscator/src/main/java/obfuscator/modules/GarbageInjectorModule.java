package obfuscator.modules;

import static obfuscator.modules.LoggerModule.log;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;

import obfuscator.ObfContext;

public class GarbageInjectorModule {
    private static final SecureRandom secureRandom = new SecureRandom();

    public static void obfuscateClass(ClassNode cn, ObfContext ctx) {
        GarbageInjectorModule.ensureClinitWithGarbage(cn);

        for (MethodNode mn : cn.methods) {
            injectGarbage(mn);
            insertArtLines(cn, mn, ctx);
        }

        log("Модуль GarbageInjector обфусцировал класс %s".formatted(cn.name));
    }

    private static void injectGarbage(MethodNode mn) {
        if (mn == null || mn.name.equals("<init>")) return;
        if ((mn.access & Opcodes.ACC_ABSTRACT) != 0 || (mn.access & Opcodes.ACC_NATIVE) != 0) return;

        InsnList instructions = generateGarbage(mn);
        if (mn.instructions == null || mn.instructions.size() == 0) {
            mn.instructions = instructions;
            mn.instructions.add(new InsnNode(Opcodes.RETURN));
        } else {
            mn.instructions.insert(instructions);
        }
    }

    private static InsnList generateGarbage(MethodNode mn) {
        InsnList insnList = new InsnList();
        int localIndex = mn.maxLocals;
        int blocks = 8 + secureRandom.nextInt(8);
        for (int b = 0; b < blocks; b++) {
            LabelNode skip = new LabelNode();
            insnList.add(new InsnNode(Opcodes.ICONST_0));
            insnList.add(new JumpInsnNode(Opcodes.IFEQ, skip));
            int ops = 6 + secureRandom.nextInt(20);
            for (int o = 0; o < ops; o++) {
                int kind = secureRandom.nextInt(4);
                if (kind == 0) {
                    int val = secureRandom.nextInt();
                    insnList.add(new LdcInsnNode(val));
                    insnList.add(new VarInsnNode(Opcodes.ISTORE, localIndex));
                    insnList.add(new VarInsnNode(Opcodes.ILOAD, localIndex));
                    int val2 = secureRandom.nextInt(1000) + 1;
                    insnList.add(new LdcInsnNode(val2));
                    insnList.add(new InsnNode(Opcodes.IMUL));
                    insnList.add(new VarInsnNode(Opcodes.ISTORE, localIndex));
                    localIndex++;
                } else if (kind == 1) {
                    long lval = Math.abs(secureRandom.nextLong());
                    insnList.add(new LdcInsnNode(lval));
                    insnList.add(new VarInsnNode(Opcodes.LSTORE, localIndex));
                    insnList.add(new VarInsnNode(Opcodes.LLOAD, localIndex));
                    insnList.add(new LdcInsnNode(secureRandom.nextLong()));
                    insnList.add(new InsnNode(Opcodes.LXOR));
                    insnList.add(new VarInsnNode(Opcodes.LSTORE, localIndex));
                    localIndex += 2;
                } else if (kind == 2) {
                    String s = NameGeneratorModule.generateRandomChinese(secureRandom.nextInt(10));
                    insnList.add(new LdcInsnNode(s));
                    insnList.add(new VarInsnNode(Opcodes.ASTORE, localIndex));
                    insnList.add(new VarInsnNode(Opcodes.ALOAD, localIndex));
                    insnList.add(new InsnNode(Opcodes.POP));
                    localIndex++;
                } else {
                    double d = secureRandom.nextDouble() * secureRandom.nextInt(Integer.MAX_VALUE);
                    insnList.add(new LdcInsnNode(d));
                    insnList.add(new VarInsnNode(Opcodes.DSTORE, localIndex));
                    insnList.add(new VarInsnNode(Opcodes.DLOAD, localIndex));
                    insnList.add(new LdcInsnNode(secureRandom.nextDouble()));
                    insnList.add(new InsnNode(Opcodes.DADD));
                    insnList.add(new VarInsnNode(Opcodes.DSTORE, localIndex));
                    localIndex += 2;
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

    private static void insertArtLines(ClassNode cn, MethodNode mn, ObfContext ctx) {
        if (mn == null || mn.name.equals("<init>") || mn.instructions == null) return;
        if ((mn.access & Opcodes.ACC_ABSTRACT) != 0 || (mn.access & Opcodes.ACC_NATIVE) != 0) return;

        List<String> ARTS = ctx.arts;
        Collections.shuffle(ARTS, secureRandom);
        for (int artIndex = 0; artIndex < ARTS.size(); artIndex++) {
            String ART = ARTS.get(artIndex);
            String[] lines = ART.split("\n");
            String fieldName;
            int suffix = 0;
            boolean fieldExists;
            do {
                fieldName = "me_yuugaos_property_%s_%s".formatted(artIndex, suffix);
                suffix++;
                fieldExists = false;
                for (FieldNode field : cn.fields) {
                    if (fieldName.equals(field.name)) {
                        fieldExists = true;
                        break;
                    }
                }
            } while (fieldExists);
            cn.fields.add(new FieldNode(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC, fieldName, "[Ljava/lang/String;", null, null));
            InsnList instructions = new InsnList();
            instructions.add(new LdcInsnNode(lines.length));
            instructions.add(new TypeInsnNode(Opcodes.ANEWARRAY, "java/lang/String"));
            instructions.add(new FieldInsnNode(Opcodes.PUTSTATIC, cn.name, fieldName, "[Ljava/lang/String;"));
            for (int i = 0; i < lines.length; i++) {
                instructions.add(new FieldInsnNode(Opcodes.GETSTATIC, cn.name, fieldName, "[Ljava/lang/String;"));
                instructions.add(new LdcInsnNode(i));
                instructions.add(new LdcInsnNode(lines[i]));
                instructions.add(new InsnNode(Opcodes.AASTORE));
            }
            AbstractInsnNode first = mn.instructions.getFirst();
            mn.instructions.insertBefore(first, instructions);
        }
    }
}