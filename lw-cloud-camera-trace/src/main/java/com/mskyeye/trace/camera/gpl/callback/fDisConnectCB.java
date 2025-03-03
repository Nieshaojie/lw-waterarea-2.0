package com.mskyeye.trace.camera.gpl.callback;

import com.sun.jna.NativeLong;

/**
 * @ClassName:fDisConnectCB
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2024/4/9 16:12
 * @Version:1.0
 **/
public class fDisConnectCB implements fDisConnect {
    public void invoke(NativeLong lLoginID, String pchDVRIP, int nDVRPort, NativeLong dwUser){
        System.out.printf("Device[%s] Port[%d] Disconnect!\n" , pchDVRIP , nDVRPort);
    }
}
