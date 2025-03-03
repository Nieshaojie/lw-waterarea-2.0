package com.mskyeye.trace.camera.hkws.callback;

import com.sun.jna.Pointer;
import com.sun.jna.win32.StdCallLibrary;

import java.io.UnsupportedEncodingException;

/**
 * @ClassName:MSGCallBack
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/12/27 15:23
 * @Version:1.0
 **/
public interface MSGCallBack extends StdCallLibrary.StdCallCallback {

    public void invoke(int lCommand, Pointer pAlarmer, Pointer pAlarmInfo, int dwBufLen, Pointer pUser) throws UnsupportedEncodingException;
}
