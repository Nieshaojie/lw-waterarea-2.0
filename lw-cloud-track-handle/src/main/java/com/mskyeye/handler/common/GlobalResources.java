package com.mskyeye.handler.common;

import com.mskyeye.handler.model.*;
import com.mskyeye.lwradarstationdata.protocol.ais.YzAisStaticInfo;
import com.mskyeye.lwradarstationdata.protocol.track.LwTrackPacket;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @ClassName:GlobalResources
 * @Description:全局资源类
 * @Author:R.Gong
 * @Date:2022/11/14 15:57
 * @Version:1.0
 **/
public class GlobalResources {

    //雷达航迹缓存map
    public static ConcurrentHashMap<Long,RadarTrackCache> radarTrackMap = new ConcurrentHashMap<>();

    //AIS航迹缓存map
    public static ConcurrentHashMap<Long,AisTrackCache> aisTrackMap = new ConcurrentHashMap<>();

    //北斗航迹缓存map
    public static ConcurrentHashMap<Long,LwTrackPacket> beidouTrackMap = new ConcurrentHashMap<>();

    //航迹处理队列,先进先出,用于衔接接收到的mq数据和处理流程
    public static ConcurrentLinkedQueue<LwTrackPacket> trackHandleQueue = new ConcurrentLinkedQueue<>();

    //巡护设备静态信息
    public static ConcurrentHashMap<Long,PatrolUserInfo> patrolStaticInfo = new ConcurrentHashMap<>();

    //融合结果map(key是MMSI)
    public static ConcurrentHashMap<Long,MergeTrackCache> mergeResultMap = new ConcurrentHashMap<>();

    //历史航迹点Map,用于对比判断是否为第一次预警
    public static ConcurrentHashMap<Long,LwTrackPacket> oldTrackMap = new ConcurrentHashMap<>();

    //AIS静态信息Map
    public static ConcurrentHashMap<Integer, YzAisStaticInfo> aisStaticInfoMap = new ConcurrentHashMap<>();

    //雷达预警计算信息Map
    public static ConcurrentHashMap<Integer, List<RadarAlarmCalInfo>> radarAlarmCalInfoMap = new ConcurrentHashMap<>();

    //AIS告警计算信息Map
    public static ConcurrentHashMap<Integer, AisAlarmCalInfo> aisAlarmCalInfoMap = new ConcurrentHashMap<>();

    //雷达code和组织ID对应Map
    public static ConcurrentHashMap<Integer, Integer> code2DeptIdMap = new ConcurrentHashMap<>();

    //车辆队列
    public static ConcurrentHashMap<String, YzCarInfo> GL_CarMap = new ConcurrentHashMap<>();

    public static final String CAR_INFO = "yz2.0_info:yz_car_info";

    public static YzGpsInfo yzGpsInfo = new YzGpsInfo();
}
