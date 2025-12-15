package obfuscator.modules;

import java.security.SecureRandom;

public final class NameGenerator {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String LAT_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String CHINESE_POOL = "的一是在不了有和人這中大為上個國我以要他時來用們生到作地於出就分對成會可主發年動同工也能下過子說產種面而方後多定行學法所民得經十三之進等部度家電力裡如水化高自二理起小物實加量都兩體制機當使點從業本去把性好應開它合還因由其些然前外天政四日那社義事平形相全表間樣與關各重新線內數正心反你明看原又麼利比或但質氣第向道命此變條只沒結解問意建月公無系軍很情者最立代想已通並提直題黨程展五果料象員革位入常文總次品式活設及管特件長求老頭基資邊流路級少圖山統接知較將組見計別她手角期根論運農指幾九區強放決西被做必戰先回則任取據處隊南給色光門即保治北造百規熱領七海口東導器壓志世金增張於細平";

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