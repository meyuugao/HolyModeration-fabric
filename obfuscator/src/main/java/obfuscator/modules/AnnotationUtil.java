package obfuscator.modules;

import obfuscator.ObfRule;
import org.objectweb.asm.tree.AnnotationNode;

import java.util.EnumSet;
import java.util.List;

public final class AnnotationUtil {
    public static EnumSet<ObfRule> readRules(AnnotationNode an) {
        EnumSet<ObfRule> set = EnumSet.noneOf(ObfRule.class);
        if (an == null || an.values == null) return set;
        for (int i = 0; i < an.values.size(); i += 2) {
            Object key = an.values.get(i);
            Object val = an.values.get(i + 1);
            if (!"value".equals(key)) continue;
            if (val instanceof List<?> lst) {
                for (Object item : lst) {
                    String name = null;
                    if (item instanceof String[] sa) {
                        if (sa.length >= 2) name = sa[1];
                    } else if (item instanceof List<?> la) {
                        if (la.size() >= 2) name = la.get(1).toString();
                    } else if (item != null) {
                        name = item.toString();
                    }
                    if (name != null) {
                        try {
                            set.add(ObfRule.valueOf(name));
                        } catch (IllegalArgumentException ignored) {}
                    }
                }
            }
        }
        return set;
    }
}