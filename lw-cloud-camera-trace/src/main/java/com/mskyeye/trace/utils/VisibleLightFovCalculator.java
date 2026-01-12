package com.mskyeye.trace.utils;

import com.mskyeye.trace.model.FovPoint;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * 和普可见光视场角获取（完整标定表）
 */
public class VisibleLightFovCalculator {

    private static final List<FovPoint> TABLE = new ArrayList<>();

    static {
        // 100 - 1000
        TABLE.add(new FovPoint(100, 20));
        TABLE.add(new FovPoint(150, 17));
        TABLE.add(new FovPoint(200, 12));
        TABLE.add(new FovPoint(250, 7.2));
        TABLE.add(new FovPoint(300, 6));
        TABLE.add(new FovPoint(350, 5));
        TABLE.add(new FovPoint(400, 4.7));
        TABLE.add(new FovPoint(450, 4.7));
        TABLE.add(new FovPoint(500, 3.5));
        TABLE.add(new FovPoint(550, 3.5));
        TABLE.add(new FovPoint(600, 3.5));
        TABLE.add(new FovPoint(650, 3));
        TABLE.add(new FovPoint(700, 2.8));
        TABLE.add(new FovPoint(750, 2.6));
        TABLE.add(new FovPoint(800, 2.5));
        TABLE.add(new FovPoint(850, 2.5));
        TABLE.add(new FovPoint(900, 2.1));
        TABLE.add(new FovPoint(950, 2));
        TABLE.add(new FovPoint(1000, 2));

        // 1100 - 2000
        TABLE.add(new FovPoint(1100, 2));
        TABLE.add(new FovPoint(1150, 1.85));
        TABLE.add(new FovPoint(1200, 1.75));
        TABLE.add(new FovPoint(1250, 1.75));
        TABLE.add(new FovPoint(1300, 1.75));
        TABLE.add(new FovPoint(1350, 1.7));
        TABLE.add(new FovPoint(1400, 1.7));
        TABLE.add(new FovPoint(1450, 1.6));
        TABLE.add(new FovPoint(1500, 1.6));
        TABLE.add(new FovPoint(1550, 1.6));
        TABLE.add(new FovPoint(1600, 1.55));
        TABLE.add(new FovPoint(1650, 1.55));
        TABLE.add(new FovPoint(1700, 1.4));
        TABLE.add(new FovPoint(1750, 1.4));
        TABLE.add(new FovPoint(1800, 1.4));
        TABLE.add(new FovPoint(1850, 1.3));
        TABLE.add(new FovPoint(1900, 1.2));
        TABLE.add(new FovPoint(1950, 1.2));
        TABLE.add(new FovPoint(2000, 1.2));

        // 2100 - 3000
        TABLE.add(new FovPoint(2100, 1.1));
        TABLE.add(new FovPoint(2150, 1.1));
        TABLE.add(new FovPoint(2200, 1.1));
        TABLE.add(new FovPoint(2250, 1.1));
        TABLE.add(new FovPoint(2300, 1.1));
        TABLE.add(new FovPoint(2350, 1.1));
        TABLE.add(new FovPoint(2400, 1.0));
        TABLE.add(new FovPoint(2450, 1.0));
        TABLE.add(new FovPoint(2500, 1.0));
        TABLE.add(new FovPoint(2550, 1.0));
        TABLE.add(new FovPoint(2600, 1.0));
        TABLE.add(new FovPoint(2650, 1.0));
        TABLE.add(new FovPoint(2700, 1.0));
        TABLE.add(new FovPoint(2750, 0.9));
        TABLE.add(new FovPoint(2800, 0.9));
        TABLE.add(new FovPoint(2850, 0.85));
        TABLE.add(new FovPoint(2900, 0.85));
        TABLE.add(new FovPoint(2950, 0.85));
        TABLE.add(new FovPoint(3000, 0.85));

        // 3100 - 4000
        TABLE.add(new FovPoint(3100, 0.8));
        TABLE.add(new FovPoint(3150, 0.8));
        TABLE.add(new FovPoint(3200, 0.7));
        TABLE.add(new FovPoint(3250, 0.7));
        TABLE.add(new FovPoint(3300, 0.7));
        TABLE.add(new FovPoint(3350, 0.7));
        TABLE.add(new FovPoint(3400, 0.65));
        TABLE.add(new FovPoint(3450, 0.6));
        TABLE.add(new FovPoint(3500, 0.6));
        TABLE.add(new FovPoint(3550, 0.58));
        TABLE.add(new FovPoint(3600, 0.58));
        TABLE.add(new FovPoint(3650, 0.58));
        TABLE.add(new FovPoint(3700, 0.58));
        TABLE.add(new FovPoint(3750, 0.58));
        TABLE.add(new FovPoint(3800, 0.58));
        TABLE.add(new FovPoint(3850, 0.58));
        TABLE.add(new FovPoint(3900, 0.58));
        TABLE.add(new FovPoint(3950, 0.58));
        TABLE.add(new FovPoint(4000, 0.58));

        TABLE.sort(Comparator.comparingDouble(p -> p.distance));
    }

    public static double getFov(double distance) {
        return FovInterpolator.interpolate(distance, TABLE);
    }
}
