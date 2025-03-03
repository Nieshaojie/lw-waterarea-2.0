package com.mskyeye.trace.camera.gpl.callback;


import com.sun.jna.NativeLong;

/**
 * @ClassName:HaveReConnect
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2024/4/9 16:16
 * @Version:1.0
 **/
public class HaveReConnect implements fHaveReConnect {
    public void invoke(NativeLong m_hLoginHandle, String pchDVRIP, int nDVRPort, NativeLong dwUser) {
        System.out.printf("ReConnect Device[%s] Port[%d]\n", pchDVRIP, nDVRPort);
    }
}
