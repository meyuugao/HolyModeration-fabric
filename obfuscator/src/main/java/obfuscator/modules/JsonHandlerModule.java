package obfuscator.modules;

import static obfuscator.modules.LoggerModule.log;


import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.regex.Pattern;

import obfuscator.ObfContext;

public class JsonHandlerModule {
    public static byte[] handleJson(JarEntry entry, byte[] entryBytes, ObfContext ctx) {
        try {
            Map<String, String> classMapping = ctx.classMap;

            String updatedContent = new String(entryBytes, StandardCharsets.UTF_8);

            String mixinPrefix = ctx.mixinPrefix;

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

                if (entry.getName().equals(ctx.refmapFile)) {
                    updatedContent = updatedContent.replaceAll(Pattern.quote(oldClass), newClass);
                } else {
                    String oldClassName = oldClass.replace("/", ".");
                    String newClassName = newClass.replace("/", ".");
                    updatedContent = updatedContent.replaceAll("\\b%s\\b".formatted(Pattern.quote(oldClassName)), newClassName);
                }

                if (entry.getName().equals(ctx.mixinFile) && oldClass.startsWith("%s/".formatted(mixinPrefix))) {
                    String oldSimple = oldClass.substring(oldClass.lastIndexOf('/') + 1);
                    String newSimple = newClass.substring(newClass.lastIndexOf('/') + 1);
                    updatedContent = updatedContent.replace("\"%s\"".formatted(oldSimple), "\"%s\"".formatted(newSimple));
                }
            }

            if (entry.getName().equals(ctx.mixinFile) && newMixinDirSimple != null) {
                updatedContent = updatedContent.replace("\"%s\"".formatted(mixinPrefix.replace("/", ".")),
                        "\"%s.%s\"".formatted(ctx.mainClientPrefix.replace("/", "."), newMixinDirSimple));
            }

            log("Обновлён json файл %s".formatted(entry.getName()));
            return updatedContent.getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log("Ошибка при обновлении json файла %s: %s".formatted(entry.getName(), e.getMessage()));
            return entryBytes;
        }
    }
}