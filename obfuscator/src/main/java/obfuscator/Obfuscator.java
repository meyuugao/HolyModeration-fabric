package obfuscator;

import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;

public class Obfuscator {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static File inputJar, outputJar;

    private Obfuscator() {
    }

    public static void main(String[] args) {
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        run(args[0]);
    }

    public static void run(String path) {
        inputJar = new File(path);
        log("Обфускатор запущен. Полученная jar: " + inputJar.getName());

        outputJar = new File(inputJar.getParentFile(), inputJar.getName().replace(".jar", "") + "-obfuscated.jar");
    }

    private static void log(String message) {
        System.out.println("[OBFUSCATOOR] " + message);
    }
}