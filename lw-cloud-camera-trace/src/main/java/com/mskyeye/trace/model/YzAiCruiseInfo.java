package com.mskyeye.trace.model;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * AI巡航信息对象 yz_ai_cruise_info
 * 
 * @author ruoyi
 * @date 2023-09-08
 */
@Data
public class YzAiCruiseInfo
{
    private static final long serialVersionUID = 1L;

    /** 巡航id */
    private Long id;

    /** 名称 */
    private String name;

    /** 经度 */
    private BigDecimal lon;

    /** 纬度 */
    private BigDecimal lat;

    /** 相机id */
    private Long cameraId;

    /** 点位ID */
    private String points;

    private List<YzAiPointInfo> pointsInfoList;

    private Integer status = 1;//巡航状态:0 巡航 1 非巡航

    private Integer curPointIndex = 0;//当前巡航点位索引

    private Integer clock = 0;//计时器,,用于巡航
}
