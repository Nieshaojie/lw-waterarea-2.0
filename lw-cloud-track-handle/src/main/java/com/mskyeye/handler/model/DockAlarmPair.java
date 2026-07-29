package com.mskyeye.handler.model;

import lombok.Data;

/**
 * @ClassName:DockAlarmPair
 * @Description:搭靠预警船舶配对缓存对象
 * @Author:R.Gong
 * @Date:2024
 * @Version:1.0
 **/
@Data
public class DockAlarmPair {

    /**
     * 船舶1唯一ID（存储较小的目标ID，统一配对key顺序）
     */
    private Long shipId1;

    /**
     * 船舶2唯一ID（存储较大的目标ID）
     */
    private Long shipId2;

    /**
     * 船舶1 MMSI，雷达目标可为null
     */
    private Long mmsi1;

    /**
     * 船舶2 MMSI，雷达目标可为null
     */
    private Long mmsi2;

    /**
     * 两船首次距离小于搭靠阈值的时间戳（毫秒）
     */
    private Long firstCloseTime;

    /**
     * 幂等标记：true=已生成搭靠告警，不再重复推送
     */
    private Boolean hasAlarm = false;

    /**
     * 本次检测两船实时距离（单位：米）
     */
    private Double lastDistance;

    /** 最后一次更新时间（毫秒）— 用于判断未告警配对是否过期失效 */
    private Long lastUpdateTime;

    /**
     * 船舶1 纬度
     */
    private Double lat1;

    /**
     * 船舶1 经度
     */
    private Double lon1;

    /**
     * 船舶2 纬度
     */
    private Double lat2;

    /**
     * 船舶2 经度
     */
    private Double lon2;

    /**
     * 目标所属探测站ID，用于分站点隔离，禁止跨站计算搭靠
     */
    private Integer stationId;
}