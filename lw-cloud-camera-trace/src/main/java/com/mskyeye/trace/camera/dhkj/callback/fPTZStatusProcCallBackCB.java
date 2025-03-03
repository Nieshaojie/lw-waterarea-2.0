package com.mskyeye.trace.camera.dhkj.callback;

import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

/**
 * @ClassName:fPTZStatusProcCallBackCB
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/9/7 9:13
 * @Version:1.0
 **/
public class fPTZStatusProcCallBackCB implements fPTZStatusProcCallBack{
    @Override
    public void invoke(NativeLong lLoginId, NativeLong lAttachHandle, Pointer pBuf, int dwBufLen, long dwUser) {
        // 解析pBuf中的数据并获取PTZ值
        byte[] data = pBuf.getByteArray(0, dwBufLen);

        // 根据数据格式解析PTZ值
        int  pan = data[0];  // 云台水平移动值
        int  tilt = data[1];  // 云台俯仰移动值
        int  zoom = data[2];  // 云台变焦值

        System.out.println("PTZ values: pan=" + pan + ", tilt=" + tilt + ", zoom=" + zoom);
    }
}
