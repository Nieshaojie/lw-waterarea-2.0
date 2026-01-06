package com.mskyeye.trace.enums;

/**
 * 云台方向枚举
 *
 * 设计目标：
 * 1. 一个枚举完整描述一个“可执行动作”
 * 2. STOP 是一等公民，而不是特殊 if
 * 3. 每个方向清楚声明自己依赖的速度字段
 */
public enum PtzDirection {

    // ====== 上下左右 ======
    UP(0, "ptzyspeed"),
    DOWN(1, "ptzyspeed"),
    LEFT(2, "ptzxspeed"),
    RIGHT(3, "ptzxspeed"),

    // ====== 斜向（组合动作，设备通常支持）======
    LEFT_UP(4, "ptzxspeed", "ptzyspeed"),
    LEFT_DOWN(5, "ptzxspeed", "ptzyspeed"),
    RIGHT_UP(6, "ptzxspeed", "ptzyspeed"),

    RIGHT_DOWN(7, "ptzxspeed", "ptzyspeed"),

    // ====== 停止（一等公民）======
    STOP(8);

    /**
     * 设备定义的动作 ID
     */
    private final int actionId;

    /**
     * 该动作需要使用的速度字段
     * - 单方向：1 个
     * - 斜方向：2 个
     * - STOP：0 个
     */
    private final String[] speedFields;

    PtzDirection(int actionId, String... speedFields) {
        this.actionId = actionId;
        this.speedFields = speedFields;
    }

    public int getActionId() {
        return actionId;
    }

    public String[] getSpeedFields() {
        return speedFields;
    }

    /**
     * 是否需要速度参数
     */
    public boolean needSpeed() {
        return speedFields != null && speedFields.length > 0;
    }
    /*
     * 将前端字符串安全转换为方向枚举
     */
    public static PtzDirection from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("direction is null");
        }
        return PtzDirection.valueOf(value.toUpperCase());
    }
}
