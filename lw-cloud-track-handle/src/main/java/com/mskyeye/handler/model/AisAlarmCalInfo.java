package com.mskyeye.handler.model;

import lombok.Data;

/**
 * @ClassName:AisAlarmCalInfo
 * @Description:AIS告警计算信息
 * @Author:R.Gong
 * @Date:2023/7/19 16:45
 * @Version:1.0
 **/
@Data
public class AisAlarmCalInfo {

    /** mmsi */
    private Integer mmsi;

    /** 报警类型(0:黑名单 1:白名单) */
    private Integer alarmType;

}
