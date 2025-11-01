package com.mskyeye.handler.model;

import lombok.Data;

/**
 * @ClassName:AisTrackCache
 * @Description:AIS航迹缓存
 * @Author:R.Gong
 * @Date:2023/1/4 16:33
 * @Version:1.0
 **/
@Data
public class AisTrackCache {

    private Integer stationId;                          /*探测站ID*/

    private Long targetId;                              /*目标批号*/

    private Long iMmsi;                              /*MMSI*/

    private double shipLon;                             /*经度*/

    private double shipLat;                             /*纬度*/

    private Long refreshTime;                           /*更新时间*/

    private double alt;                                 /*高度*/

    private String sn;
}
