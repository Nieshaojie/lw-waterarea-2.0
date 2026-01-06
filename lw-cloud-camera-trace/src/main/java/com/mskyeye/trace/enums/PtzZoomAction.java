package com.mskyeye.trace.enums;

/**
 * 镜头变倍（Zoom）控制动作枚举
 *
 * 设计说明：
 * 1. 每一个枚举值 = 一个“可直接下发给设备的动作”
 * 2. ZOOM_STOP 是一等公民，而不是 speed=0 的隐式行为
 * 3. 是否需要速度，由枚举本身声明
 */
public enum PtzZoomAction {

    /**
     * 镜头拉近（放大）
     */
    ZOOM_IN(9, true),

    /**
     * 镜头拉远（缩小）
     */
    ZOOM_OUT(10, true),

    /**
     * 镜头停止变倍
     */
    ZOOM_STOP(29, false);

    /**
     * 设备定义的动作 ID
     */
    private final int actionId;

    /**
     * 当前动作是否需要速度参数
     */
    private final boolean needSpeed;

    PtzZoomAction(int actionId, boolean needSpeed) {
        this.actionId = actionId;
        this.needSpeed = needSpeed;
    }

    public int getActionId() {
        return actionId;
    }

    public boolean needSpeed() {
        return needSpeed;
    }

    public static PtzZoomAction from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("direction is null");
        }
        return PtzZoomAction.valueOf(value.toUpperCase());
    }
}
