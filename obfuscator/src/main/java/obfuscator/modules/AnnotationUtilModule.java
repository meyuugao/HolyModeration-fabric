package obfuscator.modules;

import org.objectweb.asm.tree.AnnotationNode;

import java.util.EnumSet;
import java.util.List;

import obfuscator.ObfRule;

public final class AnnotationUtilModule {
    public static EnumSet<ObfRule> readRules(AnnotationNode an) {
        EnumSet<ObfRule> set = EnumSet.noneOf(ObfRule.class);
        if (an == null || an.values == null) return set;

        for (int i = 0; i < an.values.size(); i += 2) {
            Object key = an.values.get(i);
            Object val = an.values.get(i + 1);
            if (!key.equals("value")) continue;

            if (val instanceof List<?> lst) {
                for (Object item : lst) {
                    if (item instanceof String[] sa) {
                        if (sa.length >= 2) set.add(ObfRule.valueOf(sa[1]));
                    } else if (item instanceof List<?> la) {
                        if (la.size() >= 2) set.add(ObfRule.valueOf(la.get(1).toString()));
                    } else if (item != null) {
                        set.add(ObfRule.valueOf(item.toString()));
                    }
                }
            }
        }

        return set;
    }
}