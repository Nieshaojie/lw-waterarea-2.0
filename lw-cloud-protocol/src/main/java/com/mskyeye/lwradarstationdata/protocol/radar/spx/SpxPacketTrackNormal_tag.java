package com.mskyeye.lwradarstationdata.protocol.radar.spx;

import lombok.Data;

/**
 * @ClassName:SpxPacketTrackNormal_tag
 * @Description:spx一般航迹包标签
 * @Author:R.Gong
 * @Date:2022/11/2 16:38
 * @Version:1.0
 **/
@Data
public class SpxPacketTrackNormal_tag {

    SpxPacketTrackMinimal_tag min = new SpxPacketTrackMinimal_tag();

    private float xMetres;              /*Tracked cartesian position  */

    private float yMetres;              /*Tracked cartesian position  */

    private float measRange;            /*Measured polar position */

    private float measAzimuth;          /*Measured polar position */

    private float measSizeMetres;       /*Measured size in range  */

    private float measSizeDegrees;      /*Measured size in azimuth  */

    private float sdRange;              /*SD of range measurements  */

    private float sdAzimuth;            /*SD of azimuth measurements*/

    private float sdRangeSize;          /*SD of size-in-range measurements */

    private float sdAzimuthSize;        /*SD of size-in-azimuth measurements  */

    private short numDetectionPQ;       /*Recent hit count P (LSB out of Q (MSB) */

    private short trackClass;           /*Track class (set through track class rules)  */

    private int reserved1;

    private int reserved2;

    private int reserved3;

}
