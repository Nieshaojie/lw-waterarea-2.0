package com.mskyeye.handler.model;

import lombok.Data;

/**
 * @ClassName:MergeTrackCache
 * @Description:融合航迹缓存
 * @Author:R.Gong
 * @Date:2023/7/13 11:32
 * @Version:1.0
 **/
@Data
public class MergeTrackCache {

    private Integer stationId;                          /*探测站ID*/

    private Long merRadarId;                            /*融合的雷达批号*/

    private Long merAisMmsi;                            /*融合的AIS的MMSI*/

    private Integer matchNum = 1;                       /*融合匹配次数*/

    private Long refreshTime;                           /*更新时间*/

    // 新增：融合目标经纬度，统一存储，供搭靠预警直接读取
    private Double shipLon;
    private Double shipLat;

}
