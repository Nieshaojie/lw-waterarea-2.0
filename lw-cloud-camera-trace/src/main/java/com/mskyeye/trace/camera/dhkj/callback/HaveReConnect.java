package com.mskyeye.trace.camera.dhkj.callback;

import com.sun.jna.NativeLong;

/**
 * @ClassName:HaveReConnect
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/9/6 11:15
 * @Version:1.0
 **/
//网络连接恢复，设备重连成功回调
// 通过 CLIENT_SetAutoReconnect 设置该回调函数，当已断线的设备重连成功时，SDK会调用该函数
public class HaveReConnect implements fHaveReConnect {
    public void invoke(NativeLong m_hLoginHandle, String pchDVRIP, int nDVRPort, NativeLong dwUser) {
        System.out.printf("ReConnect Device[%s] Port[%d]\n", pchDVRIP, nDVRPort);
    }
}
