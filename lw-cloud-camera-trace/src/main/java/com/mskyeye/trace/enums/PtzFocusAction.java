package com.mskyeye.trace.enums;

/**
 * 镜头聚焦（Focus）控制枚举
 *
 * 说明：
 * 1. 聚焦通常是“短时间微调”，但仍然必须有 STOP
 * 2. 设计与 Zoom 完全一致，降低理解成本
 */
public enum PtzFocusAction {

    /**
     * 近焦（焦点向近处移动）
     */
    FOCUS_NEAR(11, true),

    /**
     * 远焦（焦点向远处移动）
     */
    FOCUS_FAR(12, true),

    /**
     * 停止聚焦
     */
    FOCUS_STOP(30, false);

    private final int actionId;
    private final boolean needSpeed;

    PtzFocusAction(int actionId, boolean needSpeed) {
        this.actionId = actionId;
        this.needSpeed = needSpeed;
    }

    public int getActionId() {
        return actionId;
    }

    public boolean needSpeed() {
        return needSpeed;
    }
    public static PtzFocusAction from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("direction is null");
        }
        return PtzFocusAction.valueOf(value.toUpperCase());
    }
}
