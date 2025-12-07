package com.example.demo.astrology;

import java.util.HashMap;
import java.util.Map;

/**
 * 主要的天文计算类（简化实现）
 *
 * 功能：
 * - 计算儒略日（Julian Day，基于 Meeus）
 * - 计算太阳黄经（简化日心模型/太阳地心位置）
 * - 计算月亮与其它行星的近似位置（用非常简化的方法，真实精度有限）
 * - 计算上升点（Ascendant）——采用近似局部恒星时（LST）转换为黄经的方法（用于等分宫）
 *
 * 提醒：
 * - 生产级别应替换为 Swiss Ephemeris 或 VSOP87 数据实现以获得高精度。
 * - 本实现仅为教学/演示，注释中会标明公式来源与近似位置。
 *
 * 参考：
 * - Jean Meeus, Astronomical Algorithms（Julian Day 等常用公式）。:contentReference[oaicite:2]{index=2}
 */
public class PlanetCalculator {

    /**
     * 计算结果的容器
     */
    public static class CalcResult {
        // 每个“行星名”对应黄经（0-360）
        public Map<String, Double> planetLongitudes = new HashMap<>();
        // 上升点黄经（0-360）——用于宫位起点
        public Double ascendant;
        // 其它可扩展字段
        public double julianDay;
    }

    /**
     * 对外接口：给出本地日期字符串（YYYY-MM-DD）、时间（HH:MM）以及经度/纬度（度）。
     * 返回 CalcResult
     *
     * 注意：时间按本地时区（用户设备时间）理解，计算时需转换为 UT（简化：按设备时区偏移）
     */
    public static CalcResult calculateFromLocal(String dateYMD, String timeHM, double lon, double lat) throws Exception {
        // 解析输入
        String[] d = dateYMD.split("-");
        int Y = Integer.parseInt(d[0]);
        int M = Integer.parseInt(d[1]);
        int D = Integer.parseInt(d[2]);

        String[] t = timeHM.split(":");
        int hour = Integer.parseInt(t[0]);
        int minute = Integer.parseInt(t[1]);

        // 这里直接假设输入为 UTC 时间会更简单；若为本地时间则需要减去时区偏移。
        // 为了简单，我把时间看作本地时区，然后用 Java 时间 API 在主线程转换为 UTC（此处为简化，直接按输入小时处理）。
        double dayFraction = (hour + (minute / 60.0)) / 24.0;

        // 计算儒略日（JD） - 使用 Meeus 算法（简化实现）
        double jd = julianDay(Y, M, D + dayFraction);

        CalcResult res = new CalcResult();
        res.julianDay = jd;

        // 计算太阳黄经（简化公式）
        double sunLon = sunEclipticLongitude(jd);
        res.planetLongitudes.put("Sun", normalizeDegrees(sunLon));

        // 计算月亮黄经（简化近似）
        double moonLon = moonEclipticLongitude(jd);
        res.planetLongitudes.put("Moon", normalizeDegrees(moonLon));

        // 这里示例：对其他行星我们使用非常粗略的平均运动近似（仅供演示）
        // 真正精确计算应使用 VSOP87 系列或 Swiss Ephemeris。
        res.planetLongitudes.put("Mercury", normalizeDegrees(sunLon + 48.0)); // 占位：真实应独立计算
        res.planetLongitudes.put("Venus", normalizeDegrees(sunLon + 75.0));
        res.planetLongitudes.put("Mars", normalizeDegrees(sunLon + 120.0));
        res.planetLongitudes.put("Jupiter", normalizeDegrees(sunLon + 200.0));
        res.planetLongitudes.put("Saturn", normalizeDegrees(sunLon + 260.0));
        res.planetLongitudes.put("Uranus", normalizeDegrees(sunLon + 300.0));
        res.planetLongitudes.put("Neptune", normalizeDegrees(sunLon + 320.0));
        res.planetLongitudes.put("Pluto", normalizeDegrees(sunLon + 330.0));

        // 计算上升点（Ascendant）——此处使用近似公式：由本地经度、JD 得到恒星时，再转成黄经
        double lst = localSiderealTime(jd, lon);
        // 近似上升点：asc = arctan2( sin(lst)*cos(eps) - tan(lat)*sin(eps), cos(lst) )
        // eps（黄赤交角）使用近似值
        double eps = obliquityEcliptic(jd);
        double lstRad = Math.toRadians(lst);
        double latRad = Math.toRadians(lat);
        double ascRad = Math.atan2(Math.sin(lstRad) * Math.cos(Math.toRadians(eps)) - Math.tan(latRad) * Math.sin(Math.toRadians(eps)),
                Math.cos(lstRad));
        double ascDeg = Math.toDegrees(ascRad);
        ascDeg = normalizeDegrees(ascDeg);
        res.ascendant = ascDeg;

        return res;
    }

    /*******************************
     * 辅助天文函数（注释解释来源）
     *******************************/

    /**
     * 计算儒略日 JD（含小数日） - 基于 Jean Meeus。
     * 参数 D 可带小数（例如日 + 小时/24）
     */
    public static double julianDay(int year, int month, double dayWithFraction) {
        int Y = year;
        int M = month;
        double D = dayWithFraction;

        if (M <= 2) {
            Y -= 1;
            M += 12;
        }
        int A = Y / 100;
        int B = 2 - A + (A / 4);

        double jd = Math.floor(365.25 * (Y + 4716))
                + Math.floor(30.6001 * (M + 1))
                + D + B - 1524.5;
        return jd;
    }

    /**
     * 近似太阳黄经（参考简化算法：通过太阳平近点、黄道周期近似）
     * 说明：这里只是演示用的简化实现，不含大部分摄动项与精细项。
     */
    public static double sunEclipticLongitude(double jd) {
        // 参考：简化太阳位置计算（近似）：
        // 计算自 J2000.0 的世纪数
        double T = (jd - 2451545.0) / 36525.0;

        // 平近点角（Mean anomaly）M (deg)
        double M = 357.52911 + 35999.05029 * T - 0.0001537 * T * T;

        // 平黄经 L0
        double L0 = 280.46646 + 36000.76983 * T + 0.0003032 * T * T;

        // 太阳光行差近似（C）
        double C = (1.914602 - 0.004817 * T - 0.000014 * T * T) * Math.sin(Math.toRadians(M))
                + (0.019993 - 0.000101 * T) * Math.sin(Math.toRadians(2 * M))
                + 0.000289 * Math.sin(Math.toRadians(3 * M));

        double trueLong = L0 + C; // 真黄经近似
        return normalizeDegrees(trueLong);
    }

    /**
     * 近似月亮黄经（非常简化的近似），用于演示
     * 精确月亮位置需要大量周期项（Meeus 中有详细项）
     */
    public static double moonEclipticLongitude(double jd) {
        double T = (jd - 2451545.0) / 36525.0;
        // 平黄经与平均月亮参数（近似）
        double L0 = 218.3164477 + 481267.88123421 * T - 0.0015786 * T*T;
        double M_sun = 357.5291092 + 35999.0502909 * T;
        double M_moon = 134.9633964 + 477198.8675055 * T;
        double D = 297.8501921 + 445267.1114034 * T;

        // 取少数项叠加（示例取一两个项）
        double lon = L0 + 6.289 * Math.sin(Math.toRadians(M_moon))  // 近点引起的项
                + 1.274 * Math.sin(Math.toRadians(2 * D - M_moon))
                + 0.658 * Math.sin(Math.toRadians(2 * D))
                - 0.214 * Math.sin(Math.toRadians(2 * M_moon))
                - 0.11 * Math.sin(Math.toRadians(D)); // 等
        return normalizeDegrees(lon);
    }

    /**
     * 近似黄赤交角（obliquity of the ecliptic）
     */
    public static double obliquityEcliptic(double jd) {
        double T = (jd - 2451545.0) / 36525.0;
        double eps0 = 23.439291 - 0.0130042 * T - 1.64e-7 * T*T + 5.04e-7 * T*T*T;
        return eps0;
    }

    /**
     * 近似地方恒星时（Local Sidereal Time），单位：度（0-360）
     * 说明：此处给出常用近似公式，来源请参考 Meeus（本函数用于计算升交点/上升点等）
     */
    public static double localSiderealTime(double jd, double longitude) {
        // 计算格林威治恒星时 GST（度）
        double T = (jd - 2451545.0) / 36525.0;
        double T0 = 280.46061837 + 360.98564736629 * (jd - 2451545.0)
                + 0.000387933 * T*T - (T*T*T) / 38710000.0;
        double GST = normalizeDegrees(T0);
        // LST = GST + 经度（东经为正）
        double LST = normalizeDegrees(GST + longitude);
        return LST;
    }

    /**
     * 角度标准化到 0-360
     */
    public static double normalizeDegrees(double deg) {
        deg = deg % 360.0;
        if (deg < 0) deg += 360.0;
        return deg;
    }
}

