package com.mskyeye.trace.camera.hkws.struct;

import com.sun.jna.Structure;

import java.util.Arrays;
import java.util.List;

/**
 * @ClassName:NET_DVR_SHIPSDETECTION_ALARM
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/12/29 14:56
 * @Version:1.0
 **/
public class NET_DVR_SHIPSDETECTION_ALARM extends Structure {

    public long dwSize;
    public NET_VCA_DEV_INFO struDevInfo = new NET_VCA_DEV_INFO();
    public long dwRelativeTime;
    public long dwAbsTime;
    public byte byShipsNum;
    public byte byShipsNumHead;
    public byte byShipsNumEnd;
    public byte byRes1;
    public NET_DVR_SHIPSINFO[] struShipInfo = new NET_DVR_SHIPSINFO[20];
    public long dwPicLen;
    public long dwThermalPicLen;

    public byte[] pPicBuffer = new byte[1];
    public byte[] pThermalPicBuffer = new byte[1];
    public short wDevInfoIvmsChannelEx;
    public byte byTimeDiffFlag;
    public byte cTimeDifferenceH;
    public byte cTimeDifferenceM;
    public byte[] byRes = new byte[251];

    @Override
    protected List<String> getFieldOrder() {
        return Arrays.asList(
                "dwSize",
                "struDevInfo",
                "dwRelativeTime",
                "dwAbsTime",
                "byShipsNum",
                "byShipsNumHead",
                "byShipsNumEnd",
                "byRes1",
                "struShipInfo",
                "dwPicLen",
                "dwThermalPicLen",
                "pPicBuffer",
                "pThermalPicBuffer",
                "wDevInfoIvmsChannelEx",
                "byTimeDiffFlag",
                "cTimeDifferenceH",
                "cTimeDifferenceM",
                "byRes"
        );
    }

}
