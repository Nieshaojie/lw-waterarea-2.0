package com.mskyeye.trace.camera.dhkj.callback;

import com.sun.jna.NativeLong;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;

/**
 * 断线回调
 */
public interface fDisConnect extends StdCallCallback {
    public void invoke(NativeLong lLoginID, String pchDVRIP, int nDVRPort, NativeLong dwUser);
}
