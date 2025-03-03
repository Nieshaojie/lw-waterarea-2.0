package com.mskyeye.trace.camera.hkws.struct;

import com.mskyeye.trace.camera.utils.SdkStructure;

/**
 * @ClassName:NET_DVR_DEVICEINFO_V40
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/12/27 16:13
 * @Version:1.0
 **/
public class NET_DVR_DEVICEINFO_V40 extends SdkStructure {

    public NET_DVR_DEVICEINFO_V30 struDeviceV30 = new NET_DVR_DEVICEINFO_V30();
    public byte bySupportLock;
    public byte byRetryLoginTime;
    public byte byPasswordLevel;
    public byte byProxyType;
    public int dwSurplusLockTime;
    public byte byCharEncodeType;
    public byte bySupportDev5;
    public byte byLoginMode;
    public byte[] byRes2 = new byte[253];
}
