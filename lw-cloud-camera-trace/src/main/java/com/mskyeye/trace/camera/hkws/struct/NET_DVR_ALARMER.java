package com.mskyeye.trace.camera.hkws.struct;

import com.mskyeye.trace.camera.utils.SdkStructure;

/**
 * @ClassName:NET_DVR_ALARMER
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/12/27 14:53
 * @Version:1.0
 **/
public class NET_DVR_ALARMER extends SdkStructure {

    public byte byUserIDValid;
    public byte bySerialValid;
    public byte byVersionValid;
    public byte byDeviceNameValid;
    public byte byMacAddrValid;
    public byte byLinkPortValid;
    public byte byDeviceIPValid;
    public byte bySocketIPValid;
    public int lUserID;

    public byte[] sSerialNumber = new byte[48];
    public int dwDeviceVersion;
    public byte[] sDeviceName = new byte[32];
    public byte[] byMacAddr = new byte[6];
    public short wLinkPort;
    public byte[] sDeviceIP = new byte[128];
    public byte[] sSocketIP = new byte[128];
    public byte byIpProtocol;
    public byte[] byRes2 = new byte[11];

}