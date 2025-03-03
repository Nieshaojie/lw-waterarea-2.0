package com.mskyeye.trace.camera.dhkj.struct;

import com.mskyeye.trace.camera.utils.SdkStructure;
import com.sun.jna.Pointer;

/**
 * @ClassName:NET_SCADA_DEVICE_LIST
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/9/7 13:35
 * @Version:1.0
 **/
public class NET_SCADA_DEVICE_LIST extends SdkStructure
{
    public int                            dwSize;
    public int                            nMax;                                 // 用户分配的结构体个数
    public int                            nRet;                                 // 设备实际返回的有效结构体个数
    public Pointer pstuDeviceIDInfo;                     // 监测设备信息,用户分配内存,大小为sizeof(NET_SCADA_DEVICE_ID_INFO)*nMax，指向NET_SCADA_DEVICE_ID_INFO

    public NET_SCADA_DEVICE_LIST()
    {
        this.dwSize = this.size();
    }
}
