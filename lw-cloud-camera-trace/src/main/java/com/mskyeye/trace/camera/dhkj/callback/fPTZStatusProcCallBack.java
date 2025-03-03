package com.mskyeye.trace.camera.dhkj.callback;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

/**
 * 订阅云台元数据接口回调函数原型
 * pBuf 现阶段主要为 NET_PTZ_LOCATION_INFO
 */
public interface fPTZStatusProcCallBack extends StdCallLibrary.StdCallCallback {
    public void invoke (NativeLong lLoginId, NativeLong lAttachHandle, Pointer pBuf, int dwBufLen, long dwUser);
}
