package obfuscator.modules;

import static obfuscator.modules.LoggerModule.log;


import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;

public final class ClassTransformer {
    private ClassTransformer() {
    }

    public static byte[] transform(byte[] bytes, ObfContext ctx, ObfRemapper remapper) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);

        String internalName = classNode.name;
        boolean isDontObf = ctx.dontObfClasses.contains(internalName);

        if (!isDontObf) {
            for (MethodNode method : classNode.methods) {
                String mk = internalName + "." + method.name + method.desc;
                if (ctx.dontObfMethods.contains(mk)) continue;
                if (method.localVariables != null && !method.localVariables.isEmpty()) {
                    for (LocalVariableNode lv : method.localVariables) {
                        if (!"this".equals(lv.name)) {
                            lv.name = NameGenerator.generateChineseName();
                            log("ЛОКАЛЬНАЯ ПЕРЕМЕННАЯ: " + internalName + "." + method.name + " -> " + lv.name);
                        }
                    }
                }
            }

            String decoderMethodName = NameGenerator.generateChineseName();
            StringEncryptionModule.injectDecoder(classNode, decoderMethodName);
            StringEncryptionModule.obfuscateFields(classNode, decoderMethodName);

            for (MethodNode methodNode : classNode.methods) {
                StringEncryptionModule.obfuscateMethods(classNode, methodNode, decoderMethodName);

                OpaquePredicateModule.obfuscate(methodNode);

                StackAbuseModule.obfuscate(methodNode);

                ControlFlowFlatteningModule.obfuscate(methodNode);

                FakeExceptionFlowModule.obfuscate(methodNode);
                ExceptionStateLoopModule.obfuscate(methodNode);

                SwitchBombModule.obfuscate(methodNode);

                GarbageInjector.injectGarbage(methodNode);
                GarbageInjector.insertArtLines(classNode, methodNode);
            }

            StructuralObfuscatorModule.obfuscateClass(classNode);

            GarbageInjector.ensureClinitWithGarbage(classNode);
        }

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassRemapper classRemapper = new ClassRemapper(writer, remapper);
        for (MethodNode m : classNode.methods) {
            m.parameters = null;
            m.signature = null;
            m.access = m.access & ~Opcodes.ACC_SYNTHETIC;
            m.access = m.access & ~Opcodes.ACC_MANDATED;
        }
        classNode.accept(classRemapper);
        return writer.toByteArray();
    }
}