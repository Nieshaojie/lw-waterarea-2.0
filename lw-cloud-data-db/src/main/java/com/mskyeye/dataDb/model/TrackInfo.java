package com.mskyeye.dataDb.model;

import lombok.Data;

/**
 * TrackInfo 存储的航迹信息
 */
@Data
public class TrackInfo {

    private Long id;//目标批号

    private Integer stationId;//探测站ID

    private Integer source;//数据源类型：0：雷达目标；1：AIS目标；2：雷达和AIS融合目标

    private Long mmsi;

    private Double lat;

    private Double lon;

    private Float course;//航向，单位：度

    private Float speed;//航速，单位：100*m/s  （*1.944/100转换成节）

    private Float head;//船首向

    private Integer status;//0为删除该目标,1为新增、更新目标

    private String alarm;//预警类型名称

    private String name;//船名

    private String shipType;//船舶类型

    private String country;
}
