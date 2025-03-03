package com.mskyeye.trace.camera.hkws.struct;

import com.mskyeye.trace.camera.utils.SdkStructure;

/**
 * @ClassName:NET_DVR_PTZPOS_PARAM
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/8/21 15:20
 * @Version:1.0
 **/
public class NET_DVR_PTZPOS_PARAM extends SdkStructure {

    public float  fPanPos;//水平参数，精确到小数点后1位
    public float  fTiltPos;//垂直参数，精确到小数点后1位
    public float  fZoomPos;//变倍参数，精确到小数点后1位
    public byte[] byRes = new byte[16] ;
}
