package obfuscator.modules;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodNode;
import obfuscator.ObfContext;
import obfuscator.ObfRule;

import java.util.EnumSet;

import static obfuscator.modules.LoggerModule.log;

public final class ClassTransformerModule {
    private ClassTransformerModule() {
    }

    public static byte[] transform(byte[] bytes, ObfContext ctx, ObfRemapperModule obfRemapper) {
        ClassReader reader = new ClassReader(bytes);
        ClassNode cn = new ClassNode();
        reader.accept(cn, ClassReader.EXPAND_FRAMES);

        String internalName = cn.name;

        EnumSet<ObfRule> classRules = ctx.dontObfRules.getOrDefault(internalName, EnumSet.noneOf(ObfRule.class));

        AssetsObfuscatorModule.replaceShaderNamesInClass(cn, ctx);

        if (!classRules.contains(ObfRule.MAP_LOCALVARS)) {
            for (MethodNode mn : cn.methods) {
                String mk = "%s.%s%s".formatted(internalName, mn.name, mn.desc);
                EnumSet<ObfRule> methodRules = ctx.dontObfRules.getOrDefault(mk, EnumSet.noneOf(ObfRule.class));
                EnumSet<ObfRule> combined = EnumSet.copyOf(classRules);
                combined.addAll(methodRules);

                if (!combined.contains(ObfRule.MAP_LOCALVARS) &&
                        mn.localVariables != null && !mn.localVariables.isEmpty()) {
                    for (LocalVariableNode lv : mn.localVariables) {
                        if (!lv.name.equals("this")) {
                            lv.name = NameGeneratorModule.generateChineseName();
                            log("Обфускация локальной переменной: %s.%s -> %s".formatted(internalName, mn.name, lv.name));
                        }
                    }
                }
            }
        }

        String decoderMethodName = NameGeneratorModule.generateChineseName();
        if (!classRules.contains(ObfRule.OBF_STRING)) {
            StringObfuscationModule.injectDecoder(cn, decoderMethodName);
            StringObfuscationModule.obfuscateClass(cn, decoderMethodName);
        }

        if (!classRules.contains(ObfRule.OBF_PRIMITIVES)) {
            PrimitiveObfuscationModule.obfuscateClass(cn);
        }

        if (!classRules.contains(ObfRule.OPAQUE_PREDICATE)) {
            OpaquePredicateModule.obfuscateClass(cn);
        }

        if (!classRules.contains(ObfRule.STACK_ABUSE)) {
            StackAbuseModule.obfuscateClass(cn);
        }

        if (!classRules.contains(ObfRule.CONTROL_FLOW_FLATTENING)) {
            ControlFlowFlatteningModule.obfuscateClass(cn);
        }

        if (!classRules.contains(ObfRule.EXCEPTIONS)) {
            FakeExceptionFlowModule.obfuscateClass(cn);
            ExceptionStateLoopModule.obfuscateClass(cn);
        }

        if (!classRules.contains(ObfRule.SWITCH_BOMB)) {
            SwitchBombModule.obfuscateClass(cn);
        }

        if (!classRules.contains(ObfRule.GARBAGE_INJECT)) {
            GarbageInjectorModule.obfuscateClass(cn, ctx);
        }

        if (!classRules.contains(ObfRule.STRUCTURAL)) {
            StructuralObfuscatorModule.obfuscateClass(cn);
        }

        AnnotationCleanupModule.clean(cn, ctx);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassRemapper classRemapper = new ClassRemapper(writer, obfRemapper);
        for (MethodNode mn : cn.methods) {
            mn.parameters = null;
            mn.signature = null;
            mn.access = mn.access & ~Opcodes.ACC_SYNTHETIC;
            mn.access = mn.access & ~Opcodes.ACC_MANDATED;
        }
        cn.accept(classRemapper);

        return writer.toByteArray();
    }
}