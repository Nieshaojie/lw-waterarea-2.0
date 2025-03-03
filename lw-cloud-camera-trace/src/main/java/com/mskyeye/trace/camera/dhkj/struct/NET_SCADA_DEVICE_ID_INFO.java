package com.mskyeye.trace.camera.dhkj.struct;

import com.mskyeye.trace.camera.utils.SdkStructure;

/**
 * @ClassName:NET_SCADA_DEVICE_ID_INFO
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/9/7 13:34
 * @Version:1.0
 **/
public  class NET_SCADA_DEVICE_ID_INFO extends SdkStructure
{
    public byte[]                         szDeviceID = new byte[64];  // 设备id
    public byte[]                         szDevName = new byte[64];  // 设备名称, 和CFG_SCADA_DEV_INFO配置中的szDevName一致
    public byte[]                         reserve = new byte[1024];
}
