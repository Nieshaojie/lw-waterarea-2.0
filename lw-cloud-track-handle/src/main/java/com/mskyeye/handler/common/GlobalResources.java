package com.mskyeye.handler.common;

import com.mskyeye.handler.model.AisTrackCache;
import com.mskyeye.handler.model.RadarTrackCache;

import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @ClassName:GlobalResources
 * @Description:全局资源类
 * @Author:R.Gong
 * @Date:2022/11/14 15:57
 * @Version:1.0
 **/
public class GlobalResources {

    //雷达航迹缓存链表
    public static CopyOnWriteArrayList<RadarTrackCache> radarTrackList = new CopyOnWriteArrayList<>();

    //AIS航迹缓存链表
    public static CopyOnWriteArrayList<AisTrackCache> aisTrackList = new CopyOnWriteArrayList<>();

}
