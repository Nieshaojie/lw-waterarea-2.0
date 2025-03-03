package com.mskyeye.trace.camera.hkws.callback;

import com.mskyeye.trace.camera.hkws.struct.NET_DVR_DEVICEINFO_V30;
import com.sun.jna.Callback;
import com.sun.jna.Pointer;

/**
 * @ClassName:fLoginResultCallBack
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/12/27 16:20
 * @Version:1.0
 **/
public interface fLoginResultCallBack extends Callback {

    void invoke(int lUserID, int dwResult, NET_DVR_DEVICEINFO_V30 lpDeviceInfo, Pointer pUser);
}
