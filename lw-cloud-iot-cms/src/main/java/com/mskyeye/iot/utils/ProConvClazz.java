package com.mskyeye.iot.utils;

import com.mskyeye.lwradarstationdata.protocol.radar.custom.TrackTcpPacket;
import com.mskyeye.lwradarstationdata.protocol.radar.custom.LwTrackPacket;

import java.nio.charset.StandardCharsets;
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

        LwTrackPacket.Content content = new LwTrackPacket.Content();

        content.setSOURCE((int) trackTcpPacket.getUSrc());

        content.setSTATIONID(trackTcpPacket.getStationId());

        content.setTID(trackTcpPacket.getUTargetNum());

        content.setSTATUS((int) trackTcpPacket.getBAdd());

        content.setMMSI(trackTcpPacket.getUMmsi());

        if(trackTcpPacket.getSShipName() == null){
            content.setNAME(null);
        }else {
            content.setNAME(new String(trackTcpPacket.getSShipName(), StandardCharsets.UTF_8));
        }
        content.setLAT(trackTcpPacket.getDTargetLat());

        content.setLON(trackTcpPacket.getDTargetLon());

        content.setCOURSE(trackTcpPacket.getICource());

        content.setSPEED(trackTcpPacket.getISpeed());

        content.setHEAD(trackTcpPacket.getIHead());

        content.setRANGEMETRES(trackTcpPacket.getRangeMetres());

        content.setAZIMUTHDEGREES(trackTcpPacket.getAzimuthDegrees());

        content.setSIZEMETRES(trackTcpPacket.getSizeMetres());

        content.setSIZEDEGREES(trackTcpPacket.getSizeDegrees());

        content.setWEIGHT(trackTcpPacket.getWeight());

        content.setSTRENGTH(trackTcpPacket.getStrength());

        List<LwTrackPacket.Content> list = new ArrayList<>();

        list.add(content);

        trackWSPacket.setITEM(list);

        trackWSPacket.setTIME(trackTcpPacket.getTLastUpdatetime());

        return trackWSPacket;
    }
}
