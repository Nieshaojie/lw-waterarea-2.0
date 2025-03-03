package com.mskyeye.trace.model;

import lombok.Data;

/**
 * @ClassName:HpCameraInfo
 * @Description:和普相机信息
 * @Author:R.Gong
 * @Date:2023/8/7 19:13
 * @Version:1.0
 **/
@Data
public class HpCameraInfo {

    private Long id;

    private String ip;

    private String port;

    private String userName;

    private String pwd;

    private String token;

    private Double pVal;

    private Double tVal;

    private Double zVal;

    /** 修正角度 */
    private Long angle;

    /** 高度 */
    private Long height;

    private Double lon;

    private Double lat;

    private Integer traceType = 0;//跟踪类型 0:未跟踪 1:联动跟踪 2:框选跟踪 3:图像跟踪


}
