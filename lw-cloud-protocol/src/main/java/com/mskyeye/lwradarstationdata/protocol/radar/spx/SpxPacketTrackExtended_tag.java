package com.mskyeye.lwradarstationdata.protocol.radar.spx;

import lombok.Data;

/**
 * @ClassName:SpxPacketTrackExtended_tag
 * @Description:spx航迹包扩展标签
 * @Author:R.Gong
 * @Date:2022/11/2 16:42
 * @Version:1.0
 **/
@Data
public class SpxPacketTrackExtended_tag {

    SpxPacketTrackNormal_tag norm = new SpxPacketTrackNormal_tag();

    private int netSize;

    private int extMask;

    private float radialSpeedMps;

    private float radialSpeedSD;

    private int age;

    private float latDegs;

    private float longDegs;

    private int msgTimeSecs;

    private int msgTimeUsecs;

    private int sensorDataBytes;

    private byte sensorSpecificData[] = new byte[32];

    private float altitudeMetres;

    //SPxPacketTrackFusion     fusion;

    private float latDegsMeas;

    private float longDegsMeas;

    private float gateStartRangeMetres;

    private float gateEndRangeMetres;

    private float gateStartAziDegs;

    private float gateEndAziDegs;

    private float cpaMetres;

    private float tcpaSecs;

    //SPxPacketTrackThreat      threat;

    private short descNetSize;
	/*char                     description[254];
	float                    accelMps;
	float                    turnRateDps;
	unsigned short           status;*/

}
