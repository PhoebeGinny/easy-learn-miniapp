package com.example.demo.astrology;
/**
 * 星座工具类：提供星座名、符号与根据黄经得到星座索引的函数
 */
public class ZodiacUtils {

    // 12星座短符号（示例）
    public static final String[] SYMBOLS = {
            "Ar", "Ta", "Ge", "Cnc", "Le", "Vi", "Li", "Sc", "Sg", "Cp", "Aq", "Pi"
    };

    public static final String[] NAMES = {
            "白羊 (Aries)","金牛 (Taurus)","双子 (Gemini)","巨蟹 (Cancer)","狮子 (Leo)","处女 (Virgo)",
            "天秤 (Libra)","天蝎 (Scorpio)","射手 (Sagittarius)","摩羯 (Capricorn)","水瓶 (Aquarius)","双鱼 (Pisces)"
    };

    /**
     * 根据黄经（0-360）返回星座索引 0-11
     */
    public static int zodiacIndex(double longitude) {
        int idx = (int) (Math.floor(longitude / 30.0)) % 12;
        if (idx < 0) idx += 12;
        return idx;
    }
}

