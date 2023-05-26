package com.mskyeye.lwradarstationdata.protocol.radar.spx;

import lombok.Data;

/**
 * @ClassName:RadarImage
 * @Description:雷达回波
 * @Author:R.Gong
 * @Date:2022/11/2 16:46
 * @Version:1.0
 **/
@Data
public class RadarImage {

    private short MsgType;      //0x4444：表示有损雷达的回波   0x4433：表示无损雷达的回波

    private byte sBak[] = new byte[9];      //备用

    private short Azi;          //仰角，单位0.01 度

    private short  resolv;      //方位，单位0.01 度

    private int Lat;            //分 暂无效

    private int Lon;            //时 暂无效

    private short Bearing;      //秒 暂无效

    private short Speed;        //空

    private byte  compress;     //计数单位为微秒 暂无效

    private short Length;       //每个数据点代表的距离，单位：米

    private byte Data[] = new byte[1400];             //各个距离库的雷达回波 //无回波时计为零 。回波的数值范围是： 1~255

}
