package com.mskyeye.trace.camera.hkws.struct;

import com.mskyeye.trace.camera.utils.SdkStructure;

/**
 * @ClassName:NET_DVR_PTZABSOLUTEEX_CFG
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2024/4/15 13:39
 * @Version:1.0
 **/
public class NET_DVR_PTZABSOLUTEEX_CFG extends SdkStructure {

    public int  dwSize;
    public NET_PTZ_INFO struPTZCtrl ;
    public int  dwFocalLen;
    public float fHorizontalSpeed;
    public float fVerticalSpeed;
    public byte byZoomType;
    public byte[] byRes = new byte[123];

}
