package com.mskyeye.trace.camera.dhkj.callback;

import com.sun.jna.NativeLong;

public class fDisConnectCB implements fDisConnect{
    public void invoke(NativeLong lLoginID, String pchDVRIP, int nDVRPort, NativeLong dwUser){
        System.out.printf("Device[%s] Port[%d] Disconnect!\n" , pchDVRIP , nDVRPort);
    }
}
