package com.mskyeye.trace.camera.gpl.callback;

import com.sun.jna.Callback;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

public interface fDisConnect extends Callback {
    void invoke(NativeLong lLoginID, String pchDVRIP, int nDVRPort, NativeLong dwUser);
}
