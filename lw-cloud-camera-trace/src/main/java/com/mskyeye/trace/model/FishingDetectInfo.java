package com.mskyeye.trace.model;

import lombok.Data;

/**
 * @ClassName:FishingDetectInfo
 * @Description:钓鱼检测信息
 * @Author:R.Gong
 * @Date:2023/8/29 9:22
 * @Version:1.0
 **/
@Data
public class FishingDetectInfo {

    private String ip;//相机IP
    private Integer channel;//相机通道号
    private Integer presetNum;//当前预置位号
    private Integer status;//0:启动检测；1：停止检测
    private String ext;//扩展信息

}
