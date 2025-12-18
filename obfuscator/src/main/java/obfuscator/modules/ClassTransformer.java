package obfuscator.modules;

import static obfuscator.modules.LoggerModule.log;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import obfuscator.ObfRule;

import java.util.EnumSet;

public final class ClassTransformer {
    private ClassTransformer() {}

    public static byte[] transform(byte[] bytes, ObfContext ctx, ObfRemapper remapper) {
        org.objectweb.asm.ClassReader reader = new org.objectweb.asm.ClassReader(bytes);
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, org.objectweb.asm.ClassReader.EXPAND_FRAMES);

        String internalName = classNode.name;

        EnumSet<ObfRule> classRules = ctx.dontObfRules.getOrDefault(internalName, EnumSet.noneOf(ObfRule.class));

        if (!classRules.contains(ObfRule.RENAME_LOCALVARS) || true) {
            for (MethodNode method : classNode.methods) {
                String mk = internalName + "." + method.name + method.desc;
                EnumSet<ObfRule> methodRules = ctx.dontObfRules.getOrDefault(mk, EnumSet.noneOf(ObfRule.class));
                EnumSet<ObfRule> combined = EnumSet.copyOf(classRules);
                combined.addAll(methodRules);

                if (!combined.contains(ObfRule.RENAME_LOCALVARS) &&
                        method.localVariables != null && !method.localVariables.isEmpty()) {
                    for (LocalVariableNode lv : method.localVariables) {
                        if (!"this".equals(lv.name)) {
                            lv.name = NameGenerator.generateChineseName();
                            log("ЛОКАЛЬНАЯ ПЕРЕМЕННАЯ: " + internalName + "." + method.name + " -> " + lv.name);
                        }
                    }
                }
            }
        }

        String decoderMethodName = NameGenerator.generateChineseName();
        if (!classRules.contains(ObfRule.OBFUSCATE_STRINGS)) {
            StringEncryptionModule.injectDecoder(classNode, decoderMethodName);
            StringEncryptionModule.obfuscateClass(classNode, decoderMethodName);
        }

        if (!classRules.contains(ObfRule.OBFUSCATE_PRIMITIVES)) {
            PrimitiveObfuscationModule.obfuscateClass(classNode);
        }

        if (!classRules.contains(ObfRule.OPAQUE_PREDICATE)) {
            OpaquePredicateModule.obfuscateClass(classNode);
        }

        if (!classRules.contains(ObfRule.STACK_ABUSE)) {
            StackAbuseModule.obfuscateClass(classNode);
        }

        if (!classRules.contains(ObfRule.CONTROL_FLOW)) {
            ControlFlowFlatteningModule.obfuscateClass(classNode);
        }

        if (!classRules.contains(ObfRule.EXCEPTIONS)) {
            FakeExceptionFlowModule.obfuscateClass(classNode);
            ExceptionStateLoopModule.obfuscateClass(classNode);
        }

        if (!classRules.contains(ObfRule.SWITCH_BOMB)) {
            SwitchBombModule.obfuscateClass(classNode);
        }

        if (!classRules.contains(ObfRule.GARBAGE)) {
            GarbageInjector.obfuscateClass(classNode);
            GarbageInjector.ensureClinitWithGarbage(classNode);
        }

        StructuralObfuscatorModule.obfuscateClass(classNode);

        AnnotationCleanupModule.clean(classNode, ctx);

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