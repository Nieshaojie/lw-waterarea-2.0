package com.mskyeye.lwradarstationdata.protocol.radar.custom;

import lombok.Data;

/**
 * @ClassName:RadarRealInfo
 * @Description:自定义雷达实时信息类
 * @Author:R.Gong
 * @Date:2022/11/9 15:32
 * @Version:1.0
 **/
@Data
public class RadarRealInfo{

    private int radarCode;

    private double radarLon;//雷达经度

    private double radarLat;//雷达纬度

    private int radarStatus;//雷达状态：0 不正常 1正常

}
