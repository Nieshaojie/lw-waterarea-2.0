package com.mskyeye.trace.camera.gpl.callback;

import com.sun.jna.Callback;
import com.sun.jna.NativeLong;
import com.sun.jna.win32.StdCallLibrary.StdCallCallback;


public interface fHaveReConnect extends Callback {
    public void invoke(NativeLong lLoginID, String pchDVRIP, int nDVRPort, NativeLong dwUser);
}
