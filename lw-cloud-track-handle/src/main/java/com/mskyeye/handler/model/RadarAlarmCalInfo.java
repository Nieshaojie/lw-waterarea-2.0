package com.mskyeye.handler.model;

import lombok.Data;

import java.util.List;

/**
 * @ClassName:RadarAlarmCalInfo
 * @Description:雷达预警计算信息
 * @Author:R.Gong
 * @Date:2023/7/19 14:50
 * @Version:1.0
 **/
@Data
public class RadarAlarmCalInfo {


    /**部门ID */
    private Integer deptId;

    /**雷达编号 */
    private Integer code;

    /**区域ID */
    private Integer areaId;

    /**区域点信息 */
    private String points;

    /**区域点经纬度信息 */
    private List<LonLatInfo> pointsLonLat;

    /**计算方法 */
    private Integer calMethod;

    /**计算属性 */
    private Integer calPro;

    /**类型阈值 */
    private Integer typeValue;

    /**类型名 */
    private String typeName;

    /** 开始时间 */
    private String startTime;

    /** 结束时间 */
    private String endTime;

    /** 是否在时间段内预警,1:时间段内预警,0:时间段外预警 */
    private Integer isInterAlarm;
}
