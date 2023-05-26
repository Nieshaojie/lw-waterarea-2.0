package com.mskyeye.lwradarstationdata.protocol.radar.custom;

/**
 * @ClassName:RadarParamHead
 * @Description:雷达参数头
 * @Author:R.Gong
 * @Date:2022/11/2 16:00
 * @Version:1.0
 **/
public class RadarParamHead {

    private byte prefix[] = new byte[4]; //0xFFFFFFFF

    private byte uType;//0,设置参数；1，读取参数：2，上报参数：3，航迹：4，回波
}
