package com.mskyeye.trace.camera.dhkj.struct;

import com.mskyeye.trace.camera.utils.SdkStructure;

/**
 * @ClassName:NET_EVENT_INFO_EXTEND
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/9/7 11:33
 * @Version:1.0
 **/
public class NET_EVENT_INFO_EXTEND extends SdkStructure {
    public boolean bRealUTC;
    public byte[] byReserved = new byte[4];
    public NET_TIME_EX stuRealUTC;
    public boolean bIsEventsTypeValid;
    public int szEventsType;
    public byte[] szReserved = new byte[1012];
}
