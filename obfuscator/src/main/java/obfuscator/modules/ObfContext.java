package obfuscator.modules;

import obfuscator.ObfRule;

import org.objectweb.asm.tree.ClassNode;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class ObfContext {
    public final Map<String, String> classMap = new HashMap<>();
    public final Map<String, String> methodMap = new HashMap<>();
    public final Map<String, String> fieldMap = new HashMap<>();
    public final Map<String, String> paramMap = new HashMap<>();
    public final Map<String, byte[]> classBytes = new HashMap<>();
    public final Map<String, ClassNode> classNodes = new HashMap<>();
    public final Map<String, Set<String>> superClasses = new HashMap<>();
    public final Set<String> dontObfClasses = new HashSet<>();
    public final Set<String> dontObfMethods = new HashSet<>();
    public final Set<String> dontObfFields = new HashSet<>();
    public final Map<String, java.util.EnumSet<ObfRule>> dontObfRules = new java.util.HashMap<>();

    public final String mainPrefix = "me/yuugao/holymoderation/";
    public final String dontObfAnnotationClass = "obfuscator/DontObf";
    public final String[] protectedPrefixes = new String[]{"me/yuugao/holymoderation", "me/yuugao/holymoderation/client"};
    public final String mixinPrefix = "me/yuugao/holymoderation/client/mixin";
}