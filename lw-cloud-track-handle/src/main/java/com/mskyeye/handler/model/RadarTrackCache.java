package com.mskyeye.handler.model;

import lombok.Data;

/**
 * @ClassName:RadarTrackCache
 * @Description:雷达航迹缓存
 * @Author:R.Gong
 * @Date:2023/1/4 15:57
 * @Version:1.0
 **/
@Data
public class RadarTrackCache {

    private Integer stationId;                          /*探测站ID*/

    private Long targetId;                              /*目标批号*/

    private Integer iMmsi;                              /*MMSI*/

    private String shipName;                            /*船名*/

    private double shipLon;                             /*经度*/

    private double shipLat;                             /*纬度*/

    private Boolean bMergeTar = false;                  /*是否为融合目标*/

    private Long refreshTime;                          /*更新时间*/
}
