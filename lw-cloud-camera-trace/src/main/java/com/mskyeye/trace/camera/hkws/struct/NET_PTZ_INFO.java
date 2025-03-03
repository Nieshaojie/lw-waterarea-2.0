package com.mskyeye.trace.camera.hkws.struct;

import com.mskyeye.trace.camera.utils.SdkStructure;

/**
 * @ClassName:NET_PTZ_INFO
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2024/4/15 13:42
 * @Version:1.0
 **/
public class NET_PTZ_INFO extends SdkStructure {
    public float fPan;
    public float fTilt;
    public float fZoom;
    public int dwFocus;
    public byte[] byRes = new byte[4];
}
