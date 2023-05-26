package com.mskyeye.lwradarstationdata.protocol.radar.spx;

import lombok.Data;

/**
 * @ClassName:SpxPacketTrackMinimal_tag
 * @Description:spx最小航迹包标签
 * @Author:R.Gong
 * @Date:2022/11/2 16:16
 * @Version:1.0
 **/
@Data
public class SpxPacketTrackMinimal_tag {

    private int id;                  /*Track ID (public) */

    private byte senderID;            /*Sender  identification  */

    private byte status;              /*Track status (provisional, confirmed etc.)  */

    private byte numCoasts;           /*Number of consecutive coasts  */

    private byte id_ttm;              /*TTM id (or 0 if no ID)  */

    private float rangeMetres;         /*Tracked Range */

    private float azimuthDegrees;      /*Tracked Azimuth  */

    private float speedMps;            /*Speed  */

    private float courseDegrees;       /*Course  */

    private float sizeMetres;          /*Smoothed size in metres  */

    private float sizeDegrees;         /*Smoothed size in degrees  */

    private int weight;              /*Weight of target (number of samples)  */

    private int strength;            /*Strength of target (sum of values) */

    private byte flags;               /*Target flags (SPX_PACKET_TRACK_FLAGS …) */

    private byte reserved1;

    private short reserved2;

    private int reserved3;

    private int reserved4;

    private int reserved5;

}
