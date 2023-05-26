package com.mskyeye.lwradarstationdata.protocol.radar.spx;

import lombok.Data;

/**
 * @ClassName:SpxPacketTrackerStatus_tag
 * @Description:spx航迹状态标签
 * @Author:R.Gong
 * @Date:2022/11/2 16:12
 * @Version:1.0
 **/
@Data
public class SpxPacketTrackerStatus_tag {

    /*Reporting control*/
    private int reportType;       /*0SPX_PACKET_TYPEB_TRACK_MIN/NORM*/

    private int reportExtFlags;   /*Bitmask for content of extended reports */

    /*Statistics*/
    private short numTracks;        /*Number of tracks */

    private short numClusters;      /*Number of clusters */

    private short numHypotheses;    /*Number of hypotheses */

    private short numTrackLinks;    /*Number of track links */

    private int numMeasAccepted;  /*Number of measurements accepted */

    private int numMeasRejected;  /*Number of measurements rejected */

    /*Status fields*/
    private float updatePreriod;    /*Update period in use (seconds) */

    private short overloadFlags;    /*Bitmask of SPX_MHT_STATUS_OVERLOAD*/

    private byte senderID;         /*Sender identification */

    private byte reserved31;

    /*Reserved fields*/
    private int serverVersion;    /*Server version, eg Vx.y = 0x00xxyy00 */

    private float radarLat;

    private float radarLon;

    private int reserved44;

    private int reserved48;

    private int reserved52;

    private int reserved56;

    private int reserved60;

}
