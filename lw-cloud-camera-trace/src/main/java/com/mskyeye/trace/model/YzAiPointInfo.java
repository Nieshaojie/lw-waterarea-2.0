package com.mskyeye.trace.model;

import lombok.Data;

import java.math.BigDecimal;

/**
 * AI点位信息对象 yz_ai_point_info
 * 
 * @author ruoyi
 * @date 2023-09-08
 */
@Data
public class YzAiPointInfo
{
    private static final long serialVersionUID = 1L;

    /** 点位id */
    private Long id;

    /** 名称 */
    private String name;

    /** 经度 */
    private BigDecimal lon;

    /** 纬度 */
    private BigDecimal lat;

    /** p值 */
    private Double pVal;

    /** t值 */
    private Double tVal;

    /** z值 */
    private Double zVal;

    /** 相机id */
    private Long cameraId;

}
