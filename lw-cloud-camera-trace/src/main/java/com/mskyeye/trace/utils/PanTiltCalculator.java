package com.mskyeye.trace.utils;

import static java.lang.Math.*;

public class PanTiltCalculator {

    public static class LatLonAlt {
        public final double lat; // 纬度（degrees）
        public final double lon; // 经度（degrees）
        public final double alt; // 高度（meters）

        public LatLonAlt(double lat, double lon, double alt) {
            this.lat = lat;
            this.lon = lon;
            this.alt = alt;
        }
    }

    public static class PanTiltView {
        public final double pan;   // 方位角（0~360）
        public final double tilt;  // 俯仰角（-90~90）
        public final double view;  // 视场角（默认15）

        public PanTiltView(double pan, double tilt, double view) {
            this.pan = pan;
            this.tilt = tilt;
            this.view = view;
        }

        @Override
        public String toString() {
            return String.format("pan: %.2f°, tilt: %.2f°, view: %.2f°", pan, tilt, view);
        }
    }

    // 根据两点位置计算结果
    // 新增支持目标宽度的版本
    public static PanTiltView calculate(LatLonAlt from, LatLonAlt to, double targetWidth) {
        double pan = calculateAzimuth(from, to);
        double tilt = calculateTilt(from, to);
        double distance = calculateDistance(from, to);
        double view = calculateViewAngleByWidth(distance, targetWidth);
        return new PanTiltView(pan, tilt, view);
    }

    // 计算方位角
    private static double calculateAzimuth(LatLonAlt from, LatLonAlt to) {
        double lat1 = toRadians(from.lat);
        double lat2 = toRadians(to.lat);
        double dLon = toRadians(to.lon - from.lon);

        double y = sin(dLon) * cos(lat2);
        double x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(dLon);
        double azimuth = toDegrees(atan2(y, x));
        return (azimuth + 360) % 360;
    }

    // 计算俯仰角
    private static double calculateTilt(LatLonAlt from, LatLonAlt to) {
        double earthRadius = 6371000; // 地球半径
        double lat1 = toRadians(from.lat);
        double lat2 = toRadians(to.lat);
        double dLat = lat2 - lat1;
        double dLon = toRadians(to.lon - from.lon);

        double a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2);
        double c = 2 * atan2(sqrt(a), sqrt(1 - a));
        double horizontalDistance = earthRadius * c;

        double heightDiff = to.alt - from.alt;
        return toDegrees(atan2(heightDiff, horizontalDistance));
    }

    /**
     * 根据目标距离和目标宽度，计算水平视场角（单位：度）
     *
     * @param distance 目标与设备的距离（单位：米）
     * @param width    目标希望在画面中覆盖的宽度（单位：米）
     * @return         视场角度（单位：度），若参数无效返回 -1
     */
    public static double calculateViewAngleByWidth(double distance, double width) {
        if (distance <= 0 || width <= 0) {
            return -1;
        }
        double angleRad = 2 * atan((width / 0.6) / distance);
        return toDegrees(angleRad);
    }

    /**
     * 计算光电设备与目标之间的三维空间距离（单位：米）
     *
     * @param from 光电设备位置
     * @param to   目标位置
     * @return     空间直线距离（米）
     */
    public static double calculateDistance(LatLonAlt from, LatLonAlt to) {
        double earthRadius = 6371000; // 平均地球半径（米）

        double lat1 = toRadians(from.lat);
        double lat2 = toRadians(to.lat);
        double dLat = lat2 - lat1;
        double dLon = toRadians(to.lon - from.lon);

        // 水平距离（球面弧长）
        double a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) * sin(dLon / 2) * sin(dLon / 2);
        double c = 2 * atan2(sqrt(a), sqrt(1 - a));
        double horizontalDistance = earthRadius * c;

        // 垂直高度差
        double heightDiff = to.alt - from.alt;

        // 返回斜线距离
        return sqrt(horizontalDistance * horizontalDistance + heightDiff * heightDiff);
    }

}
