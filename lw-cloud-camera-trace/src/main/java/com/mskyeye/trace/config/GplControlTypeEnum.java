package com.mskyeye.trace.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GplControlTypeEnum {

    // 场景模式
    SCENE_MODE_NORMAL(0, "常规模式"),
    SCENE_MODE_OPTICAL_DEFOG(1, "光学透雾"),
    SCENE_MODE_ELECTRONIC_DEFOG_LOW(2, "电子透雾-低"),
    SCENE_MODE_ELECTRONIC_DEFOG_MEDIUM(3, "电子透雾-中"),
    SCENE_MODE_ELECTRONIC_DEFOG_HIGH(4, "电子透雾-高"),

    // 辅助控制
    CROSSHAIR_ON(5, "十字丝开"),
    CROSSHAIR_OFF(6, "十字丝关"),

    // 聚焦
    ONE_KEY_FOCUS(7, "一键聚焦"),

    // 热成像模式
    THERMAL_BLACK_HOT(8, "黑热"),
    THERMAL_WHITE_HOT(9, "白热");

    private final int code;
    private final String label;

    /**
     * 根据 code 查找对应枚举
     */
    public static GplControlTypeEnum fromCode(int code) {
        for (GplControlTypeEnum e : values()) {
            if (e.code == code) return e;
        }
        return null;
    }

    /**
     * 是否为场景模式控制指令（0~4）
     */
    public boolean isSceneMode() {
        return code >= 0 && code <= 4;
    }

    /**
     * 是否为热成像控制指令（8/9）
     */
    public boolean isThermalMode() {
        return code == 8 || code == 9;
    }
}

