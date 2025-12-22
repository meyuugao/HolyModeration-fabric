package obfuscator.modules;

import static obfuscator.modules.LoggerModule.log;


import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

public class AntiDebugInjectModule {
    public static void obfuscateClass(ClassNode cn) {
        for (MethodNode mn : cn.methods) {
            if (mn.name.startsWith("<") || mn.instructions == null || mn.instructions.size() == 0) continue;

            InsnList insn = new InsnList();

            insn.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "java/lang/management/ManagementFactory",
                    "getRuntimeMXBean",
                    "()Ljava/lang/management/RuntimeMXBean;",
                    false
            ));

            insn.add(new MethodInsnNode(
                    Opcodes.INVOKEINTERFACE,
                    "java/lang/management/RuntimeMXBean",
                    "getInputArguments",
                    "()Ljava/util/List;",
                    true
            ));

            insn.add(new VarInsnNode(Opcodes.ASTORE, mn.maxLocals));
            int listVar = mn.maxLocals;
            mn.maxLocals++;

            insn.add(new VarInsnNode(Opcodes.ALOAD, listVar));
            insn.add(new MethodInsnNode(
                    Opcodes.INVOKEINTERFACE,
                    "java/util/List",
                    "iterator",
                    "()Ljava/util/Iterator;",
                    true
            ));

            insn.add(new VarInsnNode(Opcodes.ASTORE, mn.maxLocals));
            int itVar = mn.maxLocals;
            mn.maxLocals++;

            LabelNode loopStart = new LabelNode();
            LabelNode loopEnd = new LabelNode();

            insn.add(loopStart);
            insn.add(new VarInsnNode(Opcodes.ALOAD, itVar));
            insn.add(new MethodInsnNode(
                    Opcodes.INVOKEINTERFACE,
                    "java/util/Iterator",
                    "hasNext",
                    "()Z",
                    true
            ));
            insn.add(new JumpInsnNode(Opcodes.IFEQ, loopEnd));

            insn.add(new VarInsnNode(Opcodes.ALOAD, itVar));
            insn.add(new MethodInsnNode(
                    Opcodes.INVOKEINTERFACE,
                    "java/util/Iterator",
                    "next",
                    "()Ljava/lang/Object;",
                    true
            ));
            insn.add(new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/String"));
            insn.add(new VarInsnNode(Opcodes.ASTORE, mn.maxLocals));
            int argVar = mn.maxLocals;
            mn.maxLocals++;

            insn.add(new VarInsnNode(Opcodes.ALOAD, argVar));
            insn.add(new LdcInsnNode("jdwp"));
            insn.add(new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/String",
                    "contains",
                    "(Ljava/lang/CharSequence;)Z",
                    false
            ));

            LabelNode noDebug = new LabelNode();
            insn.add(new JumpInsnNode(Opcodes.IFEQ, noDebug));

            insn.add(new MethodInsnNode(
                    Opcodes.INVOKESTATIC,
                    "java/lang/Runtime",
                    "getRuntime",
                    "()Ljava/lang/Runtime;",
                    false
            ));
            insn.add(new InsnNode(Opcodes.ICONST_0));
            insn.add(new MethodInsnNode(
                    Opcodes.INVOKEVIRTUAL,
                    "java/lang/Runtime",
                    "halt",
                    "(I)V",
                    false
            ));

            insn.add(noDebug);
            insn.add(new JumpInsnNode(Opcodes.GOTO, loopStart));
            insn.add(loopEnd);

            mn.instructions.insert(insn);
            mn.maxStack += 4;
        }

        log("Модуль AntiDebugInject обфусцировал класс %s".formatted(cn.name));
    }
}