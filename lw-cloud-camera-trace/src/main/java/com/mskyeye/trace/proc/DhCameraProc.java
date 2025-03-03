package com.mskyeye.trace.proc;

import com.mskyeye.trace.model.YzCameraInfo;
import com.sun.jna.Pointer;
import org.springframework.stereotype.Component;

/**
 * @ClassName:DhCameraProc
 * @Description:大华相机功能类
 * @Author:R.Gong
 * @Date:2023/9/6 13:27
 * @Version:1.0
 **/
@Component
public class DhCameraProc {

    /**
     * 通过PTZ直接引导相机
     *
     * @param yzCameraInfo
     * @param pVal
     * @param tVal
     * @param zVal
     * @return
     * @throws Exception
     */
    public boolean ptzControl(YzCameraInfo yzCameraInfo, double pVal, double tVal, double zVal) throws Exception {

        boolean result = yzCameraInfo.getDhNetSDK().CLIENT_DHPTZControlEx2(Long.valueOf(yzCameraInfo.getLoginInfo()),
                0,0x43, (int) (pVal * 10),(int) (tVal * 10),(int) (zVal * 10),false,new Pointer(0));
        if (!result)
            System.out.println("错误码是:" + yzCameraInfo.getDhNetSDK().CLIENT_GetLastError());
        return result;
    }
}
