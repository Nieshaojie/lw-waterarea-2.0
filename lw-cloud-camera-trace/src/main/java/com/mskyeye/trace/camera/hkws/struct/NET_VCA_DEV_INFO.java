package com.mskyeye.trace.camera.hkws.struct;

import com.mskyeye.trace.camera.utils.SdkStructure;

/**
 * @ClassName:NET_VCA_DEV_INFO
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/12/29 14:58
 * @Version:1.0
 **/
public class NET_VCA_DEV_INFO extends SdkStructure {
    public NET_DVR_IPADDR struDevIP = new NET_DVR_IPADDR();

    public int wPort;
    public byte byChannel;
    public byte byIvmsChannel;

}
