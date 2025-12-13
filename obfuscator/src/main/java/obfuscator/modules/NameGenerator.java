package obfuscator.modules;

import java.security.SecureRandom;

public final class NameGenerator {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String LAT_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String CHINESE_POOL = "...";

    private NameGenerator() {
    }

    public static String generateLatName() {
        int length = 16 + secureRandom.nextInt(16);
        StringBuilder name = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            name.append(LAT_CHARS.charAt(secureRandom.nextInt(LAT_CHARS.length())));
        }
        return name.toString();
    }

    public static String generateChineseName() {
        int length = 16 + secureRandom.nextInt(16);
        return generateRandomChinese(length);
    }

    public static String generateRandomChinese(int len) {
        if (len <= 0) return "";
        StringBuilder sb = new StringBuilder(len);
        int poolLen = CHINESE_POOL.length();
        for (int i = 0; i < len; i++) {
            sb.append(CHINESE_POOL.charAt(secureRandom.nextInt(poolLen)));
        }
        return sb.toString();
    }
}