package com.mskyeye.trace.camera.hkws.struct;

import com.mskyeye.trace.camera.utils.SdkStructure;

/**
 * @ClassName:NET_VCA_POLYGON
 * @Description:TODO
 * @Author:R.Gong
 * @Date:2023/12/29 16:05
 * @Version:1.0
 **/
public class NET_VCA_POLYGON extends SdkStructure {

    public long dwPointNum;
    public NET_VCA_POINT[] struPos = new NET_VCA_POINT[10];
}
