package obfuscator;

import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import obfuscator.modules.RemapperModule;

public class HolyObfuscator {
    private HolyObfuscator() {
    }

    public static void main(String[] args) {
        HolyObfuscator holyObfuscator = new HolyObfuscator();
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        holyObfuscator.run(args[0]);
    }

    public void run(String path) {
        RemapperModule.run(new File(path));
    }
}