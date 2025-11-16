package com.example.demo.astrology;

import java.util.Map;

/**
 * 简单的解释器：根据行星所在星座/宫位生成文本解释（非常基础）
 *
 * 说明：
 * - 真正的占星解读需要结合行星、宫位、星座、相位（角度关系）等复杂逻辑。
 * - 这里我们给出可扩展的模板：把每个行星的星座与其象征意义拼成句子，作为示例。
 */
public class AstrologyInterpreter {

    public static String interpret(PlanetCalculator.CalcResult result, String name) {
        StringBuilder sb = new StringBuilder();
        if (name != null && !name.isEmpty()) {
            sb.append(name).append(" 的命盘分析：\n\n");
        } else {
            sb.append("命盘分析：\n\n");
        }

        // 输出基本信息
        sb.append(String.format("儒略日 (JD): %.5f\n\n", result.julianDay));

        // 列出每个行星与其星座
        for (Map.Entry<String, Double> e : result.planetLongitudes.entrySet()) {
            String planet = e.getKey();
            double lon = e.getValue();
            int zIdx = ZodiacUtils.zodiacIndex(lon);
            String sign = ZodiacUtils.NAMES[zIdx];
            sb.append(String.format("%s：黄经 %.2f°，位于 %s\n", planet, lon, sign));
            // 生成简单解释
            sb.append(planetMeaning(planet, sign));
            sb.append("\n");
        }

        // 上升点说明
        sb.append(String.format("\n上升点 (Ascendant) 近似黄经: %.2f°，星座: %s\n",
                result.ascendant, ZodiacUtils.NAMES[ZodiacUtils.zodiacIndex(result.ascendant)]));

        sb.append("\n（注）本计算使用简化天文算法，若需要更高精度请集成 Swiss Ephemeris 或 VSOP 算法。");

        return sb.toString();
    }

    private static String planetMeaning(String planet, String sign) {
        // 极为简化的模板解释，真实占星应基于更复杂规则。
        switch (planet) {
            case "Sun":
                return "太阳代表核心自我、意志与生命力。位于" + sign + "的人通常表现出相关星座的基本特质。";
            case "Moon":
                return "月亮代表情感、潜意识与安全感。位于" + sign + "会影响情绪表达方式。";
            case "Mercury":
                return "水星代表沟通与思维方式。位于" + sign + "的人思考与表达受该星座影响。";
            case "Venus":
                return "金星代表爱情、价值与审美。位于" + sign + "的人在关系与审美上会展现该星座风格。";
            case "Mars":
                return "火星代表行动力与欲望，位于" + sign + "影响个人动力与竞争方式。";
            default:
                return planet + " 在 " + sign + "，该行星的象征意义会以该星座特质展现。";
        }
    }
}

