package obfuscator.modules;

import static obfuscator.modules.LoggerModule.log;


import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import obfuscator.ObfContext;

public class AssetsObfuscatorModule {
    private static final String AES_ALGORITHM = "AES";
    private static SecretKey encryptionKey;

    static {
        try {
            encryptionKey = generateEncryptionKey();
        } catch (Exception e) {
            log("Исключение при генерации ключа обфускации: %s".formatted(e));
        }
    }

    private static SecretKey generateEncryptionKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance(AES_ALGORITHM);
        keyGen.init(256);
        return keyGen.generateKey();
    }

    private static byte[] encryptFile(byte[] fileData) throws Exception {
        Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
        return cipher.doFinal(fileData);
    }

    public static void obfuscateAssets(JarFile originalJar, ObfContext ctx) {
        try {
            Map<String, String> shaderBaseNameMap = new HashMap<>();

            Enumeration<JarEntry> entries = originalJar.entries();
            List<JarEntry> assetEntries = new ArrayList<>();

            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                assetEntries.add(entry);
            }

            for (JarEntry entry : assetEntries) {
                String entryName = entry.getName();
                if (entryName.startsWith("assets/") && !entry.isDirectory() && !getFileExtension(entryName).isEmpty()) {
                    if (entryName.endsWith(".json") && entryName.contains("shader")) {
                        String shaderName = Paths.get(entryName).getFileName().toString().replace(".json", "");
                        String baseName = NameGeneratorModule.generateLatName().toLowerCase();
                        shaderBaseNameMap.put(shaderName, baseName);
                        log("Найден шейдер JSON: %s -> %s".formatted(shaderName, baseName));
                    }
                }
            }

            for (JarEntry entry : assetEntries) {
                String entryName = entry.getName();
                if (entryName.startsWith("assets/") && !entry.isDirectory() && !getFileExtension(entryName).isEmpty()) {
                    InputStream is = originalJar.getInputStream(entry);
                    byte[] fileData = is.readAllBytes();

                    Path entryPath = Paths.get(entryName);
                    String fileName = entryPath.getFileName().toString();
                    String fileNameWithoutExt = removeFileExtension(fileName);
                    String directory = entryName.substring(0, entryName.lastIndexOf('/') + 1);

                    String newFileName = "";

                    if (shaderBaseNameMap.containsKey(fileNameWithoutExt)) {
                        newFileName = "%s%s".formatted(shaderBaseNameMap.get(fileNameWithoutExt), getFileExtension(entryName));
                    } else {
                        boolean found = false;
                        for (Map.Entry<String, String> shaderEntry : shaderBaseNameMap.entrySet()) {
                            String shaderName = shaderEntry.getKey();
                            if (fileNameWithoutExt.equals(shaderName) || fileNameWithoutExt.startsWith("%s_".formatted(shaderName))) {
                                newFileName = "%s%s".formatted(shaderEntry.getValue(), getFileExtension(entryName));
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            newFileName = "%s%s".formatted(NameGeneratorModule.generateLatName().toLowerCase(), getFileExtension(entryName));
                        }
                    }

                    String fullNewPath = "%s%s".formatted(directory, newFileName);

                    if (entryName.endsWith(".json") && entryName.contains("shader")) {
                        String shaderName = entryPath.getFileName().toString().replace(".json", "");
                        String baseName = newFileName.replace(getFileExtension(newFileName), "");
                        ctx.shadersNameMap.put(shaderName, baseName);

                        String oldJsonContent = new String(fileData);
                        String newJsonContent = updateJsonShaderReferences(oldJsonContent, shaderName, baseName);
                        fileData = newJsonContent.getBytes();
                    }

                    ctx.assetsMap.put(entryName, fullNewPath);
                    ctx.assetsBytes.put(fullNewPath, encryptFile(fileData));

                    is.close();
                    log("Ресурс обфусцирован: %s -> %s".formatted(entryName, newFileName));
                }
            }

            for (Map.Entry<String, String> entry : ctx.shadersNameMap.entrySet()) {
                log("  %s -> %s".formatted(entry.getKey(), entry.getValue()));
            }
        } catch (Exception e) {
            log("Исключение при обфускации ассетов: %s".formatted(e));
        }
    }

    private static String updateJsonShaderReferences(String jsonContent, String oldShaderName, String newShaderName) {
        String[] lines = jsonContent.split("\n");
        StringBuilder result = new StringBuilder();

        for (String line : lines) {
            String trimmedLine = line.trim();
            if (trimmedLine.contains("\"vertex\"") || trimmedLine.contains("\"fragment\"")) {
                if (trimmedLine.contains("%s\"".formatted(oldShaderName))) {
                    line = line.replace("%s\"".formatted(oldShaderName), "%s\"".formatted(newShaderName));
                }
            }
            result.append(line).append("\n");
        }

        return result.toString();
    }

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        return dotIndex > 0 ? fileName.substring(dotIndex) : "";
    }

    private static String removeFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        return dotIndex > 0 ? fileName.substring(0, dotIndex) : fileName;
    }

    public static void replaceShaderNamesInClass(ClassNode cn, ObfContext ctx) {
        try {
            if (cn.methods != null) {
                for (MethodNode method : cn.methods) {
                    if (method.instructions != null) {
                        for (AbstractInsnNode instruction : method.instructions) {
                            if (instruction instanceof LdcInsnNode ldcInsnNode) {
                                if (ldcInsnNode.cst instanceof String s) {
                                    for (Map.Entry<String, String> shaderEntry : ctx.shadersNameMap.entrySet()) {
                                        if (s.equals(shaderEntry.getKey())) {
                                            ldcInsnNode.cst = shaderEntry.getValue();
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log("Исключение при замене имён шейдеров: %s".formatted(e));
        }
    }
}