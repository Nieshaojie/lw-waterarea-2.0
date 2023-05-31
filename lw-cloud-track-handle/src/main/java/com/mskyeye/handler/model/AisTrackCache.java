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

    private Integer stationId;                          /*融合雷达目标的探测站ID*/

    private Long radarTarId;                               /*融合雷达目标的雷达批号*/

    private Long targetId;                              /*目标批号*/

    private Integer iMmsi;                              /*MMSI*/

    private Boolean bMergeTar = false;                  /*是否为融合目标*/

    private Integer matchFrames = 0;                    /*融合匹配帧数*/

    private Long refreshTime;                           /*更新时间*/
}
