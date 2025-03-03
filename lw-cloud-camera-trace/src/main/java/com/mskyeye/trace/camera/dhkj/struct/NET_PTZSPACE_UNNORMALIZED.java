package com.mskyeye.trace.camera.dhkj.struct;

import com.mskyeye.trace.camera.utils.SdkStructure;

/**
 * @ClassName:NET_PTZSPACE_UNNORMALIZED
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/9/7 11:31
 * @Version:1.0
 **/
public class NET_PTZSPACE_UNNORMALIZED extends SdkStructure {
    public int nPosX;
    public int nPosY;
    public int nZoom;
    public byte[] byReserved = new byte[52];
}
