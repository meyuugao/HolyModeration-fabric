package obfuscator.modules;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Map;

import obfuscator.ObfContext;

public class LoggerModule {
    public static void writeMappings(File inputJar, File outputJar, ObfContext ctx) {
        try {
            File mappingFile = new File(inputJar.getParentFile(), "obfuscation-mapping.txt");
            PrintWriter writer = new PrintWriter(new FileWriter(mappingFile));
            writer.println("=== МАППИНГ КЛАССОВ ===");
            for (Map.Entry<String, String> e : ctx.classMap.entrySet()) {
                writer.println("%s -> %s".formatted(e.getKey(), e.getValue()));
            }

            writer.println("\n=== МАППИНГ МЕТОДОВ ===");
            for (Map.Entry<String, String> e : ctx.methodMap.entrySet()) {
                writer.println("%s -> %s".formatted(e.getKey(), e.getValue()));
            }

            writer.println("\n=== МАППИНГ ПОЛЕЙ ===");
            for (Map.Entry<String, String> e : ctx.fieldMap.entrySet()) {
                writer.println("%s -> %s".formatted(e.getKey(), e.getValue()));
            }

            writer.println("\n=== МАППИНГ ПАРАМЕТРОВ ===");
            for (Map.Entry<String, String> e : ctx.paramMap.entrySet()) {
                writer.println("%s -> %s".formatted(e.getKey(), e.getValue()));
            }

            writer.println("\n=== МАППИНГ РЕСУРСОВ ===");
            for (Map.Entry<String, String> e : ctx.assetsMap.entrySet()) {
                writer.println("%s -> %s".formatted(e.getKey(), e.getValue()));
            }

            writer.println("\n=== ИСКЛЮЧЕНИЯ @DontObf ===");
            writer.println("Классы: %s".formatted(String.join(", ", ctx.dontObfClasses)));
            writer.println("Методы: %s".formatted(String.join(", ", ctx.dontObfMethods)));
            writer.println("Поля: %s".formatted(String.join(", ", ctx.dontObfFields)));

            log("=== ОБФУСКАЦИЯ ЗАВЕРШЕНА ===");
            log("Обфусцированный JAR: %s".formatted(outputJar.getName()));
            log("Маппинг сохранен в: %s".formatted(mappingFile.getName()));
            log("Обработано классов: %s".formatted(ctx.classMap.size()));
            log("Обработано методов: %s".formatted(ctx.methodMap.size()));
            log("Обработано полей: %s".formatted(ctx.fieldMap.size()));
            log("Обработано параметров: %s".formatted(ctx.paramMap.size()));
            log("Обработано ресурсов: %s".formatted(ctx.assetsMap.size()));
            writer.close();
        } catch (Exception e) {
            log("Ошибка при записи маппингов: %s".formatted(e));
        }
    }

    public static void log(String message) {
        System.out.printf("[HolyObfuscator] %s%n", message);
    }
}