package com.mskyeye.trace.utils;

import com.mskyeye.trace.model.FovPoint;

import java.util.List;

/**
 * 视场角线性插值工具
 */
public class FovInterpolator {

    public static double interpolate(double distance, List<FovPoint> table) {
        if (table == null || table.isEmpty()) {
            throw new IllegalArgumentException("FOV table is empty");
        }

        // 小于最小值
        if (distance <= table.get(0).distance) {
            return table.get(0).fov;
        }

        // 大于最大值
        if (distance >= table.get(table.size() - 1).distance) {
            return table.get(table.size() - 1).fov;
        }

        for (int i = 0; i < table.size() - 1; i++) {
            FovPoint p1 = table.get(i);
            FovPoint p2 = table.get(i + 1);

            if (distance >= p1.distance && distance <= p2.distance) {
                return p1.fov +
                        (distance - p1.distance) *
                        (p2.fov - p1.fov) /
                        (p2.distance - p1.distance);
            }
        }

        // 理论兜底
        return table.get(table.size() - 1).fov;
    }
}
