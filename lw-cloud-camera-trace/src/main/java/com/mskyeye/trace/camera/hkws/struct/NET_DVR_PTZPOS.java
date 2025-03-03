package com.mskyeye.trace.camera.hkws.struct;

import com.mskyeye.trace.camera.utils.SdkStructure;

/**
 * @ClassName:NET_DVR_PTZPOS
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/8/7 13:58
 * @Version:1.0
 **/
public class NET_DVR_PTZPOS extends SdkStructure {

    public short wAction;//获取时该字段无效
    public short wPanPos;//水平参数
    public short wTiltPos;//垂直参数
    public short wZoomPos;//变倍参数

}
