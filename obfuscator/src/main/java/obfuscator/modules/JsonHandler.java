package obfuscator.modules;

import static obfuscator.modules.LoggerModule.log;


import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.regex.Pattern;

public class JsonHandler {
    public static byte[] handleJson(JarEntry entry, byte[] entryBytes, Map<String, String> classMapping) {
        try {
            String updatedContent = new String(entryBytes, StandardCharsets.UTF_8);

            String mixinPrefix = "me/yuugao/holymoderation/client/mixin";

            List<Map.Entry<String, String>> mappingEntries = new ArrayList<>(classMapping.entrySet());
            mappingEntries.sort((a, b) -> Integer.compare(b.getKey().length(), a.getKey().length()));

            String newMixinDirSimple = null;
            if (classMapping.containsKey(mixinPrefix)) {
                String mappedMixinDir = classMapping.get(mixinPrefix);
                if (mappedMixinDir != null && mappedMixinDir.contains("/")) {
                    newMixinDirSimple = mappedMixinDir.substring(mappedMixinDir.lastIndexOf('/') + 1);
                } else {
                    newMixinDirSimple = mappedMixinDir;
                }
            }

            for (Map.Entry<String, String> mapEntry : mappingEntries) {
                String oldClass = mapEntry.getKey();
                String newClass = mapEntry.getValue();

                if (entry.getName().equals("HolyModeration-refmap.json")) {
                    updatedContent = updatedContent.replaceAll(Pattern.quote(oldClass), newClass);
                } else {
                    String oldClassName = oldClass.replace("/", ".");
                    String newClassName = newClass.replace("/", ".");
                    updatedContent = updatedContent.replaceAll("\\b" + Pattern.quote(oldClassName) + "\\b", newClassName);
                }

                if (entry.getName().equals("holymoderation.mixins.json") && oldClass.startsWith(mixinPrefix + "/")) {
                    String oldSimple = oldClass.substring(oldClass.lastIndexOf('/') + 1);
                    String newSimple = newClass.substring(newClass.lastIndexOf('/') + 1);
                    updatedContent = updatedContent.replace("\"" + oldSimple + "\"", "\"" + newSimple + "\"");
                }
            }

            if (entry.getName().equals("holymoderation.mixins.json") && newMixinDirSimple != null) {
                updatedContent = updatedContent.replace("\"me.yuugao.holymoderation.client.mixin\"", "\"me.yuugao.holymoderation.client." + newMixinDirSimple + "\"");
            }

            log("ОБНОВЛЕН JSON: " + entry.getName());
            return updatedContent.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log("ОШИБКА при обновлении " + entry.getName() + ": " + e.getMessage());
            return entryBytes;
        }
    }
}