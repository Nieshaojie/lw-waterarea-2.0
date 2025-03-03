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

    private double shipLon;                             /*经度*/

    private double shipLat;                             /*纬度*/

    private Integer refNum = 0;                                /*更新的连续帧数,用于判断是否为稳定目标*/

    private Long refreshTime;                           /*更新时间*/
}
