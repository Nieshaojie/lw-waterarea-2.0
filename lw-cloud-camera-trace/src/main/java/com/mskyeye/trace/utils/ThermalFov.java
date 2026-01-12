package com.mskyeye.trace.utils;

import com.mskyeye.trace.model.FovPoint;

import java.util.Arrays;
import java.util.List;

/**
 * 和普非制冷热像视场角获取（完整标定表）
 */
public class ThermalFov {

    private static final List<FovPoint> TABLE = Arrays.asList(
            new FovPoint(100, 17.2),
            new FovPoint(150, 17.2),
            new FovPoint(200, 12.4),
            new FovPoint(250, 12.4),
            new FovPoint(300, 5.6),
            new FovPoint(350, 5.6),
            new FovPoint(400, 5.06),
            new FovPoint(450, 5.06),
            new FovPoint(500, 5.06),

            new FovPoint(550, 4.06),
            new FovPoint(600, 4.06),
            new FovPoint(650, 4.06),

            new FovPoint(700, 3.01),
            new FovPoint(750, 3.01),

            new FovPoint(800, 2.67),
            new FovPoint(850, 2.67),
            new FovPoint(900, 2.67),
            new FovPoint(950, 2.67),

            new FovPoint(1000, 2.4),

            new FovPoint(1050, 2.25),
            new FovPoint(1100, 2.15),
            new FovPoint(1150, 2.15),

            new FovPoint(1200, 1.91),
            new FovPoint(1250, 1.91),
            new FovPoint(1300, 1.91),
            new FovPoint(1350, 1.91),
            new FovPoint(1400, 1.91),
            new FovPoint(1450, 1.91),
            new FovPoint(1500, 1.91)
    );

    public static double getFov(double distance) {
        return FovInterpolator.interpolate(distance, TABLE);
    }
}
