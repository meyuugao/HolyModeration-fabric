package me.yuugao.holymoderation.client.obfuscation;

import java.util.Base64;
import java.util.Random;

public class StringObfuscator {
    private static final Random random = new Random();
    
    public static String encrypt(String input) {
        if (input == null || input.isEmpty()) return input;
        
        byte[] bytes = input.getBytes();
        byte[] key = generateKey(bytes.length);
        byte[] encrypted = new byte[bytes.length];
        
        for (int i = 0; i < bytes.length; i++) {
            encrypted[i] = (byte) (bytes[i] ^ key[i]);
        }
        
        return Base64.getEncoder().encodeToString(encrypted);
    }
    
    public static String decrypt(String encrypted) {
        if (encrypted == null || encrypted.isEmpty()) return encrypted;
        
        try {
            byte[] decoded = Base64.getDecoder().decode(encrypted);
            byte[] key = generateKey(decoded.length);
            byte[] decrypted = new byte[decoded.length];
            
            for (int i = 0; i < decoded.length; i++) {
                decrypted[i] = (byte) (decoded[i] ^ key[i]);
            }
            
            return new String(decrypted);
        } catch (Exception e) {
            return encrypted;
        }
    }
    
    private static byte[] generateKey(int length) {
        byte[] key = new byte[length];
        long seed = 0x5A5A5A5A5A5A5A5AL;
        
        for (int i = 0; i < length; i++) {
            seed = (seed * 1103515245L + 12345L) & 0x7FFFFFFFFFFFFFFFL;
            key[i] = (byte) ((seed >> (i % 8)) & 0xFF);
        }
        
        return key;
    }
}