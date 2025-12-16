package obfuscator;

import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import obfuscator.modules.RemapperModule;

public class Obfuscator {
    private Obfuscator() {
    }

    public static void main(String[] args) {
        Obfuscator obfuscator = new Obfuscator();
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        obfuscator.run(args[0]);
    }

    public void run(String path) {
        RemapperModule.runRemap(new File(path));
    }
}