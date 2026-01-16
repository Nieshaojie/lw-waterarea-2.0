package com.mskyeye.trace.calib;

/**
 * 基于单一方向标定的 T 值自动补偿器
 *
 * 物理假设：
 * - 主要安装误差来自整体俯仰偏差
 * - 横滚误差忽略
 */
public class TAngleCompensator {

    /** 标定方位角（度） */
    private final double calibAzimuth;

    /** 该方位下测得的 T 值误差（度） */
    private final double calibDeltaT;

    public TAngleCompensator(double calibAzimuth, double calibDeltaT) {
        this.calibAzimuth = calibAzimuth;
        this.calibDeltaT = calibDeltaT;
    }

    /**
     * 根据当前方位角计算 T 值补偿量
     *
     * @param azimuth 当前光电方位角（度）
     * @return 需要补偿的 T 值（度）
     */
    public double computeDeltaT(double azimuth) {
        double rad = Math.toRadians(azimuth - calibAzimuth);
        return calibDeltaT * Math.cos(rad);
    }

    /**
     * 修正原始 T 值
     *
     * @param rawT    原始 T 值（度）
     * @param azimuth 当前光电方位角（度）
     * @return 修正后的 T 值
     */
    public double correct(double rawT, double azimuth) {
        return rawT - computeDeltaT(azimuth);
    }
}
