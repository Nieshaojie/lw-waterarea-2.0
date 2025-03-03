package com.mskyeye.trace.camera.hkws.struct;

import com.mskyeye.trace.camera.utils.SdkStructure;

/**
 * @ClassName:NET_DVR_SETUPALARM_PARAM
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/12/27 15:02
 * @Version:1.0
 **/
public class NET_DVR_SETUPALARM_PARAM extends SdkStructure {

    public int dwSize;
    public byte byLevel;
    public byte byAlarmInfoType;
    public byte byRetAlarmTypeV40;
    public byte byRetDevInfoVersion;
    public byte byRetVQDAlarmType;
    public byte byFaceAlarmDetection;
    public byte bySupport;
    public byte byBrokenNetHttp;
    public short wTaskNo;
    public byte byDeployType;
    public byte[] byRes1 = new byte[3];
    public byte byAlarmTypeURL;
    public byte byCustomCtrl;
    
}
