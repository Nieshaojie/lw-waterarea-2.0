package com.mskyeye.iot.utils;

import com.mskyeye.lwradarstationdata.protocol.radar.custom.FlyTrackTcpPacket;
import com.mskyeye.lwradarstationdata.protocol.radar.custom.TrackTcpPacket;
import com.mskyeye.lwradarstationdata.protocol.track.Content;
import com.mskyeye.lwradarstationdata.protocol.track.LwTrackPacket;

import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName:ProConvClazz
 * @Description:协议转换类
 * @Author:R.Gong
 * @Date:2022/12/6 15:30
 * @Version:1.0
 **/
public class ProConvClazz {

    //航迹数据TCP转websocket
    public static LwTrackPacket trackTcp2WS(TrackTcpPacket trackTcpPacket) throws Exception{

        LwTrackPacket trackWSPacket = new LwTrackPacket();

        Content content = new Content();

        content.setSOURCE((int) trackTcpPacket.getUSrc());

        content.setSTATIONID(trackTcpPacket.getStationId());

        content.setTID(trackTcpPacket.getUTargetNum());

        content.setSTATUS((int) trackTcpPacket.getBAdd());

        content.setMMSI((long) trackTcpPacket.getUMmsi());

        content.setLAT(trackTcpPacket.getDTargetLat());

        content.setLON(trackTcpPacket.getDTargetLon());

        content.setCOURSE(trackTcpPacket.getICource());

        content.setSPEED(trackTcpPacket.getISpeed());

        content.setHEAD(trackTcpPacket.getIHead());

        content.setNAVIGATION(trackTcpPacket.getISpeed() > 3.0 ?1:0);

        content.setRANGEMETRES(trackTcpPacket.getRangeMetres());

        content.setAZIMUTHDEGREES(trackTcpPacket.getAzimuthDegrees());

        content.setSIZEMETRES(trackTcpPacket.getSizeMetres());

        content.setSIZEDEGREES(trackTcpPacket.getSizeDegrees());

        content.setWEIGHT(trackTcpPacket.getWeight());

        content.setSTRENGTH(trackTcpPacket.getStrength());

        content.setALT(trackTcpPacket.getAlt());

        List<Content> list = new ArrayList<>();

        list.add(content);

        trackWSPacket.setITEM(list);

        trackWSPacket.setTIME(System.currentTimeMillis());//用服务器时间,防止各雷达站时间不同步

        return trackWSPacket;
    }

    //航迹数据TCP转websocket
    public static LwTrackPacket flyTrackTcp2WS(FlyTrackTcpPacket trackTcpPacket) throws Exception{

        LwTrackPacket trackWSPacket = new LwTrackPacket();

        Content content = new Content();

        content.setSOURCE(6);

        content.setSTATIONID(trackTcpPacket.getStationId());

        content.setTID(trackTcpPacket.getTargetId());

        content.setSTATUS((int) trackTcpPacket.getBAdd());

        content.setFLYTYPE(trackTcpPacket.getType());

        content.setALT(trackTcpPacket.getAlt());

        content.setLAT(trackTcpPacket.getLat());

        content.setLON(trackTcpPacket.getLon());

        content.setSPEED((float)trackTcpPacket.getSpeed3D());



        List<Content> list = new ArrayList<>();

        list.add(content);

        trackWSPacket.setITEM(list);

        trackWSPacket.setTIME(System.currentTimeMillis());//用服务器时间,防止各雷达站时间不同步

        return trackWSPacket;
    }
}
