package obfuscator.modules;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

public class LoggerModule {
    public static void writeMappings(File inputJar, File outputJar, Map<String, String> classMapping, Map<String, String> methodMapping, Map<String, String> fieldMapping, Map<String, String> paramMapping, Set<String> dontObfClasses, Set<String> dontObfMethods, Set<String> dontObfFields) {
        try {
            File mappingFile = new File(inputJar.getParentFile(), "obfuscation-mapping.txt");
            PrintWriter writer = new PrintWriter(new FileWriter(mappingFile));
            writer.println("=== МАППИНГ КЛАССОВ ===");
            for (Map.Entry<String, String> e : classMapping.entrySet()) {
                writer.println(e.getKey() + " -> " + e.getValue());
            }

            writer.println("\n=== МАППИНГ МЕТОДОВ ===");
            for (Map.Entry<String, String> e : methodMapping.entrySet()) {
                writer.println(e.getKey() + " -> " + e.getValue());
            }

            writer.println("\n=== МАППИНГ ПОЛЕЙ ===");
            for (Map.Entry<String, String> e : fieldMapping.entrySet()) {
                writer.println(e.getKey() + " -> " + e.getValue());
            }

            writer.println("\n=== МАППИНГ ПАРАМЕТРОВ ===");
            for (Map.Entry<String, String> e : paramMapping.entrySet()) {
                writer.println(e.getKey() + " -> " + e.getValue());
            }

            writer.println("\n=== ИСКЛЮЧЕНИЯ @DontObf ===");
            writer.println("Классы: " + String.join(", ", dontObfClasses));
            writer.println("Методы: " + String.join(", ", dontObfMethods));
            writer.println("Поля: " + String.join(", ", dontObfFields));

            log("=== ОБФУСКАЦИЯ ЗАВЕРШЕНА ===");
            log("Обфусцированный JAR: " + outputJar.getName());
            log("Маппинг сохранен в: " + mappingFile.getName());
            log("Обработано классов: " + classMapping.size());
            log("Обработано методов: " + methodMapping.size());
            log("Обработано полей: " + fieldMapping.size());
            writer.close();
        } catch (Exception e) {
            log("ОШИБКА ПРИ ЗАПИСИ МАППИНГОВ: " + e);
        }
    }

    public static void log(String message) {
        System.out.println("[HolyObfuscator] " + message);
    }
}