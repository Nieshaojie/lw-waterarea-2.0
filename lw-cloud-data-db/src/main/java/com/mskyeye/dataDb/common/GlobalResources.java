package com.mskyeye.dataDb.common;

import com.mskyeye.lwradarstationdata.protocol.ais.YzAisStaticInfo;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @ClassName:GlobalResources
 * @Description:全局资源
 * @Author:R.Gong
 * @Date:2023/5/25 17:20
 * @Version:1.0
 **/
public class GlobalResources {
    //航迹共享队列
    public static ConcurrentHashMap<Long,YzAisStaticInfo> aisStaticDataMap= new ConcurrentHashMap<>();
}
