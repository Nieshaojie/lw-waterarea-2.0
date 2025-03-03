package com.mskyeye.trace.camera.hkws.struct;

import com.mskyeye.trace.camera.utils.SdkStructure;

/**
 * @ClassName:NET_DVR_IPADDR
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/12/29 16:23
 * @Version:1.0
 **/
public class NET_DVR_IPADDR extends SdkStructure {

    public byte[] sIpV4 = new byte[16];
    public byte[] byIPv6 = new byte[128];
}
