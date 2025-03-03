package com.mskyeye.trace.camera.dhkj.callback;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

// 消息回调函数原形(pBuf内存由SDK内部申请释放)
public interface fMessCallBack extends StdCallLibrary.StdCallCallback {
    public boolean invoke(NativeLong lCommand , NativeLong lLoginID , Pointer pStuEvent , int dwBufLen , String strDeviceIP , NativeLong nDevicePort , NativeLong dwUser);
}
