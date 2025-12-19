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
                writer.println("%s -> %s".formatted(e.getKey(), e.getValue()));
            }

            writer.println("\n=== МАППИНГ МЕТОДОВ ===");
            for (Map.Entry<String, String> e : methodMapping.entrySet()) {
                writer.println("%s -> %s".formatted(e.getKey(), e.getValue()));
            }

            writer.println("\n=== МАППИНГ ПОЛЕЙ ===");
            for (Map.Entry<String, String> e : fieldMapping.entrySet()) {
                writer.println("%s -> %s".formatted(e.getKey(), e.getValue()));
            }

            writer.println("\n=== МАППИНГ ПАРАМЕТРОВ ===");
            for (Map.Entry<String, String> e : paramMapping.entrySet()) {
                writer.println("%s -> %s".formatted(e.getKey(), e.getValue()));
            }

            writer.println("\n=== ИСКЛЮЧЕНИЯ @DontObf ===");
            writer.println("Классы: %s".formatted(String.join(", ", dontObfClasses)));
            writer.println("Методы: %s".formatted(String.join(", ", dontObfMethods)));
            writer.println("Поля: %s".formatted(String.join(", ", dontObfFields)));

            log("=== ОБФУСКАЦИЯ ЗАВЕРШЕНА ===");
            log("Обфусцированный JAR: %s".formatted(outputJar.getName()));
            log("Маппинг сохранен в: %s".formatted(mappingFile.getName()));
            log("Обработано классов: %s".formatted(classMapping.size()));
            log("Обработано методов: %s".formatted(methodMapping.size()));
            log("Обработано полей: %s".formatted(fieldMapping.size()));
            writer.close();
        } catch (Exception e) {
            log("Ошибка при записи маппингов: %s".formatted(e));
        }
    }

    public static void log(String message) {
        System.out.printf("[HolyObfuscator] %s%n", message);
    }
}