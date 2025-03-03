package com.mskyeye.lwradarstationdata.protocol.radar.custom;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mskyeye.lwradarstationdata.protocol.utils.serializer.LonLatDoubleSerialize;
import com.mskyeye.lwradarstationdata.protocol.utils.serializer.OyzFloatSerialize;
import lombok.Data;

/**
 * @ClassName:TrackTcpPacket
 * @Description:探测站的TCP航迹数据包
 * @Author:R.Gong
 * @Date:2022/11/2 16:54
 * @Version:1.0
 **/
@Data
public class TrackTcpPacket {

    private byte bAdd;                       /*1:更新，0:清除*/

    private byte uSrc;                       /*数据来源，0：雷达,1：真实AIS,3:正向外推AIS,4:反向外推AIS*/

    private long uTargetNum;                 /*目标ID*/

    @JsonSerialize(using = LonLatDoubleSerialize.class)
    private double dTargetLon;               /*目标经度*/

    @JsonSerialize(using = LonLatDoubleSerialize.class)
    private double dTargetLat;               /*目标纬度*/
    @JsonSerialize(using = OyzFloatSerialize.class)
    private float iSpeed;                    /*目标速度*/
    @JsonSerialize(using = OyzFloatSerialize.class)
    private float iCource;                   /*目标航向*/
    @JsonSerialize(using = OyzFloatSerialize.class)
    private float iHead;                     /*目标船头*/

    private int iHeight;                     /*目标高度*/

    private short iWidth;                    /*目标宽度*/

    private int uMmsi;                       /*船舶标号*/

    private int rateOfTurn;                  /*转向率*/

    private byte sShipName[] = new byte[21]; /*船舶名称*/

    @JsonSerialize(using = OyzFloatSerialize.class)
    private float rangeMetres;               /*Tracked Range，距离，2020.12.23，添加 */

    @JsonSerialize(using = OyzFloatSerialize.class)
    private float azimuthDegrees;            /*Tracked Azimuth  ，方位，2020.12.23，添加*/

    @JsonSerialize(using = OyzFloatSerialize.class)
    private float sizeMetres;                /*Smoothed size in metres  ，尺寸，2020.12.23，添加*/

    @JsonSerialize(using = OyzFloatSerialize.class)
    private float sizeDegrees;               /*Smoothed size in degrees ，展宽，2020.12.23，添加 */

    private int weight;                      /*Weight of target (number of samples)  ，样本数，2020.12.23，添加*/

    private int strength;                    /*Strength of target (sum of values) ，强度，2020.12.23，添加*/

    private int stationId;                   /*探测站ID*/

    private long tLastUpdatetime;            //更新时间

}
