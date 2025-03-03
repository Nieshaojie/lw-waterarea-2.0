package com.mskyeye.trace.camera.dhkj.callback;

import com.sun.jna.NativeLong;
import com.sun.jna.win32.StdCallLibrary;

// 网络连接恢复回调函数原形
public interface fHaveReConnect extends StdCallLibrary.StdCallCallback {
    public void invoke(NativeLong lLoginID, String pchDVRIP, int nDVRPort, NativeLong dwUser);
}
