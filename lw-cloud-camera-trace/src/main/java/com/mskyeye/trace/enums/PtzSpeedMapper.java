package com.mskyeye.trace.enums;

/**
 * 云台速度映射器
 *
 * 职责：
 * - 将“前端速度等级”转换为“设备真实速度”
 * - 隔离设备差异，避免污染业务代码
 */
public final class PtzSpeedMapper {

    private static final int DEVICE_MIN = 1;
    private static final int DEVICE_MAX = 63;

    private PtzSpeedMapper() {}

    /**
     * 前端速度等级 → 设备速度
     *
     * @param level 前端速度等级（0-100）
     */
    public static int toDeviceSpeed(int level) {
        if (level <= 0) {
            return DEVICE_MIN;
        }
        if (level >= 100) {
            return DEVICE_MAX;
        }
        return DEVICE_MIN + (level * (DEVICE_MAX - DEVICE_MIN) / 100);
    }
}
